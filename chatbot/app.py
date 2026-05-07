"""
DEMP Chatbot — FastAPI service backed by an open-source LLM (Ollama).

Features:
- Uses a local open-source LLM via Ollama (default model: llama3.2).
- The project knowledge base (knowledge_base.py) is injected as the system
  prompt so the model is *project-aware* and can answer questions about
  creating events, booking tickets, payments, dashboards, the tech stack,
  etc., AND general questions.
- Maintains short per-session conversation history.
- Graceful fallback to keyword matching if Ollama is not reachable.

Setup:
    1. Install Ollama: https://ollama.com/download
    2. Pull a model:    `ollama pull llama3.2`        (~2 GB, recommended)
                   or:  `ollama pull phi3:mini`       (~2.3 GB, faster)
                   or:  `ollama pull qwen2.5:3b`      (~2 GB)
    3. Make sure Ollama is running (it auto-starts on install).
    4. pip install -r requirements.txt
    5. uvicorn app:app --reload --port 8001

Environment overrides:
    OLLAMA_URL    default: http://localhost:11434
    OLLAMA_MODEL  default: llama3.2
"""
from __future__ import annotations

import os
import re
import uuid
from typing import Optional

import httpx
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

from knowledge_base import KB, FALLBACK, PROJECT_OVERVIEW
import create_event_flow as cef

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.2")
DEMP_API_URL = os.getenv("DEMP_API_URL", "http://localhost:8080")
MAX_HISTORY = 10  # last N turns kept per session

app = FastAPI(title="DEMP Chatbot (LLM)", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# System prompt built from the curated knowledge base
# ---------------------------------------------------------------------------
def _build_system_prompt() -> str:
    kb_text = "\n".join(
        f"- **{e['intent']}**: {e['answer']}" for e in KB if e["intent"] not in
        {"greeting", "thanks", "bye"}
    )
    return f"""You are the **DEMP Assistant**, a friendly chatbot for the Digital Event Management Platform.

ABOUT THE PROJECT
{PROJECT_OVERVIEW}

PROJECT KNOWLEDGE BASE (use this as the authoritative source for any
question about the platform; answer concisely and use markdown formatting):
{kb_text}

GUIDELINES
- For questions about DEMP (events, tickets, payments, dashboards, signup,
  architecture, how to run, etc.), ground your answer in the knowledge base.
- For general questions unrelated to DEMP, answer normally as a helpful
  assistant.
- Keep answers concise (under ~180 words unless asked for detail).
- Use markdown: **bold**, *italic*, `code`, and `- ` bullet lists.
- If you genuinely don't know, say so briefly and suggest what to try.
"""

SYSTEM_PROMPT = _build_system_prompt()

# Per-session chat history: { session_id: [ {role, content}, ... ] }
SESSIONS: dict[str, list[dict]] = {}


# ---------------------------------------------------------------------------
# Models
# ---------------------------------------------------------------------------
class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000)
    session_id: Optional[str] = None
    # Optional context forwarded by the React app so the bot can act on the
    # user's behalf (e.g. create an event using their JWT).
    auth_token: Optional[str] = None
    user_id: Optional[int] = None


class ChatResponse(BaseModel):
    reply: str
    session_id: str
    source: str  # "llm" | "fallback"
    model: Optional[str] = None


# ---------------------------------------------------------------------------
# Keyword-matcher fallback (used when Ollama is unreachable)
# ---------------------------------------------------------------------------
_TOKEN_RE = re.compile(r"[a-zA-Z']+")
_STOPWORDS = {
    "the", "a", "an", "is", "are", "am", "to", "of", "for", "on", "in",
    "at", "by", "and", "or", "with", "i", "you", "we", "they", "it",
    "do", "does", "did", "can", "could", "would", "should", "shall",
    "will", "my", "your", "me", "us", "any", "some", "this", "that",
    "these", "those", "how", "what", "where", "when", "why", "who",
    "please", "tell", "about", "there", "here", "so", "if", "be",
    "have", "has", "had", "get", "got", "want", "need", "new",
}

# Light synonym map — normalize common alt phrasings to canonical KB terms
# so paraphrased questions still hit the right intent.
_SYNONYMS = {
    "signin": "login", "sign in": "login", "log in": "login",
    "logon": "login", "sign on": "login",
    "signup": "sign up", "register account": "sign up", "join": "sign up",
    "logout": "log out", "sign out": "log out", "signout": "log out",
    "buy": "book", "purchase": "book", "reserve": "book",
    "cost": "price", "fees": "price", "fee": "price",
    "refund": "refund payment",
    "host": "create event", "create": "create event",
    "make event": "create event", "new event": "create event",
    "events list": "all events", "see events": "all events",
    "show events": "all events", "list events": "all events",
    "current events": "upcoming events", "available events": "upcoming events",
    "present events": "upcoming events",
    "stack": "tech stack", "frameworks": "tech stack",
    "technologies": "tech stack",
    "db": "database", "mysql": "database",
    "endpoints": "api", "rest": "api", "routes backend": "api",
    "pages": "frontend routes", "navigation": "frontend routes",
    "ports": "port", "url": "port",
    "jwt": "auth", "token": "auth", "bearer": "auth",
    "rbac": "roles", "permission": "roles", "permissions": "roles",
    "category": "event types", "categories": "event types",
    "online": "virtual", "in person": "in_person",
    "seat": "capacity", "seats": "capacity", "sold out": "capacity",
    "qr": "qr code", "ticket qr": "qr code",
    "speaker": "speakers", "presenter": "speakers", "guest": "speakers",
    "dashboard": "organizer dashboard",
}


def _normalize(text: str) -> str:
    out = " " + text.lower() + " "
    # Apply longer phrases first to avoid partial overwrites
    for phrase, repl in sorted(_SYNONYMS.items(), key=lambda kv: -len(kv[0])):
        out = out.replace(" " + phrase + " ", " " + repl + " ")
    return out.strip()


def _tokens(text: str) -> set[str]:
    return {t for t in _TOKEN_RE.findall(text.lower()) if t not in _STOPWORDS and len(t) > 1}


def _score(message: str, keywords: list[str]) -> float:
    msg_norm = " " + _normalize(message) + " "
    msg_tokens = _tokens(msg_norm)
    total = 0.0
    for kw in keywords:
        kw_l = kw.lower()
        # Exact phrase match (high weight, scaled by phrase length)
        if kw_l in msg_norm:
            total += 1.5 + 0.5 * kw_l.count(" ")
            continue
        # Token overlap (partial credit).
        # For multi-word keywords, require at least 2 overlapping tokens —
        # otherwise a single common word like "event" lets unrelated intents
        # (e.g. *create event*) outscore the right one (e.g. *edit event*).
        kw_tokens = _tokens(kw_l)
        if not kw_tokens:
            continue
        overlap = kw_tokens & msg_tokens
        if not overlap:
            continue
        if len(kw_tokens) >= 2 and len(overlap) < 2:
            continue
        total += 0.7 * (len(overlap) / len(kw_tokens))
    return total


def _topic_suggestions() -> str:
    intents = [e["intent"] for e in KB if e["intent"] not in {"greeting", "thanks", "bye"}]
    pretty = [i.replace("_", " ") for i in intents]
    return ", ".join(f"*{p}*" for p in pretty[:18])


def keyword_fallback(message: str) -> str:
    msg_tokens = _tokens(_normalize(message))
    scored = []
    for entry in KB:
        s = _score(message, entry["keywords"])
        ans_tokens = _tokens(entry["answer"])
        if ans_tokens and msg_tokens:
            ans_overlap = len(msg_tokens & ans_tokens)
            if ans_overlap:
                s += 0.25 * ans_overlap
        if s > 0:
            scored.append((s, entry))

    if not scored:
        return (
            "I couldn't find a match for that. I can help with topics like:\n"
            f"{_topic_suggestions()}.\n\nTry rephrasing, or ask *show present events*."
        )

    scored.sort(key=lambda t: t[0], reverse=True)
    top_score, top = scored[0]

    # Strong match — return directly.
    if top_score >= 0.8:
        return top["answer"]

    # Weak match — return best guess but offer alternatives if a close 2nd exists.
    if len(scored) > 1 and (scored[1][0] / max(top_score, 0.01)) > 0.6:
        alt = scored[1][1]["intent"].replace("_", " ")
        return (
            top["answer"]
            + f"\n\n_Not what you meant? You can also ask about **{alt}**._"
        )
    return top["answer"]


# ---------------------------------------------------------------------------
# Ollama call
# ---------------------------------------------------------------------------
async def call_ollama(history: list[dict]) -> Optional[str]:
    """
    Calls Ollama's /api/chat endpoint. Returns the assistant message content
    or None if the call fails.
    """
    payload = {
        "model": OLLAMA_MODEL,
        "messages": [{"role": "system", "content": SYSTEM_PROMPT}, *history],
        "stream": False,
        "options": {"temperature": 0.4, "num_predict": 512},
    }
    try:
        async with httpx.AsyncClient(timeout=120.0) as client:
            r = await client.post(f"{OLLAMA_URL}/api/chat", json=payload)
            r.raise_for_status()
            data = r.json()
            return data.get("message", {}).get("content", "").strip() or None
    except Exception as exc:  # noqa: BLE001 - we want a graceful fallback
        print(f"[chatbot] Ollama call failed: {exc}")
        return None


# ---------------------------------------------------------------------------
# Live events lookup (DEMP backend via api-gateway)
# ---------------------------------------------------------------------------
_EVENTS_INTENT_RE = re.compile(
    r"\b("
    r"present|current|available|upcoming|ongoing|live|today'?s?|this week|"
    r"happening|active|now|list .*events?|show .*events?|"
    r"what .*events?|which .*events?|any events?|all events?|events? list|"
    r"find events?|browse events?"
    r")\b",
    re.IGNORECASE,
)


def _wants_event_listing(message: str) -> bool:
    msg = message.lower()
    if "event" not in msg and "events" not in msg:
        return False
    return bool(_EVENTS_INTENT_RE.search(msg))


def _fmt_event(ev: dict) -> str:
    name = ev.get("eventName") or ev.get("name") or "Untitled event"
    date = ev.get("date") or ""
    time = ev.get("time") or ""
    when = " · ".join(x for x in [date, time] if x)
    etype = ev.get("eventType") or ""
    addr = ev.get("address") or {}
    venue_parts = []
    if isinstance(addr, dict):
        for k in ("venue", "city", "state", "country"):
            v = addr.get(k)
            if v:
                venue_parts.append(str(v))
    venue = ", ".join(venue_parts)
    desc = (ev.get("description") or "").strip()
    if len(desc) > 110:
        desc = desc[:107] + "..."

    cur = ev.get("currentAttendees")
    mx = ev.get("maxAttendees")
    seats = ""
    if isinstance(cur, int) and isinstance(mx, int) and mx > 0:
        left = max(mx - cur, 0)
        seats = f" · **{left}** seats left"

    line1 = f"**{name}**"
    if etype:
        line1 += f" _({etype})_"
    parts = [line1]
    if when:
        parts.append(f"  📅 {when}{seats}")
    if venue:
        parts.append(f"  📍 {venue}")
    if desc:
        parts.append(f"  {desc}")
    return "\n".join(parts)


async def fetch_live_events(limit: int = 8) -> Optional[str]:
    """Fetch events from the DEMP api-gateway. Returns markdown or None."""
    url = f"{DEMP_API_URL}/api/events/all"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            r = await client.get(url)
            if r.status_code != 200:
                return None
            events = r.json()
    except Exception as exc:  # noqa: BLE001
        print(f"[chatbot] events fetch failed: {exc}")
        return None

    if not isinstance(events, list):
        return None

    # Filter out deleted / inactive if those flags exist
    visible = [
        e for e in events
        if isinstance(e, dict) and not e.get("isDeleted")
        and (e.get("activeStatus") in (None, "ACTIVE", "active", "APPROVED"))
    ]

    # Prefer upcoming (date today or later); if none, show all visible
    from datetime import date as _date
    today = _date.today().isoformat()
    upcoming = [e for e in visible if (e.get("date") or "") >= today]
    pool = upcoming or visible

    # Sort by date ascending
    pool.sort(key=lambda e: (e.get("date") or "", e.get("time") or ""))

    if not pool:
        return (
            "There are **no events** available right now. "
            "Check back later, or organizers can create one from the *Create Event* page."
        )

    shown = pool[:limit]
    header = f"Here are the **{len(shown)} upcoming events** on DEMP:\n\n"
    body = "\n\n".join(f"{i+1}. {_fmt_event(e)}" for i, e in enumerate(shown))
    extra = ""
    if len(pool) > limit:
        extra = f"\n\n_…and {len(pool) - limit} more. Visit the home page to browse all._"
    return header + body + extra


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------
@app.get("/")
def root():
    return {
        "service": "DEMP Chatbot",
        "version": app.version,
        "llm": {"provider": "ollama", "url": OLLAMA_URL, "model": OLLAMA_MODEL},
        "endpoints": {"chat": "POST /chat", "health": "GET /health"},
    }


@app.get("/health")
async def health():
    ollama_ok = False
    models: list[str] = []
    try:
        async with httpx.AsyncClient(timeout=3.0) as client:
            r = await client.get(f"{OLLAMA_URL}/api/tags")
            if r.status_code == 200:
                ollama_ok = True
                models = [m.get("name", "") for m in r.json().get("models", [])]
    except Exception:
        pass
    return {
        "status": "healthy",
        "ollama_reachable": ollama_ok,
        "ollama_models": models,
        "configured_model": OLLAMA_MODEL,
        "kb_intents": len(KB),
    }


@app.post("/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    session_id = req.session_id or str(uuid.uuid4())
    history = SESSIONS.setdefault(session_id, [])
    history.append({"role": "user", "content": req.message})
    # Trim history (keep last MAX_HISTORY * 2 messages = N turns)
    if len(history) > MAX_HISTORY * 2:
        del history[: len(history) - MAX_HISTORY * 2]

    # ------------------------------------------------------------------
    # Conversational "create event" wizard — runs the entire flow inside
    # the chat (instead of just returning the static how-to steps).
    # ------------------------------------------------------------------
    if cef.is_cancel(req.message) and cef.is_active(session_id):
        cef.cancel(session_id)
        reply = "Okay, I cancelled the event creation."
        history.append({"role": "assistant", "content": reply})
        return ChatResponse(reply=reply, session_id=session_id, source="create_event", model=None)

    if cef.is_active(session_id):
        reply = await cef.advance(
            session_id,
            req.message,
            api_url=DEMP_API_URL,
            auth_token=req.auth_token,
            user_id=req.user_id,
        )
        history.append({"role": "assistant", "content": reply})
        return ChatResponse(reply=reply, session_id=session_id, source="create_event", model=None)

    if cef.looks_like_create_intent(req.message):
        reply = await cef.start(session_id, req.message)
        history.append({"role": "assistant", "content": reply})
        return ChatResponse(reply=reply, session_id=session_id, source="create_event", model=None)

    # Live-data tool: if the user is asking about current/available events,
    # query the DEMP backend directly and return the live list.
    if _wants_event_listing(req.message):
        live = await fetch_live_events()
        if live:
            history.append({"role": "assistant", "content": live})
            return ChatResponse(
                reply=live, session_id=session_id, source="live_events", model=None
            )

    llm_reply = await call_ollama(history)
    if llm_reply:
        history.append({"role": "assistant", "content": llm_reply})
        return ChatResponse(
            reply=llm_reply, session_id=session_id, source="llm", model=OLLAMA_MODEL
        )

    # Fallback path
    reply = keyword_fallback(req.message)
    history.append({"role": "assistant", "content": reply})
    return ChatResponse(
        reply=reply, session_id=session_id, source="fallback", model=None
    )


@app.delete("/chat/{session_id}")
def reset_session(session_id: str):
    SESSIONS.pop(session_id, None)
    return {"status": "ok", "session_id": session_id}

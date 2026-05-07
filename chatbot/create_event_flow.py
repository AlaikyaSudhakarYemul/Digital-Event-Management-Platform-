"""
Conversational "create event" wizard for the DEMP chatbot.

When the user says something like *"create event Diwali Bash on 2026-11-01
at 18:00, IN_PERSON, 200 seats, speaker John, venue HYDERABAD"* (or just
*"create an event"*), the bot enters a per-session wizard, parses what it
can from the message, asks for any missing fields one at a time, then
POSTs to the DEMP backend at `/api/events/create` using the JWT forwarded
by the frontend.

State machine kept in memory per `session_id`. Use ``cancel`` / ``abort``
at any time to bail out.
"""
from __future__ import annotations

import re
from typing import Any, Optional

import httpx

# ---------------------------------------------------------------------------
# Step order & prompts
# ---------------------------------------------------------------------------
STEPS = [
    "eventName",
    "description",
    "date",
    "time",
    "eventType",
    "maxAttendees",
    "speaker",
    "venue",
    "confirm",
]

PROMPTS = {
    "eventName":    "What's the **event name**?",
    "description":  "Give a short **description** of the event.",
    "date":         "What's the **date**? Use `YYYY-MM-DD` (e.g. `2026-12-15`).",
    "time":         "What **time** does it start? Use 24-hour `HH:MM` (e.g. `18:30`).",
    "eventType":    "What's the **event type** — `IN_PERSON`, `VIRTUAL`, or `HYBRID`?",
    "maxAttendees": "**Max attendees** (between 10 and 500)?",
    "speaker":      "Who's the **speaker**? (Type the speaker name; I'll match it to the database.)",
    "venue":        "Which **venue / city**? (Type a venue or city; I'll match it to the saved addresses.)",
}

TRIGGER_RE = re.compile(
    r"\b(create|host|organize|organise|add|make|start|setup|publish|list|need|want)\s+"
    r"(?:to\s+)?(?:create\s+)?(an?\s+)?(new\s+)?event\b",
    re.IGNORECASE,
)
CANCEL_RE = re.compile(r"^\s*(cancel|abort|quit|stop|nevermind|never mind)\s*$", re.IGNORECASE)

# ISO date: 2026-04-30
DATE_ISO_RE = re.compile(r"\b(\d{4})-(\d{1,2})-(\d{1,2})\b")
# DD/MM/YYYY or DD-MM-YYYY (also accept YYYY/MM/DD when first group has 4 digits)
DATE_DMY_RE = re.compile(r"\b(\d{1,2})[/.-](\d{1,2})[/.-](\d{2,4})\b")
# "April 30 2026" / "30 April 2026" / "30 Apr 2026"
_MONTHS = {
    "jan": 1, "january": 1, "feb": 2, "february": 2, "mar": 3, "march": 3,
    "apr": 4, "april": 4, "may": 5, "jun": 6, "june": 6, "jul": 7, "july": 7,
    "aug": 8, "august": 8, "sep": 9, "sept": 9, "september": 9,
    "oct": 10, "october": 10, "nov": 11, "november": 11, "dec": 12, "december": 12,
}
DATE_TXT_RE = re.compile(
    r"\b(?:(\d{1,2})\s+([A-Za-z]+)|([A-Za-z]+)\s+(\d{1,2}))[,\s]+(\d{4})\b",
    re.IGNORECASE,
)

# Time:  18:30  |  10:30 pm  |  10pm  |  10 am  |  10 o'clock  |  10 clock
TIME_RE = re.compile(
    r"\b(\d{1,2})(?::(\d{2}))?\s*(am|pm|a\.m\.|p\.m\.|o'?\s*clock|clock)?\b",
    re.IGNORECASE,
)
TYPE_RE = re.compile(r"\b(IN[_ -]?PERSON|INPERSON|VIRTUAL|HYBRID|ONLINE|IN PERSON)\b", re.IGNORECASE)
COUNT_RE = re.compile(
    r"(\d{2,4})\s*(?:seats?|attendees?|capacity|people|max|guests?|persons?)\b"
    r"|\b(?:capacity|max(?:imum)?|seats?|attendees?|with|for)\D{0,6}(\d{2,4})\b",
    re.IGNORECASE,
)


def looks_like_create_intent(message: str) -> bool:
    return bool(TRIGGER_RE.search(message))


def is_cancel(message: str) -> bool:
    return bool(CANCEL_RE.match(message))


# ---------------------------------------------------------------------------
# Per-session state
# ---------------------------------------------------------------------------
# { session_id: {"step": str, "data": {...}} }
WIZARDS: dict[str, dict[str, Any]] = {}


def is_active(session_id: str) -> bool:
    return session_id in WIZARDS


def cancel(session_id: str) -> None:
    WIZARDS.pop(session_id, None)


# ---------------------------------------------------------------------------
# Field parsing from a free-form message
# ---------------------------------------------------------------------------
def _normalize_type(raw: str) -> Optional[str]:
    s = raw.upper().replace(" ", "_").replace("-", "_")
    if "IN_PERSON" in s or s == "INPERSON":
        return "IN_PERSON"
    if s in {"VIRTUAL", "ONLINE"}:
        return "VIRTUAL"
    if s == "HYBRID":
        return "HYBRID"
    return None


def _parse_any_date(text: str) -> Optional[str]:
    """Return an ISO `YYYY-MM-DD` string from many common formats, or None."""
    m = DATE_ISO_RE.search(text)
    if m:
        y, mo, d = int(m.group(1)), int(m.group(2)), int(m.group(3))
        if 1 <= mo <= 12 and 1 <= d <= 31:
            return f"{y:04d}-{mo:02d}-{d:02d}"

    m = DATE_DMY_RE.search(text)
    if m:
        a, b, c = m.group(1), m.group(2), m.group(3)
        # Disambiguate DMY vs YMD: if the first group is 4 digits, it's YMD
        if len(a) == 4:
            y, mo, d = int(a), int(b), int(c)
        else:
            d, mo = int(a), int(b)
            y = int(c)
            if y < 100:
                y += 2000
        if 1 <= mo <= 12 and 1 <= d <= 31:
            return f"{y:04d}-{mo:02d}-{d:02d}"

    m = DATE_TXT_RE.search(text)
    if m:
        day = int(m.group(1) or m.group(4))
        month_word = (m.group(2) or m.group(3) or "").lower()
        year = int(m.group(5))
        mo = _MONTHS.get(month_word)
        if mo and 1 <= day <= 31:
            return f"{year:04d}-{mo:02d}-{day:02d}"
    return None


def _parse_any_time(text: str) -> Optional[str]:
    """Return `HH:MM` (24h) from `18:30`, `10pm`, `10 o'clock`, etc."""
    for m in TIME_RE.finditer(text):
        hh = int(m.group(1))
        mm = int(m.group(2)) if m.group(2) else 0
        suffix = (m.group(3) or "").lower().replace(".", "").replace(" ", "")
        # Skip if this match is actually part of a date like 2026-04-30
        start = m.start()
        if start > 0 and text[start - 1] in {"-", "/"}:
            continue
        if suffix.startswith("pm") and hh < 12:
            hh += 12
        elif suffix.startswith("am") and hh == 12:
            hh = 0
        # "10 o'clock" / "10 clock" — leave as 24h hour, default morning if <12
        if 0 <= hh < 24 and 0 <= mm < 60:
            return f"{hh:02d}:{mm:02d}"
    return None


def parse_initial(message: str) -> dict[str, Any]:
    """Best-effort extraction of fields from the trigger message."""
    data: dict[str, Any] = {}

    d = _parse_any_date(message)
    if d:
        data["date"] = d

    t = _parse_any_time(message)
    if t:
        data["time"] = t

    m = TYPE_RE.search(message)
    if m:
        norm = _normalize_type(m.group(1))
        if norm:
            data["eventType"] = norm

    m = COUNT_RE.search(message)
    if m:
        data["maxAttendees"] = int(m.group(1) or m.group(2))

    # speaker NAME — "speaker John Doe"
    sm = re.search(r"speaker[:\s]+([A-Za-z][A-Za-z .'-]{1,40})", message, re.IGNORECASE)
    if sm:
        data["speaker"] = sm.group(1).strip().rstrip(",.")

    # venue NAME — "venue Hyderabad" / "at HYDERABAD" / "in <city>"
    vm = re.search(
        r"\b(?:venue|location|city|address|at|in)[:\s]+([A-Za-z][A-Za-z .'-]{1,40})",
        message,
        re.IGNORECASE,
    )
    if vm:
        candidate = vm.group(1).strip().rstrip(",.")
        if candidate.lower() not in {"the", "a", "an", "hybrid", "virtual", "person"}:
            data["venue"] = candidate

    # Event NAME — "create event <NAME> of/with/at/on/for ..."
    nm = re.search(
        r"\bevent\s+(?:named?\s+|called\s+)?(?:\"([^\"]+)\"|'([^']+)'|([A-Za-z][A-Za-z0-9 .'-]*?))"
        r"(?=\s+(?:of|with|at|on|for|in|\d|$|,|\.|!))",
        message,
        re.IGNORECASE,
    )
    if nm:
        name = (nm.group(1) or nm.group(2) or nm.group(3) or "").strip()
        # Strip trailing words that look like a type/keyword leak
        name = re.sub(r"\s+(?:of|with|at|on|for|in)$", "", name, flags=re.IGNORECASE).strip()
        if name and name.lower() not in {"a", "an", "the", "new"}:
            data["eventName"] = name.title() if name.islower() else name

    return data


# ---------------------------------------------------------------------------
# Per-step validators — return (ok, value, error_message)
# ---------------------------------------------------------------------------
def _validate(field: str, value: str) -> tuple[bool, Any, str]:
    v = value.strip()
    if field == "eventName":
        if not v:
            return False, None, "Event name can't be empty."
        return True, v, ""
    if field == "description":
        if len(v) < 10:
            return False, None, "Description must be **at least 10 characters** (and at most 100)."
        if len(v) > 100:
            return False, None, "Description must be **at most 100 characters**."
        return True, v, ""
    if field == "date":
        iso = _parse_any_date(v)
        if not iso:
            return False, None, "I couldn't read that date. Try `30/04/2026`, `2026-04-30`, or `April 30 2026`."
        return True, iso, ""
    if field == "time":
        hhmm = _parse_any_time(v)
        if not hhmm:
            return False, None, "I couldn't read that time. Try `18:30`, `10pm`, or `10 o'clock`."
        return True, hhmm, ""
    if field == "eventType":
        norm = _normalize_type(v)
        if not norm:
            return False, None, "Type must be `IN_PERSON`, `VIRTUAL`, or `HYBRID`."
        return True, norm, ""
    if field == "maxAttendees":
        m = re.search(r"\d+", v)
        if not m:
            return False, None, "Give a number between 10 and 500."
        n = int(m.group(0))
        if n < 10 or n > 500:
            return False, None, "Max attendees must be between **10** and **500**."
        return True, n, ""
    if field in ("speaker", "venue"):
        if len(v) < 2:
            return False, None, "Please give a longer name."
        return True, v, ""
    return True, v, ""


# ---------------------------------------------------------------------------
# Backend lookups
# ---------------------------------------------------------------------------
async def _resolve_speaker(api_url: str, name: Optional[str]) -> Optional[dict]:
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            r = await client.get(f"{api_url}/api/speakers")
            if r.status_code != 200:
                return None
            speakers = r.json() or []
    except Exception as exc:  # noqa: BLE001
        print(f"[create_event] speaker fetch failed: {exc}")
        return None
    if not speakers:
        return None
    if not name:
        return speakers[0]
    needle = name.lower().strip()
    for s in speakers:
        sname = (s.get("name") or s.get("speakerName") or "").lower()
        if sname == needle or needle in sname or sname in needle:
            return s
    # No match — fall back to the first available speaker
    return speakers[0]


async def _resolve_address(api_url: str, query: Optional[str]) -> Optional[dict]:
    """Match by city/venue/address substring against /api/admin/all."""
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            r = await client.get(f"{api_url}/api/admin/all")
            if r.status_code != 200:
                return None
            addrs = r.json() or []
    except Exception as exc:  # noqa: BLE001
        print(f"[create_event] address fetch failed: {exc}")
        return None
    if not addrs:
        return None
    if not query:
        return addrs[0]
    needle = query.lower().strip()
    for a in addrs:
        haystack = " ".join(
            str(a.get(k) or "") for k in ("address", "venue", "city", "state", "country")
        ).lower()
        if needle in haystack:
            return a
    # No match — fall back to first saved address
    return addrs[0]


# ---------------------------------------------------------------------------
# Main step handlers
# ---------------------------------------------------------------------------
# Required fields the user MUST supply. Speaker/venue can default to the
# first row in the DB, and description defaults to the event name — so the
# user only has to fight the bot for these.
_REQUIRED = ("eventName", "date", "time", "eventType", "maxAttendees")


def _apply_defaults(data: dict) -> None:
    """Fill optional fields with sensible defaults so we don't ask for them."""
    if not data.get("description") and data.get("eventName"):
        name = str(data["eventName"]).strip()
        etype = str(data.get("eventType") or "event").replace("_", " ").lower()
        # Backend requires description length 10–100
        desc = f"{name} — a {etype} event hosted on DEMP."
        if len(desc) > 100:
            desc = desc[:100]
        data["description"] = desc


def _next_missing_step(data: dict) -> str:
    _apply_defaults(data)
    for s in _REQUIRED:
        if data.get(s) in (None, ""):
            return s
    return "submit"


def _summary(data: dict) -> str:
    rows = [
        f"- **Name:** {data.get('eventName')}",
        f"- **Description:** {data.get('description')}",
        f"- **When:** {data.get('date')} at {data.get('time')}",
        f"- **Type:** {data.get('eventType')}",
        f"- **Capacity:** {data.get('maxAttendees')}",
        f"- **Speaker:** {data.get('speaker')}",
    ]
    if data.get("eventType") != "VIRTUAL":
        rows.append(f"- **Venue:** {data.get('venue')}")
    return "\n".join(rows)


async def start(session_id: str, message: str) -> str:
    """Begin or continue the wizard from the trigger message."""
    parsed = parse_initial(message)
    WIZARDS[session_id] = {"step": "", "data": parsed}
    return await advance(session_id, "")


async def advance(
    session_id: str,
    message: str,
    *,
    api_url: str = "http://localhost:8080",
    auth_token: Optional[str] = None,
    user_id: Optional[int] = None,
) -> str:
    """Process a user message inside the wizard. Returns the next bot reply."""
    state = WIZARDS.get(session_id)
    if state is None:
        return "Hmm, I lost the thread. Type *create event* to start again."

    data = state["data"]
    current = state.get("step") or ""

    # Apply the answer to the current step (if any)
    if current and current != "submit" and message.strip():
        ok, value, err = _validate(current, message)
        if not ok:
            return f"⚠️ {err}\n\n{PROMPTS[current]}"
        data[current] = value

    # Pick the next missing field; if none missing, submit immediately.
    nxt = _next_missing_step(data)
    state["step"] = nxt

    if nxt == "submit":
        return await _submit(
            session_id,
            data,
            api_url=api_url,
            auth_token=auth_token,
            user_id=user_id,
        )

    return PROMPTS[nxt]


# ---------------------------------------------------------------------------
# Submission
# ---------------------------------------------------------------------------
async def _submit(
    session_id: str,
    data: dict,
    *,
    api_url: str,
    auth_token: Optional[str],
    user_id: Optional[int],
) -> str:
    if not user_id:
        cancel(session_id)
        return (
            "⚠️ I can't create the event because I don't know which account "
            "to use. Please **log in** in the website first, then ask me again."
        )

    # Resolve speaker (auto-pick first available if none given / no match)
    speaker = await _resolve_speaker(api_url, data.get("speaker"))
    if not speaker:
        cancel(session_id)
        return (
            "⚠️ No speakers exist in the database yet. Add at least one speaker, "
            "then ask me to create the event again."
        )

    # Resolve address (skip if VIRTUAL)
    address = None
    if data["eventType"] != "VIRTUAL":
        address = await _resolve_address(api_url, data.get("venue"))
        if not address:
            cancel(session_id)
            return (
                "⚠️ No saved addresses to use as a venue. Add one in the admin "
                "section, then ask me to create the event again."
            )

    payload = {
        "eventName": data["eventName"],
        "description": data["description"],
        "date": data["date"],
        "time": data["time"],
        "eventType": data["eventType"],
        "maxAttendees": data["maxAttendees"],
        "speakers": [{"speakerId": speaker.get("speakerId")}],
        "address": address,
        "image": None,
        "user": {"userId": user_id},
    }

    headers = {"Content-Type": "application/json"}
    if auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"

    try:
        async with httpx.AsyncClient(timeout=15.0) as client:
            r = await client.post(
                f"{api_url}/api/events/create", json=payload, headers=headers
            )
    except Exception as exc:  # noqa: BLE001
        cancel(session_id)
        return f"⚠️ Couldn't reach the backend at `{api_url}`: `{exc}`"

    cancel(session_id)

    if r.status_code in (200, 201):
        try:
            ev = r.json() or {}
            ev_id = ev.get("eventId") or ev.get("id")
        except Exception:  # noqa: BLE001
            ev_id = None
        suffix = f" (ID `{ev_id}`)" if ev_id else ""
        return (
            f"✅ **Event created successfully!**{suffix}\n\n"
            + _summary(data)
            + "\n\nYou can see it on the home page under *Upcoming Events*."
        )

    body = r.text[:300] if r.text else r.reason_phrase
    return (
        f"⚠️ The backend rejected the request (HTTP {r.status_code}).\n\n"
        f"```\n{body}\n```"
    )

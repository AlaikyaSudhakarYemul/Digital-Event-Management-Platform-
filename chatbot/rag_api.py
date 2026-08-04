
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import uvicorn
import re

# Import your classes and functions from your notebook/module
from rag_embedding import rag_retriever, ollama_rag_response


app = FastAPI()

# Enable CORS for all origins (for development)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# FAQ Cache — returns instant answers without calling the LLM
# ---------------------------------------------------------------------------
FAQ_CACHE = {
    "what is demp": (
        "**DEMP** (Digital Event Management Platform) is a full-stack microservices application "
        "that manages the complete event lifecycle — from user registration and event creation to "
        "ticketing, payment processing, notifications, and analytics. "
        "It supports three roles: **Admin**, **Organizer**, and **User**."
    ),
    "how do i register": (
        "To register on DEMP:\n"
        "1. Go to the **Sign Up** page.\n"
        "2. Enter your name, email, password, contact number, and select your role (User/Organizer).\n"
        "3. Click **Register** — you are logged in automatically."
    ),
    "how do i sign up": (
        "To sign up:\n"
        "1. Click **Sign Up** on the homepage.\n"
        "2. Fill in your name, email, password, contact number, and role.\n"
        "3. Submit the form to create your account."
    ),
    "how do i login": (
        "To log in:\n"
        "1. Click **Login** on the homepage.\n"
        "2. Enter your registered email and password.\n"
        "3. Click **Login** — you will be redirected to your role-based dashboard."
    ),
    "how do i book a ticket": (
        "To book a ticket:\n"
        "1. Browse **Events** and select one.\n"
        "2. Click **Book Ticket** on the event detail page.\n"
        "3. Confirm your booking and proceed to **Payment**.\n"
        "4. After successful payment, your ticket appears in **My Tickets**."
    ),
    "how do i create an event": (
        "Creating an event requires the **Organizer** role:\n"
        "1. Log in as an Organizer.\n"
        "2. Go to **Organizer Dashboard → Create Event**.\n"
        "3. Fill in title, date, venue, capacity, and ticket price.\n"
        "4. Click **Publish** to make it visible to users."
    ),
    "how do i make a payment": (
        "Payments happen during ticket booking:\n"
        "1. After selecting a ticket, click **Proceed to Payment**.\n"
        "2. Enter your payment details.\n"
        "3. On success, you receive a booking confirmation and ticket.\n"
        "The platform uses a secure payment processing service."
    ),
    "what roles are available": (
        "DEMP supports three roles:\n"
        "- **Admin**: Full platform control — manage users, events, view reports.\n"
        "- **Organizer**: Create and manage events, track registrations and revenue.\n"
        "- **User**: Browse events, book tickets, make payments, view ticket history."
    ),
    "how do i view my tickets": (
        "To view your tickets:\n"
        "1. Log in as a **User**.\n"
        "2. Go to **My Tickets** in your profile or dashboard.\n"
        "3. All confirmed bookings and tickets are listed there with event details."
    ),
    "what is the tech stack": (
        "DEMP is built with:\n"
        "- **Frontend**: React\n"
        "- **Backend**: Java Spring Boot Microservices\n"
        "- **Database**: MySQL\n"
        "- **Service Discovery**: Eureka Server (port 8761)\n"
        "- **API Gateway**: Spring Cloud Gateway (port 8080)\n"
        "- **AI Chatbot**: Python FastAPI with RAG + ChromaDB + SentenceTransformer"
    ),
    "what microservices are there": (
        "DEMP consists of 10 microservices:\n"
        "- **api-gateway** (port 8080) — routing & CORS\n"
        "- **eureka-server** (port 8761) — service discovery\n"
        "- **user-service** (port 8084) — auth & profiles\n"
        "- **admin-service** (port 8083) — admin operations\n"
        "- **event-service** — event management\n"
        "- **registration-service** — bookings\n"
        "- **tickets-service** — ticket generation\n"
        "- **payment-service** — transactions\n"
        "- **notification-service** — alerts\n"
        "- **organizer-service** — organizer workflows"
    ),
    "how does the chatbot work": (
        "The DEMP chatbot (**EventMate**) uses **RAG** (Retrieval-Augmented Generation):\n"
        "1. Your question is embedded as a vector using SentenceTransformer.\n"
        "2. Relevant DEMP document chunks are retrieved from **ChromaDB**.\n"
        "3. The context is sent to an **Ollama LLM** (qwen3.5:2b) to generate an answer.\n"
        "For common questions, a **cache layer** returns instant answers without calling the LLM."
    ),
    "how do i cancel a registration": (
        "To cancel a registration:\n"
        "1. Log in and go to **My Registrations**.\n"
        "2. Find the registration you want to cancel.\n"
        "3. Click **Cancel Registration**.\n"
        "Cancellation policies depend on the event organizer's settings."
    ),
    "how do i contact support": (
        "For support on DEMP:\n"
        "- Use the **EventMate chatbot** for instant help with common questions.\n"
        "- Contact your platform **Admin** for account or access issues.\n"
        "- Reach the **Organizer** directly for event-specific queries."
    ),
}


def _normalize(text: str) -> str:
    """Lowercase and strip punctuation for fuzzy FAQ matching."""
    return re.sub(r"[^\w\s]", "", text.lower()).strip()


def get_faq_answer(question: str) -> Optional[str]:
    """Return a cached FAQ answer if the question matches any key, else None."""
    q = _normalize(question)
    for key, answer in FAQ_CACHE.items():
        if _normalize(key) in q or q in _normalize(key):
            return answer
    return None


# ---------------------------------------------------------------------------
# Endpoint: return all FAQ question labels for the frontend suggestion list
# ---------------------------------------------------------------------------
@app.get("/faq")
def get_faq_list():
    return {"faqs": list(FAQ_CACHE.keys())}


class QueryRequest(BaseModel):
    question: str
    model_name: Optional[str] = "qwen3.5:2b"
    top_k: Optional[int] = 5


@app.post("/ask")
def ask_question(request: QueryRequest):
    try:
        # 1. Check FAQ cache first — instant response, no LLM call needed
        cached = get_faq_answer(request.question)
        if cached:
            return {"answer": cached, "source": "cache"}

        # 2. Fall back to full RAG + LLM pipeline
        answer = ollama_rag_response(
            request.question,
            rag_retriever,
            model_name=request.model_name,
            top_k=request.top_k,
        )
        return {"answer": answer, "source": "llm"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    uvicorn.run("rag_api:app", host="0.0.0.0", port=8000, reload=True)

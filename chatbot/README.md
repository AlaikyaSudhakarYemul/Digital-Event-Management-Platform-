# DEMP Chatbot — Powered by an Open-Source LLM (Ollama)

A FastAPI chatbot that answers **any** question — both questions about the
Digital Event Management Platform (events, tickets, payments, dashboards,
architecture, etc.) and general questions — using a local open-source LLM
through [Ollama](https://ollama.com).

The project knowledge base in `knowledge_base.py` is injected as the system
prompt, so the model is *project-aware*. If Ollama is not running, the
service automatically falls back to keyword matching against the same KB.

## 1. Install Ollama (one-time)

Download from https://ollama.com/download and install. It runs as a
background service on `http://localhost:11434`.

Pull a small open-source model (pick one):

```powershell
ollama pull llama3.2          # ~2 GB — recommended default
ollama pull phi3:mini         # ~2.3 GB — fast
ollama pull qwen2.5:3b        # ~2 GB
ollama pull mistral           # ~4 GB — higher quality
```

Verify:

```powershell
ollama list
```

## 2. Install Python deps

```powershell
cd chatbot
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

## 3. Run the chatbot service

```powershell
uvicorn app:app --reload --port 8001
```

Or just double-click `start.bat`.

The React widget on the home page calls `POST http://localhost:8001/chat`.

## Configuration (env vars)

| Variable       | Default                  | Purpose                          |
| -------------- | ------------------------ | -------------------------------- |
| `OLLAMA_URL`   | `http://localhost:11434` | Ollama server URL                |
| `OLLAMA_MODEL` | `llama3.2`               | Which pulled model to use        |

Example:

```powershell
$env:OLLAMA_MODEL = "phi3:mini"
uvicorn app:app --reload --port 8001
```

## Endpoints

- `GET  /`                 service info & current model
- `GET  /health`           shows whether Ollama is reachable + installed models
- `POST /chat`             body: `{ "message": "...", "session_id": "optional" }`
- `DELETE /chat/{id}`      reset a conversation

## How project knowledge is added

`knowledge_base.py` contains a `KB` list of intents (create event, book
ticket, payments, dashboards, tech stack, run instructions, etc.). At
startup, `app.py` flattens this into a system prompt for the LLM, so
project answers stay grounded. Add new entries to `KB` to teach the bot
more — no retraining needed.

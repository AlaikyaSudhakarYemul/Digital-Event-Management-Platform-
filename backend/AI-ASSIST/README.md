# AI-ASSIST Service

This service converts natural language prompts into structured event creation commands and orchestrates calls to existing microservices.

## Run

1. Create virtual environment
2. Install dependencies
3. Run uvicorn

```powershell
cd backend/AI-ASSIST
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8090 --reload
```

## Endpoint

- POST /api/ai/execute

## Example Request

```json
{
  "prompt": "Create a technical event with 2 speakers, online mode, price 500",
  "context": {
    "userId": 12,
    "defaultDate": "2026-05-10",
    "timezone": "Asia/Kolkata"
  }
}
```

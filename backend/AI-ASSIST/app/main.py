import os
from typing import Any, Dict

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from .models import AIExecuteRequest, AIExecuteResponse
from .orchestrator import decode_role_from_auth_header, execute_create_event, parse_prompt


load_dotenv()

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
ALLOWED_ORIGIN = os.getenv("ALLOWED_ORIGIN", "http://localhost:3000")

app = FastAPI(title="AI Assist Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[ALLOWED_ORIGIN],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health() -> Dict[str, str]:
    return {"status": "up"}


@app.post("/api/ai/execute", response_model=AIExecuteResponse)
async def execute_prompt(
    body: AIExecuteRequest,
    authorization: str | None = Header(default=None),
) -> AIExecuteResponse:
    role, email = decode_role_from_auth_header(authorization)
    if role is None:
        raise HTTPException(status_code=401, detail="Missing or invalid token")

    if str(role).upper() != "ORGANIZER":
        raise HTTPException(status_code=403, detail="Only organizer can use AI create workflow")

    command = parse_prompt(body.prompt, body.context)

    if command.intent != "create_event":
        return AIExecuteResponse(
            status="unsupported",
            message="Only create_event prompt is supported right now",
            command=command,
            missingFields=command.missingFields,
            unsupportedFields=command.unsupportedFields,
        )

    if command.missingFields:
        return AIExecuteResponse(
            status="needs_input",
            message="Missing required information to create event",
            command=command,
            missingFields=command.missingFields,
            unsupportedFields=command.unsupportedFields,
        )

    headers: Dict[str, Any] = {}
    if authorization:
        headers["Authorization"] = authorization

    try:
        created = await execute_create_event(API_BASE_URL, command, headers)
    except Exception as ex:
        raise HTTPException(status_code=502, detail=f"Failed to orchestrate event creation: {ex}") from ex

    return AIExecuteResponse(
        status="created",
        message="Event created successfully",
        command=command,
        eventId=created.get("eventId"),
        summary={
            "eventName": created.get("eventName"),
            "eventType": created.get("eventType"),
            "user": email,
            "speakerIds": created.get("speakerIds", []),
        },
        unsupportedFields=command.unsupportedFields,
    )

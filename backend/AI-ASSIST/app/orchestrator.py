import re
from typing import Any, Dict, List, Tuple

import httpx
import jwt

from .models import ParsedCommand, ParsedEventData


def _extract_number(prompt: str, pattern: str, default: int | None = None) -> int | None:
    match = re.search(pattern, prompt, re.IGNORECASE)
    if not match:
        return default
    try:
        return int(match.group(1))
    except ValueError:
        return default


def _extract_mode(prompt: str) -> str | None:
    p = prompt.lower()
    if "online" in p or "virtual" in p:
        return "VIRTUAL"
    if "hybrid" in p:
        return "HYBRID"
    if "offline" in p or "in-person" in p or "in person" in p:
        return "OFFLINE"
    return None


def parse_prompt(prompt: str, context: Dict[str, Any]) -> ParsedCommand:
    lower_prompt = prompt.lower()

    intent = "create_event" if "create" in lower_prompt and "event" in lower_prompt else "unknown"

    event_name = "AI Generated Event"
    quoted_name = re.search(r'"([^"]+)"', prompt)
    if quoted_name:
        event_name = quoted_name.group(1)
    elif "technical" in lower_prompt:
        event_name = "Technical Event"

    speaker_count = _extract_number(lower_prompt, r"(\d+)\s+speakers?", default=0)
    price = _extract_number(lower_prompt, r"(?:price|rs|₹)\s*(\d+)", default=None)
    max_attendees = _extract_number(lower_prompt, r"(?:max|capacity|attendees?)\s*(\d+)", default=100)

    event_type = _extract_mode(lower_prompt) or "VIRTUAL"
    address_id = context.get("addressId")

    user_id = context.get("userId")
    default_date = context.get("defaultDate")
    default_time = context.get("defaultTime", "18:00:00")

    missing_fields: List[str] = []
    if user_id is None:
        missing_fields.append("userId")

    if event_type in {"OFFLINE", "HYBRID"} and not address_id:
        missing_fields.append("addressId")

    unsupported_fields: List[str] = []
    if price is not None:
        unsupported_fields.append("price")

    data = ParsedEventData(
        eventName=event_name,
        description=f"Created by AI assistant from prompt: {prompt}",
        eventType=event_type,
        date=default_date,
        time=default_time,
        addressId=address_id,
        speakerCount=speaker_count,
        maxAttendees=max_attendees or 100,
        userId=user_id,
        price=price,
    )

    return ParsedCommand(
        intent=intent,
        confidence=0.9 if intent == "create_event" else 0.4,
        data=data,
        missingFields=missing_fields,
        unsupportedFields=unsupported_fields,
    )


def decode_role_from_auth_header(auth_header: str | None) -> Tuple[str | None, str | None]:
    if not auth_header or not auth_header.startswith("Bearer "):
        return None, None
    token = auth_header.replace("Bearer ", "", 1).strip()
    try:
        payload = jwt.decode(token, options={"verify_signature": False})
        role = payload.get("role")
        email = payload.get("sub") or payload.get("email")
        return role, email
    except Exception:
        return None, None


async def _choose_speakers(api_base: str, speaker_count: int, headers: Dict[str, str]) -> List[int]:
    if speaker_count <= 0:
        return []
    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.get(f"{api_base}/api/speakers", headers=headers)
        response.raise_for_status()
        speakers = response.json() or []
        return [s.get("speakerId") for s in speakers[:speaker_count] if s.get("speakerId") is not None]


async def execute_create_event(api_base: str, command: ParsedCommand, headers: Dict[str, str]) -> Dict[str, Any]:
    data = command.data

    speaker_ids = await _choose_speakers(api_base, data.speakerCount or 0, headers)
    payload: Dict[str, Any] = {
        "eventName": data.eventName,
        "description": data.description,
        "eventType": data.eventType,
        "date": data.date,
        "time": data.time,
        "image": data.image,
        "addressId": data.addressId,
        "speakerIds": speaker_ids,
        "userId": data.userId,
        "maxAttendees": data.maxAttendees,
    }

    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.post(f"{api_base}/api/events/create", json=payload, headers=headers)
        response.raise_for_status()
        return response.json()

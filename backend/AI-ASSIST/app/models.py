from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class AIExecuteRequest(BaseModel):
    prompt: str = Field(min_length=3)
    context: Dict[str, Any] = Field(default_factory=dict)


class ParsedEventData(BaseModel):
    eventName: Optional[str] = None
    description: Optional[str] = None
    eventType: Optional[str] = None
    date: Optional[str] = None
    time: Optional[str] = None
    image: Optional[str] = None
    addressId: Optional[int] = None
    speakerCount: Optional[int] = None
    speakerIds: List[int] = Field(default_factory=list)
    maxAttendees: int = 100
    userId: Optional[int] = None
    price: Optional[int] = None


class ParsedCommand(BaseModel):
    intent: str
    confidence: float
    data: ParsedEventData
    missingFields: List[str] = Field(default_factory=list)
    unsupportedFields: List[str] = Field(default_factory=list)


class AIExecuteResponse(BaseModel):
    status: str
    message: str
    command: Optional[ParsedCommand] = None
    eventId: Optional[int] = None
    summary: Optional[Dict[str, Any]] = None
    missingFields: List[str] = Field(default_factory=list)
    unsupportedFields: List[str] = Field(default_factory=list)

import requests

# Backend service base (Spring Boot DEMP service)
ORGANIZER_SERVICE_BASE_URL = "http://localhost:8080/api"


def organizer_sign_in(email, password):
    """Authenticate organizer and return login response.

    Request:
      POST /api/auth/login
      {
        "email": "organizer@example.com",
        "password": "secret"
      }

    Response:
      {
        "user": { ... },
        "token": "<jwt>"
      }
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/auth/login"
    payload = {"email": email, "password": password}
    response = requests.post(url, json=payload)
    response.raise_for_status()
    return response.json()


def _auth_headers(token):
    return {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
    }


def get_events_by_organizer(token, user_id):
    """Get all events created by one organizer.

    Endpoint:
      GET /api/events/organizer/{userId}

    Response:
      [ Event, Event, ... ]
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/organizer/{user_id}"
    response = requests.get(url, headers=_auth_headers(token))
    response.raise_for_status()
    return response.json()


def get_event_by_id(token, event_id):
    """Fetch one event by id.

    Endpoint:
      GET /api/events/{id}

    Response:
      Event
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/{event_id}"
    response = requests.get(url, headers=_auth_headers(token))
    response.raise_for_status()
    return response.json()


def create_event(token, user_id, event_name, description, date, time, event_type,
                 max_attendees, address_id=None, speaker_ids=None,
                 image=None, active_status=None):
    """Create a new organizer event.

    Endpoint:
      POST /api/events/create

    Request body (from Event entity):
      {
        "eventName": str,
        "description": str,
        "date": "YYYY-MM-DD",
        "time": "HH:mm:ss",
        "image": str | null,
        "eventType": "IN_PERSON" | "VIRTUAL" | "HYBRID",
        "address": { "addressId": int } | null,
        "speakers": [ { "speakerId": int }, ... ] | null,
        "user": { "userId": int },
        "activeStatus": "ACTIVE" | "INACTIVE" | "CANCELLED" | "COMPLETED" | "POSTPONED" | null,
        "maxAttendees": int
      }

    Response:
      Event (created)
    """
    payload = {
        "eventName": event_name,
        "description": description,
        "date": date,
        "time": time,
        "image": image,
        "eventType": event_type,
        "user": {"userId": user_id},
        "activeStatus": active_status,
        "maxAttendees": max_attendees,
    }

    if address_id is not None:
        payload["address"] = {"addressId": address_id}

    if speaker_ids:
        payload["speakers"] = [{"speakerId": sid} for sid in speaker_ids]

    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/create"
    response = requests.post(url, json=payload, headers=_auth_headers(token))
    response.raise_for_status()
    return response.json()


def update_event(token, event_id, updated_event_data):
    """Update an event.

    Endpoint:
      PUT /api/events/{id}

    Request body:
      Same structure as create_event payload.

    Response:
      Event (updated)
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/{event_id}"
    response = requests.put(url, json=updated_event_data, headers=_auth_headers(token))
    response.raise_for_status()
    return response.json()


def delete_event(token, event_id):
    """Delete an event.

    Endpoint:
      DELETE /api/events/{id}

    Response:
      "Event deleted successfully"
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/{event_id}"
    response = requests.delete(url, headers=_auth_headers(token))
    response.raise_for_status()
    return response.text


def search_events(token, event_name):
    """Search events by event name.

    Endpoint:
      GET /api/events/search?eventName=...

    Response:
      [ Event, ... ]
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/search"
    response = requests.get(
        url,
        params={"eventName": event_name},
        headers=_auth_headers(token),
    )
    response.raise_for_status()
    return response.json()


def get_paginated_events(token, page=0, size=3, event_name=None):
    """Get paginated events (optionally filtered by event name).

    Endpoint:
      GET /api/events/paginated?page=0&size=3&eventName=optional

    Response:
      Spring Page<Event> JSON.
    """
    params = {"page": page, "size": size}
    if event_name:
        params["eventName"] = event_name

    url = f"{ORGANIZER_SERVICE_BASE_URL}/events/paginated"
    response = requests.get(url, params=params, headers=_auth_headers(token))
    response.raise_for_status()
    return response.json()


def get_registrations_by_event(token, event_id):
    """View registrations of an organizer's event.

    Endpoint:
      GET /api/registrations/event/{eventId}

    Response:
      [ Registration, ... ]
    """
    url = f"{ORGANIZER_SERVICE_BASE_URL}/registrations/event/{event_id}"
    response = requests.get(url, headers=_auth_headers(token))
    response.raise_for_status()
    return response.json()

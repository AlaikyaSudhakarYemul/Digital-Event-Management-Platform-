"""
Conversational state machine for user-service flows (sign in, view profile, update profile, view events, register event).
"""
from services.user_service import user_sign_in, get_user_profile, update_user_profile, view_events, register_event
from services.intent_recognizer import recognize_user_intent

USER_SERVICE_STATE = {}

def reset_user_service_state(session_id):
    USER_SERVICE_STATE.pop(session_id, None)

async def handle_user_service(req, session_id):
    state = USER_SERVICE_STATE.setdefault(session_id, {})
    intent = state.get("intent") or recognize_user_intent(req.message)
    state["intent"] = intent
    # --- SIGN IN ---
    if intent == "sign_in":
        if "email" not in state:
            state["step"] = "ask_email"
            return "Please enter your email to sign in."
        if "password" not in state:
            state["step"] = "ask_password"
            return "Please enter your password."
        try:
            token = user_sign_in(state["email"], state["password"])
            reset_user_service_state(session_id)
            return f"Sign in successful! Your JWT token: {token}"
        except Exception as e:
            reset_user_service_state(session_id)
            return f"Sign in failed: {e}"
    elif intent == "view_profile":
        token = req.auth_token or state.get("token")
        if not token:
            state["step"] = "ask_token"
            return "Please provide your JWT token to view your profile."
        try:
            profile = get_user_profile(token)
            reset_user_service_state(session_id)
            return f"Your profile info:\n{profile}"
        except Exception as e:
            reset_user_service_state(session_id)
            return f"Could not fetch profile: {e}"
    elif intent == "update_profile":
        token = req.auth_token or state.get("token")
        if not token:
            state["step"] = "ask_token"
            return "Please provide your JWT token to update your profile."
        if "field" not in state:
            state["step"] = "ask_field"
            return "Which detail would you like to update? (e.g., name, email, phone, etc.)"
        if "value" not in state:
            state["step"] = "ask_value"
            return f"Please enter the new value for your {state['field']}."
        try:
            profile_data = {state["field"]: state["value"]}
            result = update_user_profile(token, profile_data)
            reset_user_service_state(session_id)
            return f"Profile updated successfully!\n{result}"
        except Exception as e:
            reset_user_service_state(session_id)
            return f"Profile update failed: {e}"
    elif intent == "view_events":
        token = req.auth_token or state.get("token")
        if not token:
            state["step"] = "ask_token"
            return "Please provide your JWT token to view events."
        try:
            events = view_events(token)
            reset_user_service_state(session_id)
            return f"Here are your events:\n{events}"
        except Exception as e:
            reset_user_service_state(session_id)
            return f"Could not fetch events: {e}"
    elif intent == "register_event":
        token = req.auth_token or state.get("token")
        if not token:
            state["step"] = "ask_token"
            return "Please provide your JWT token to register for an event."
        if "event_id" not in state:
            state["step"] = "ask_event_id"
            return "Please enter the event ID you want to register for."
        try:
            result = register_event(token, state["event_id"])
            reset_user_service_state(session_id)
            return f"Successfully registered for event!\n{result}"
        except Exception as e:
            reset_user_service_state(session_id)
            return f"Event registration failed: {e}"
    else:
        reset_user_service_state(session_id)
        return None

def update_user_service_state(session_id, message):
    state = USER_SERVICE_STATE.setdefault(session_id, {})
    step = state.get("step")
    if step == "ask_email":
        state["email"] = message.strip()
    elif step == "ask_password":
        state["password"] = message.strip()
    elif step == "ask_token":
        state["token"] = message.strip()
    elif step == "ask_field":
        state["field"] = message.strip()
    elif step == "ask_value":
        state["value"] = message.strip()
    elif step == "ask_event_id":
        state["event_id"] = message.strip()

def recognize_user_intent(user_message):
    """
    Simple intent recognition for user-service actions.
    Returns: intent string (e.g., 'sign_in', 'view_profile', 'update_profile', 'view_events', 'register_event')
    """
    msg = user_message.lower()
    if any(word in msg for word in ["sign in", "login", "log in", "authenticate"]):
        return "sign_in"
    if any(word in msg for word in ["profile", "my info", "account details"]):
        if "update" in msg or "edit" in msg or "change" in msg:
            return "update_profile"
        return "view_profile"
    if any(word in msg for word in ["show events", "list events", "view events", "all events"]):
        return "view_events"
    if any(word in msg for word in ["register", "sign up for event", "join event"]):
        return "register_event"
    return "unknown"

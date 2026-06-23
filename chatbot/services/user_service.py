import requests

USER_SERVICE_BASE_URL = "http://localhost:8000/api"

def user_sign_in(email, password):
    """Authenticate user and return JWT token."""
    url = f"{USER_SERVICE_BASE_URL}/auth/login"
    payload = {"email": email, "password": password}
    response = requests.post(url, json=payload)
    response.raise_for_status()
    return response.json().get("token")

def get_user_profile(token):
    url = f"{USER_SERVICE_BASE_URL}/users/profile"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()

def update_user_profile(token, profile_data):
    url = f"{USER_SERVICE_BASE_URL}/users/profile"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.put(url, json=profile_data, headers=headers)
    response.raise_for_status()
    return response.json()

def view_events(token):
    url = f"{USER_SERVICE_BASE_URL}/events"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()

def register_event(token, event_id):
    url = f"{USER_SERVICE_BASE_URL}/events/{event_id}/register"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {}
    response = requests.post(url, json=payload, headers=headers)
    response.raise_for_status()
    return response.json()

const API_URL = 'http://localhost:8080/api/events';
const API_BASE = 'http://localhost:8080/api';

export const deleteEvent = async (eventId, token) => {
  const response = await fetch(`${API_URL}/${eventId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'Failed to delete event');
  }
  return await response.text();
};

export const registerForEvent = async (eventId, user) => {
  const token = localStorage.getItem('auth_token') || localStorage.getItem('authToken') || localStorage.getItem('token');
  if (!token) {
    throw new Error('User not authenticated');
  }
  const userId = user?.userId || user?.id;
  if (!userId) {
    throw new Error('Please log in again before registering.');
  }

  let response;
  try {
    response = await fetch('http://localhost:8080/api/registrations', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        userId: userId,
        eventId: eventId
      }),
    });
  } catch {
    throw new Error('Unable to reach registration service. Please refresh and try again.');
  }
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || (response.status === 409 ? 'You are already registered for this event.' : 'Failed to register for event'));
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return await response.json();
  }

  const text = await response.text();
  let parsed = null;
  try {
    parsed = JSON.parse(text);
  } catch (e) {
    parsed = null;
  }

  if (parsed) {
    return parsed;
  }

  const idMatch = text.match(/\d+/);
  return {
    registrationId: idMatch ? Number(idMatch[0]) : null,
    message: text,
  };
};

export const fetchSpeakers = async () => {
  const response = await fetch(`${API_BASE}/speakers`);
  if (!response.ok) {
    throw new Error('Failed to load speakers');
  }
  return response.json();
};

export const fetchAddresses = async () => {
  const response = await fetch(`${API_BASE}/admin/all`);
  if (!response.ok) {
    throw new Error('Failed to load addresses');
  }
  return response.json();
};

export const createEventForOrganizer = async ({ eventData, token, userId }) => {
  const payload = {
    ...eventData,
    user: { userId },
  };

  const response = await fetch(`${API_URL}/create`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || 'Failed to create event');
  }

  return response.json();
};

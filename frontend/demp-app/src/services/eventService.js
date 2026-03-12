const API_URL = 'http://localhost:8080/api/events';

const readJsonSafely = async (response) => {
  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) {
    return null;
  }
  return response.json();
};

export const fetchCopiedEventDetails = async (eventId) => {
  const token = localStorage.getItem('auth_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  // Prefer dedicated copy API; fallback to existing event details API.
  const copyResponse = await fetch(`http://localhost:8080/api/copied/events/${eventId}`, { headers });
  if (copyResponse.ok) {
    return readJsonSafely(copyResponse);
  }

  const eventResponse = await fetch(`${API_URL}/${eventId}`, { headers });
  if (!eventResponse.ok) {
    const error = await eventResponse.text();
    throw new Error(error || 'Failed to fetch event details for copy');
  }

  return readJsonSafely(eventResponse);
};

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
  const token = localStorage.getItem('auth_token');
  if (!token) {
    throw new Error('User not authenticated');
  }
  const response = await fetch('http://localhost:8080/api/registrations', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      user: { userId: user.userId },
      event: { eventId: eventId }
    }),
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'Failed to register for event');
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

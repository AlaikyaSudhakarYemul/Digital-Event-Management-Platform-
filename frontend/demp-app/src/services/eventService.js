const API_URL = 'http://localhost:8080/api/events';

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
  return await response.text();
};

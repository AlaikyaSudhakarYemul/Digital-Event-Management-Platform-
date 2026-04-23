const API_BASE = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8080';

export const sendChatbotMessage = async (message, userId = null) => {
  const response = await fetch(`${API_BASE}/api/chatbot/message`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      message,
      userId,
    }),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Chatbot API failed with ${response.status}`);
  }

  return response.json();
};

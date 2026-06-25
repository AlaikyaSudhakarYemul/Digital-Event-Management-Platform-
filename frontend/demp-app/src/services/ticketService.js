const EVENT_API_BASE = process.env.REACT_APP_EVENT_API_BASE || "http://localhost:8082/api/events";
const TICKET_API_BASE = process.env.REACT_APP_TICKET_API_BASE || "http://localhost:8083/api";

function parseResponseBody(text) {
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  const raw = await response.text();
  const body = parseResponseBody(raw);

  if (!response.ok) {
    const message =
      (body && typeof body === "object" && (body.message || body.error)) ||
      (typeof body === "string" && body) ||
      `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return body;
}

function getStoredUserState() {
  try {
    return JSON.parse(localStorage.getItem("user") || "null");
  } catch {
    return null;
  }
}

export function getAuthToken() {
  const directToken = localStorage.getItem("auth_token");
  if (directToken) {
    return directToken;
  }

  const userState = getStoredUserState();
  if (userState?.token) {
    return userState.token;
  }

  return null;
}

export function getCurrentUserProfile() {
  const explicitProfile = localStorage.getItem("user_profile");
  if (explicitProfile) {
    try {
      return JSON.parse(explicitProfile);
    } catch {
      return null;
    }
  }

  const userState = getStoredUserState();
  if (!userState) {
    return null;
  }

  if (userState.user && typeof userState.user === "object") {
    return userState.user;
  }

  return userState;
}

export function getCurrentUserId() {
  const profile = getCurrentUserProfile();
  if (!profile) {
    return null;
  }

  const candidate = profile.userId ?? profile.id;
  if (candidate == null) {
    return null;
  }

  const numeric = Number(candidate);
  return Number.isFinite(numeric) && numeric > 0 ? numeric : null;
}

export async function getAllEvents() {
  const events = await request(`${EVENT_API_BASE}/all`, {
    method: "GET",
  });

  return Array.isArray(events) ? events : [];
}

export async function bookTicket(payload) {
  const token = getAuthToken();
  return request(`${TICKET_API_BASE}/tickets/book`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
  });
}

export async function getTicketsByUserId(userId) {
  return request(`${TICKET_API_BASE}/tickets/user/${userId}`, {
    method: "GET",
  });
}

export async function cancelTicket(ticketId) {
  return request(`${TICKET_API_BASE}/tickets/${ticketId}/cancel`, {
    method: "PUT",
  });
}

export async function processPayment(payload) {
  return request(`${TICKET_API_BASE}/payments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });
}

export async function getPaymentByTicketId(ticketId) {
  return request(`${TICKET_API_BASE}/payments/ticket/${ticketId}`, {
    method: "GET",
  });
}

export async function refundPayment(paymentId) {
  return request(`${TICKET_API_BASE}/payments/${paymentId}/refund`, {
    method: "POST",
  });
}

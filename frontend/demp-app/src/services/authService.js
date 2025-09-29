const API_URL = 'http://localhost:8080/api/auth'; // Change this to your backend URL if needed

// USER LOGIN
export const login = async (email, password) => {
  const response = await fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Login failed');
  }

  const data = await response.json();
  localStorage.setItem('auth_token', data.token);

  if (data.userName && data.email && data.contactNo) {
    localStorage.setItem(
      'user',
      JSON.stringify({
        userName: data.userName,
        email: data.email,
        contactNo: data.contactNo,
        role: data.role,
      })
    );
  }

  return data;
};

// USER SIGNUP
export const signup = async (name, email, role, password, contactNo) => {
  const response = await fetch(`${API_URL}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userName: name, email, role, password, contactNo }),
  });

  if (!response.ok) {
    let error = {};
    try {
      error = await response.json();
    } catch {
      error.message = 'Signup failed';
    }
    throw new Error(error.message || 'Signup failed');
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : {};

  if (data.userName && data.email && data.contactNo) {
    localStorage.setItem(
      'user',
      JSON.stringify({
        userName: data.userName,
        email: data.email,
        contactNo: data.contactNo,
        role: data.role,
      })
    );
  }

  return data;
};

// USER TOKEN
export const getToken = () => {
  return localStorage.getItem('auth_token');
};

export const removeToken = () => {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('user');
};

export const isAuthenticated = () => {
  return !!getToken();
};

export const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

// ADMIN TOKEN MANAGEMENT

export const setAdminToken = (token) => {
  localStorage.setItem('adminToken', token);
};

export const getAdminToken = () => {
  return localStorage.getItem('adminToken');
};


export const removeAdminToken = () => {
  localStorage.removeItem('adminToken');
  localStorage.removeItem('user'); // optional if you store user info
};

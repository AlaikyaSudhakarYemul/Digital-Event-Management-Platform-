import { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState('');
  const [role, setRole] = useState('');

  // read from localStorage
  const readAuthFromStorage = () => {
    try {
      const savedUser = JSON.parse(localStorage.getItem('user') || 'null');
      const savedToken = localStorage.getItem('authToken') || localStorage.getItem('token') || localStorage.getItem('auth_token') || '';
      const savedRole = (localStorage.getItem('userRole') || '').toUpperCase();
      setUser(savedUser);
      setToken(savedToken || '');
      setRole(savedRole || '');
    } catch {
      setUser(null);
      setToken('');
      setRole('');
    }
  };

  useEffect(() => {
    readAuthFromStorage();
    const onStorage = () => readAuthFromStorage();
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  // login keeps compatibility with existing calls: login(userData)
  const login = (userData) => {
    setUser(userData || null);
    if (userData) localStorage.setItem('user', JSON.stringify(userData));
    // also refresh token/role from storage if AuthPopup already saved them
    const savedToken = localStorage.getItem('authToken') || localStorage.getItem('token') || localStorage.getItem('auth_token') || '';
    const savedRole = (localStorage.getItem('userRole') || '').toUpperCase();
    setToken(savedToken || '');
    setRole(savedRole || '');
  };

  const logout = () => {
    setUser(null);
    setToken('');
    setRole('');
    localStorage.removeItem('authToken');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role,
        isOrganizer: role === 'ORGANIZER',
        login,
        logout,
        refreshAuthFromStorage: readAuthFromStorage,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
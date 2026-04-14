import { createContext, useState, useEffect } from 'react';
 
export const AuthContext = createContext();
 
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  const normalizeUser = (userData) => {
    if (!userData) return null;
    if (userData.user) return userData.user;
    return userData;
  };
 
  useEffect(() => {
    const savedUser = JSON.parse(localStorage.getItem('user'));
    if (savedUser) setUser(savedUser);
  }, []);
 
  const login = (userData) => {
    const normalizedUser = normalizeUser(userData);
    setUser(normalizedUser);
    localStorage.setItem('user', JSON.stringify(normalizedUser));
    if (userData?.token) {
      localStorage.setItem('auth_token', userData.token);
    }
  };
 
  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('auth_token');
  };
 
  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
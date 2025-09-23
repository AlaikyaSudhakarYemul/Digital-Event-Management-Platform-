import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setAdminToken } from '../../services/authService';
 
const AdminLogin = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
 
  const handleLogin = async (e) => {
    e.preventDefault();
 
    try {
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
 
      if (!res.ok) throw new Error('Invalid credentials');
 
      const data = await res.json();
 
      if (data.user.role !== 'ADMIN') {
        setError('Access denied. You are not an admin.');
        return;
      }
 
      // Save admin token
      setAdminToken(data.token);
 
      // Optionally store admin info
      localStorage.setItem('user', JSON.stringify({
        email: data.user.email,
        role: data.user.role,
        userName: data.user.userName
      }));
 
      navigate('/admin/dashboard');
    } catch (err) {
      setError('Login failed. Please check your credentials.');
    }
  };
 
  return (
    <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
      <div className="bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-md">
        <h2 className="text-2xl font-bold text-cyan-400 mb-6 text-center">Admin Login</h2>
        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              placeholder="Enter email"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              placeholder="Enter password"
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-cyan-500 hover:bg-cyan-600 text-white font-semibold py-2 px-4 rounded-lg"
          >
            Login
          </button>
          {error && <p className="text-red-400 text-sm mt-2 text-center">{error}</p>}
        </form>
      </div>
    </div>
  );
};
 
export default AdminLogin;
 
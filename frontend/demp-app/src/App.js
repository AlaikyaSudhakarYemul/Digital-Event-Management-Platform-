import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import UserDashboard from './pages/UserDashboard/UserDashboard';
import Home from './pages/Home/Home';
import { getToken, getAdminToken } from './services/authService'; // Assuming you have separate tokens
import AdminLogin from './pages/Admin/AdminLogin'; // Add your admin login component
import AdminDashboard from './pages/Admin/AdminDashboard'; // Add your admin dashboard component
import './App.css';

// PrivateRoute for user
function PrivateRoute({ children }) {
  return getToken() ? children : <Navigate to="/" />;
}

// PrivateRoute for admin
function AdminPrivateRoute({ children }) {
  return getAdminToken() ? children : <Navigate to="/admin/login" />;
}

function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<Home />} />

      {/* User Protected Route */}
      <Route
        path="/userdashboard"
        element={
          <PrivateRoute>
            <UserDashboard />
          </PrivateRoute>
        }
      />

      {/* Admin Routes */}
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route
        path="/admin/dashboard"
        element={
          <AdminPrivateRoute>
            <AdminDashboard />
          </AdminPrivateRoute>
        }
      />
    </Routes>
  );
}

export default App;

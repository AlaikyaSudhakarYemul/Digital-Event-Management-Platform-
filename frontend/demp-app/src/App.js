import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { getToken } from './services/authService';
import UserDashboard from './pages/UserDashboard/UserDashboard';
import Home from './pages/Home/Home';

// PrivateRoute for user
function PrivateRoute({ children }) {
  return getToken() ? children : <Navigate to="/" />;
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
      {/* Add admin routes here */}
    </Routes>
  );
}

export default App;
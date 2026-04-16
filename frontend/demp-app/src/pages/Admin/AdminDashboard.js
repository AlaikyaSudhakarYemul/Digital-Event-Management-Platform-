import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);

  const [users, setUsers] = useState([]);
  const [usersError, setUsersError] = useState('');
  const [activeTab, setActiveTab] = useState('users');

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (!token) return;

    fetch(`${API_BASE}/api/user/all`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok) throw new Error('Failed to load users');
        return res.json();
      })
      .then(setUsers)
      .catch((err) => setUsersError(err.message));
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/admin/login');
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Top bar */}
      <header className="bg-indigo-700 text-white px-6 py-4 flex justify-between items-center shadow">
        <div className="flex items-center space-x-3">
          <span className="text-xl font-bold">EVENTRA</span>
          <span className="text-indigo-300 text-sm">/ Admin Dashboard</span>
        </div>
        <div className="flex items-center space-x-4">
          <span className="text-sm">
            Logged in as <strong>{user?.userName || user?.name || 'Admin'}</strong>
          </span>
          <button
            onClick={handleLogout}
            className="bg-indigo-500 hover:bg-indigo-400 px-3 py-1 rounded-lg text-sm transition"
          >
            Logout
          </button>
        </div>
      </header>

      <div className="flex">
        {/* Sidebar */}
        <aside className="w-56 bg-white shadow-md min-h-screen pt-6">
          <nav className="space-y-1 px-3">
            {[
              { id: 'users', label: 'All Users' },
              { id: 'organizers', label: 'Organizers' },
            ].map((item) => (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`w-full text-left px-4 py-2 rounded-lg text-sm font-medium transition ${
                  activeTab === item.id
                    ? 'bg-indigo-100 text-indigo-700'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>
        </aside>

        {/* Main content */}
        <main className="flex-1 p-8">
          <h2 className="text-xl font-bold text-gray-800 mb-6">
            {activeTab === 'users' ? 'All Users' : 'Organizers'}
          </h2>

          {usersError && (
            <div className="mb-4 p-3 bg-red-50 border border-red-300 text-red-700 rounded-lg text-sm">
              {usersError}
            </div>
          )}

          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead className="bg-indigo-50 text-indigo-700">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold">ID</th>
                  <th className="px-4 py-3 text-left font-semibold">Name</th>
                  <th className="px-4 py-3 text-left font-semibold">Email</th>
                  <th className="px-4 py-3 text-left font-semibold">Role</th>
                  <th className="px-4 py-3 text-left font-semibold">Contact</th>
                  <th className="px-4 py-3 text-left font-semibold">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {users
                  .filter((u) =>
                    activeTab === 'organizers' ? u.role === 'ORGANIZER' : true
                  )
                  .map((u) => (
                    <tr key={u.userId} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-gray-500">{u.userId}</td>
                      <td className="px-4 py-3 font-medium text-gray-800">{u.userName}</td>
                      <td className="px-4 py-3 text-gray-600">{u.email}</td>
                      <td className="px-4 py-3">
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-semibold ${
                            u.role === 'ADMIN'
                              ? 'bg-red-100 text-red-700'
                              : u.role === 'ORGANIZER'
                              ? 'bg-yellow-100 text-yellow-700'
                              : 'bg-green-100 text-green-700'
                          }`}
                        >
                          {u.role}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{u.contactNo || '—'}</td>
                      <td className="px-4 py-3">
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-semibold ${
                            u.isDeleted
                              ? 'bg-gray-200 text-gray-500'
                              : 'bg-emerald-100 text-emerald-700'
                          }`}
                        >
                          {u.isDeleted ? 'Deleted' : 'Active'}
                        </span>
                      </td>
                    </tr>
                  ))}
                {users.filter((u) =>
                  activeTab === 'organizers' ? u.role === 'ORGANIZER' : true
                ).length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                      No records found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminDashboard;

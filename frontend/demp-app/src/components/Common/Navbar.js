import React, { useContext, useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from './../../assets/images/logo.png';
import { AuthContext } from '../../contexts/AuthContext';
import { UserCircleIcon } from '@heroicons/react/24/outline';
 
const Navbar = ({ onSignUpClick, onCreateEventClick }) => {
 
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown on outside click
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
 
  return (
    <nav className="relative z-10 flex items-center justify-between px-6 py-6 max-w-7xl mx-auto">
      <div className="flex items-center space-x-2">
        <img src={logo} className="w-8 h-8 bg-cyan-400 rounded-lg" alt="Logo" />
        <span className="text-white font-bold text-xl">EVENTRA</span>
      </div>
 
      <div className="hidden md:flex items-center space-x-6">
        <a href="#" className="text-white hover:text-cyan-300 transition-colors">Contact us</a>
        <a href="#" className="text-white hover:text-cyan-300 transition-colors">About us</a>
        <a href="#" className="text-white hover:text-cyan-300 transition-colors">Portfolio</a>
 
        {user ? (
          <>
            {user.role === 'ORGANIZER' && (
              <button
                onClick={onCreateEventClick}
                className="bg-white text-purple-600 px-4 py-2 rounded-full hover:bg-purple-100 transition"
              >
                Create Event
              </button>
            )}

            <div className="flex items-center space-x-3 ml-4 relative" ref={dropdownRef}>
              <button
                className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-purple-600 font-semibold focus:outline-none"
                onClick={() => setDropdownOpen((open) => !open)}
              >
                {user.name
                  ? user.name.charAt(0).toUpperCase()
                  : <UserCircleIcon className="w-6 h-6" />}
              </button>
              {dropdownOpen && (
                <div className="absolute right-0 mt-2 w-40 bg-white rounded shadow-lg z-20">
                  <button
                    className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
                    onClick={() => {
                      setDropdownOpen(false);
                      if (user.role === 'ORGANIZER') {
                        navigate('/organizer/dashboard');
                      } else {
                        navigate('/userdashboard');
                      }
                    }}
                  >
                    {user.role === 'ORGANIZER' ? 'Organizer Dashboard' : 'User Dashboard'}
                  </button>
                  <button
                    className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
                    onClick={() => {
                      setDropdownOpen(false);
                      logout();
                    }}
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
          </>
        ) : (
          <button
            className="border border-white text-white hover:bg-white hover:text-purple-600 px-4 py-2 rounded-full transition-colors"
            onClick={onSignUpClick}
          >
            Sign up
          </button>
        )}
      </div>
    </nav>
  );
};
 
export default Navbar;
 
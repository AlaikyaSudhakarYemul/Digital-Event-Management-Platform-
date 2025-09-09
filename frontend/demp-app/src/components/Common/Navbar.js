import React, { useContext } from 'react';
import logo from './../../assets/images/logo.png';
import { AuthContext } from '../../contexts/AuthContext';
import { UserCircleIcon } from '@heroicons/react/24/outline';
 
const Navbar = ({ onSignUpClick, onCreateEventClick }) => {
 
  const { user, logout } = useContext(AuthContext);
 
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
            <button
              onClick={onCreateEventClick}
              className="bg-white text-purple-600 px-4 py-2 rounded-full hover:bg-purple-100 transition"
            >
              Create Event
            </button>
 
            <div className="flex items-center space-x-3 ml-4">
              <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-purple-600 font-semibold">
                {user.name
                  ? user.name.charAt(0).toUpperCase()
                  : <UserCircleIcon className="w-6 h-6" />}
              </div>
 
              <button
                onClick={logout}
                className="text-white border border-white px-3 py-1 rounded-full hover:bg-white hover:text-purple-600 transition"
              >
                Logout
              </button>
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
 
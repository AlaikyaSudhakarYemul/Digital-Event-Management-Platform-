import React, {useState} from 'react';
import SignupPopup from './SignUp';
import logo from './../../assets/images/logo.png';
 
const HeroSection = () => {
      const [showSignup, setShowSignup] = useState(false);
  return (
    <div >
      {/* Main Content */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 pt-0 pb-12">
 
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left Side - Illustration */}
          <div className="relative">
            {/* Background Shapes */}
            <div className="absolute inset-0 opacity-20">
              <div className="absolute top-10 left-10 w-32 h-32 bg-purple-400 rounded-full blur-xl"></div>
              <div className="absolute bottom-20 right-10 w-24 h-24 bg-pink-400 rounded-full blur-lg"></div>
              <div className="absolute top-1/2 left-1/4 w-16 h-16 bg-yellow-400 rounded-full blur-md"></div>
            </div>
            
            {/* Central Clipboard */}
            <div className="relative z-10 flex justify-center">
              <div className="bg-white rounded-2xl p-6 shadow-2xl transform rotate-3 hover:rotate-0 transition-transform duration-300">
                <div className="bg-gray-800 w-16 h-4 rounded-full mx-auto mb-4"></div>
                <div className="bg-gradient-to-r from-purple-500 to-pink-500 text-white p-4 rounded-lg mb-4">
                  <h3 className="font-bold text-lg mb-2">EVENT</h3>
                  <h3 className="font-bold text-lg">MANAGEMENT</h3>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center space-x-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <div className="h-2 bg-gray-200 rounded flex-1"></div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <div className="h-2 bg-gray-200 rounded flex-1"></div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <div className="h-2 bg-gray-200 rounded flex-1"></div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <div className="h-2 bg-gray-200 rounded flex-1"></div>
                  </div>
                </div>
              </div>
            </div>
 
            {/* Floating Elements */}
            <div className="absolute top-4 left-4 bg-yellow-400 rounded-xl p-3 shadow-lg animate-bounce">
              <div className="w-8 h-8 bg-white rounded-full flex items-center justify-center">
                <span className="text-yellow-600 font-bold">$</span>
              </div>
            </div>
 
            <div className="absolute top-20 right-8 bg-pink-400 rounded-xl p-3 shadow-lg animate-pulse">
              <div className="w-6 h-6 bg-white rounded-full"></div>
            </div>
 
            <div className="absolute bottom-16 left-8 bg-cyan-400 rounded-xl p-3 shadow-lg">
              <div className="w-6 h-6 bg-white rounded-full"></div>
            </div>
 
            <div className="absolute bottom-4 right-16 bg-white rounded-xl p-2 shadow-lg">
              <div className="w-8 h-8 bg-gradient-to-r from-pink-400 to-purple-400 rounded-lg"></div>
            </div>
 
            {/* Character */}
            <div className="absolute bottom-8 right-8">
              <div className="w-16 h-20 bg-gradient-to-b from-yellow-400 to-orange-400 rounded-t-full"></div>
              <div className="w-16 h-12 bg-blue-500 rounded-b-lg"></div>
            </div>
          </div>
 
          {/* Right Side - Text Content */}
          <div className="text-white space-y-6">
            <h1 className="text-5xl lg:text-6xl font-bold leading-tight">
              Planning
              <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-white">
                software
              </span>
            </h1>
           
            <div className="w-16 h-1 bg-white"></div>
           
            <p className="text-lg text-gray-200 leading-relaxed max-w-lg">
              Delivering seamless event experiences through smart planning and technology.
             Empower your team with tools that streamline coordination, communication, and success.
            </p>
           
            <button className="bg-cyan-400 hover:bg-cyan-500 text-gray-900 font-semibold px-8 py-4 rounded-full transition-colors transform hover:scale-105 duration-200">
              Get started!
            </button>
          </div>
        </div>
      </div>
 
      {/* Background Decorative Elements */}
      <div className="absolute inset-0 opacity-10 pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-2 h-2 bg-white rounded-full animate-ping"></div>
        <div className="absolute top-1/3 right-1/3 w-1 h-1 bg-cyan-400 rounded-full animate-pulse"></div>
        <div className="absolute bottom-1/4 left-1/3 w-1.5 h-1.5 bg-pink-400 rounded-full animate-bounce"></div>
        <div className="absolute top-2/3 right-1/4 w-1 h-1 bg-yellow-400 rounded-full animate-ping"></div>
      </div>
            {/* Show the signup popup */}
      {showSignup && <SignupPopup onClose={() => setShowSignup(false)} />}
 
    </div>
  );
};
 
export default HeroSection;
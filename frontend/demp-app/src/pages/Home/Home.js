import React, { useState, useEffect, useContext } from 'react';
import Navbar from '../../components/Common/Navbar';
import HeroSection from '../../components/Home/HomeSection';
import Footer from '../../components/Common/Footer';
import SignupPopup from '../../components/Home/SignUp';
import HowItWorks from '../../components/Home/HowItWorks';
import AuthPopup from '../../components/Home/SignUp';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import UpcomingEvents from '../../components/Home/UpcomingEvents';
 
const Home = () => {
  const navigate = useNavigate();
  const [showSignup, setShowSignup] = useState(false);
  const [showPopup, setShowPopup] = useState(false);
  const { user } = useContext(AuthContext);

  const handleCreateEventClick = () => {
    if (!user) {
      setShowPopup(true);
    } else {
      navigate('/CreateEvent');
    }
  };
 
  return (
    <div className="relative min-h-screen bg-gradient-to-br from-pink-500 via-purple-600 to-indigo-900 overflow-hidden">
      <Navbar
        onSignUpClick={() => setShowPopup(true)}
        onCreateEventClick={handleCreateEventClick}
      />

      {showPopup && (
        <AuthPopup
          onClose={() => setShowPopup(false)}
          onLoginSuccess={() => setShowPopup(false)}
        />
      )}

      <HeroSection />
      <HowItWorks />

      {/* Upcoming Events Component */}
      <UpcomingEvents navigate={navigate} />

      <Footer />

      {showSignup && <SignupPopup onClose={() => setShowSignup(false)} />}
    </div>
  );
};
 
export default Home;
 
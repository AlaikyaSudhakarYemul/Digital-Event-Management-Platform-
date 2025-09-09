import React, { useState, useEffect, useContext } from 'react';
import Navbar from '../../components/Common/Navbar';
import HeroSection from '../../components/Home/HomeSection';
import Footer from '../../components/Common/Footer';
import SignupPopup from '../../components/Home/SignUp';
import HowItWorks from '../../components/Home/HowItWorks';
import AuthPopup from '../../components/Home/SignUp';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
 
const Home = () => {
  const navigate = useNavigate();
  const [showSignup, setShowSignup] = useState(false);
  const [showPopup, setShowPopup] = useState(false);
  const [events, setEvents] = useState([]);
  const { user } = useContext(AuthContext);
 
  const handleCreateEventClick = () => {
    if (!user) {
      setShowPopup(true);
    } else {
      navigate('/CreateEvent');
    }
  };
 
  useEffect(() => {
    fetch('http://localhost:8080/api/events/all')
      .then(response => response.json())
      .then(data => {
        console.log('Fetched events:', data);
        setEvents(data);
      })
      .catch(error => console.error('Error fetching events:', error));
  }, []);
 
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
 
      {/* Dynamic Event List from Backend */}
      <div className="max-w-2xl mx-auto mt-10">
        <h2 className="text-2xl font-bold mb-4 text-white">Upcoming Events</h2>
 
        {events.map(event => (
          <div key={event.eventId} className="border rounded-lg p-2 mb-4 shadow bg-white">
            {event.imageUrl && (
              <img
                src={event.imageUrl}
                alt={event.eventName}
                className="w-full h-48 object-cover rounded-t-lg mb-2"
              />
            )}
            <h3 className="text-xl font-semibold">{event.eventName}</h3>
            <p>Date: {event.date}</p>
            <p>Location: {event.address?.address || 'Not specified'}</p>
 
            <button
  onClick={() => {
    console.log('Navigating to event:', event.eventId);
    navigate(`/events/${event.eventId}`);
  }}
  className="mt-2 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition font-semibold"
>
  View Details
</button>
 
          </div>
        ))}
      </div>
 
      <Footer />
 
      {showSignup && <SignupPopup onClose={() => setShowSignup(false)} />}
    </div>
  );
};
 
export default Home;
 
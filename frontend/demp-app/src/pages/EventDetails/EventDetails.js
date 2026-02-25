// src/pages/EventDetails.jsx
import React, { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import { registerForEvent } from '../../services/eventService';

const EventDetails = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [event, setEvent] = useState(null);
  const [error, setError] = useState(null);
  const [registerLoading, setRegisterLoading] = useState(false);
  const [registerMessage, setRegisterMessage] = useState('');
  const [isRegistered, setIsRegistered] = useState(false);
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        console.log("Fetching event with ID:", eventId);
        const res = await fetch(`http://localhost:8080/api/events/${eventId}`);
        console.log("Fetch response:", res);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        console.log("Fetched event data:", data);
        if (!cancelled) setEvent(data);
      } catch (e) {
        console.error('Error fetching event:', e);
        if (!cancelled) setError(e.message);
      }
    })();
    return () => { cancelled = true; };
  }, [eventId]);

  const formatDate = (d) => {
    if (!d) return '—';
    const dt = new Date(d);
    return isNaN(dt) ? d : dt.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  };

  const formatTime = (t) => {
    // Handles "HH:mm:ss", "HH:mm", or ISO datetime; otherwise returns raw
    if (!t) return '—';
    // If backend sends "17:15:00"
    if (/^\d{2}:\d{2}(:\d{2})?$/.test(t)) {
      const [h, m] = t.split(':');
      const d = new Date();
      d.setHours(Number(h), Number(m || 0), 0, 0);
      return d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
    }
    const dt = new Date(t);
    return isNaN(dt) ? t : dt.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
  };

  const handleRegister = async () => {
    if (!user) {
      setRegisterMessage('Please log in to register.');
      return;
    }
    setRegisterLoading(true);
    setRegisterMessage('');
    try {
      await registerForEvent(eventId, user);
      setIsRegistered(true);
      setShowSuccessPopup(true);
    } catch (e) {
      setRegisterMessage(e.message);
    } finally {
      setRegisterLoading(false);
    }
  };

  const handlePopupClose = () => {
    setShowSuccessPopup(false);
    navigate('/');
  };

  if (error) return <div className="p-6 text-red-400">Failed to load: {error}</div>;
  if (!event) return <div className="flex justify-center items-center h-screen text-white">Loading...</div>;

  // Safely compute full address only if event.address is present
  const a = event.address;
  const hasAddress = !!a;
  const fullAddress = hasAddress
    ? [
        a.address ?? a.addressLine ?? a.street ?? '',
        a.state,
        a.country,
      ].filter(Boolean).join(', ')
      + (a.pincode || a.zip || a.postalCode ? ` - ${a.pincode ?? a.zip ?? a.postalCode}` : '')
    : '—';

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Hero Section with Image */}
      {event.image && (
        <div className="relative w-full h-64 md:h-96 bg-gray-800">
          <img
            src={event.image}
            alt={event.eventName}
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <h1 className="text-4xl md:text-5xl font-bold text-center">{event.eventName}</h1>
          </div>
        </div>
      )}

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Details */}
          <div className="lg:col-span-2">
            {!event.image && <h1 className="text-4xl font-bold mb-6">{event.eventName}</h1>}

            <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
              <p className="text-lg mb-2"><strong>Date:</strong> {formatDate(event.date)}</p>
              <p className="text-lg mb-2"><strong>Time:</strong> {formatTime(event.time)}</p>
              <p className="text-lg mb-2"><strong>Event Type:</strong> {event.eventType ?? '—'}</p>
              <p className="text-lg mb-2"><strong>Location:</strong> {hasAddress ? (a.address ?? a.addressLine ?? a.street ?? '—') : '—'}</p>
              <p className="text-lg mb-4"><strong>Full Address:</strong> {fullAddress}</p>
              <p className="text-lg mb-6"><strong>Description:</strong> {event.description ?? '—'}</p>

              {/* Register Button */}
              <button
                onClick={handleRegister}
                disabled={registerLoading || isRegistered}
                className={`bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white font-bold py-3 px-6 rounded-lg transition duration-300 ${isRegistered ? 'cursor-not-allowed' : ''}`}
              >
                {isRegistered ? 'Registered' : registerLoading ? 'Registering...' : 'Register'}
              </button>
              {registerMessage && (
                <p className={`mt-4 text-sm ${registerMessage.includes('Successfully') ? 'text-green-400' : 'text-red-400'}`}>
                  {registerMessage}
                </p>
              )}
            </div>
          </div>

          {/* Success Popup */}
          {showSuccessPopup && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60">
              <div className="bg-white rounded-lg shadow-lg p-8 max-w-sm w-full mx-4">
                <h2 className="text-2xl font-bold text-green-600 mb-4">Success!</h2>
                <p className="text-gray-600 mb-6">You have successfully registered for the event.</p>
                <button
                  onClick={handlePopupClose}
                  className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition duration-300"
                >
                  OK
                </button>
              </div>
            </div>
          )}

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Speakers */}
            {event.speakers?.length > 0 && (
              <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
                <h2 className="text-2xl font-semibold mb-4">Speakers</h2>
                {event.speakers.map((speaker, index) => (
                  <div key={index} className="mb-4 last:mb-0">
                    <p className="font-bold">{speaker.name}</p>
                    <p className="text-sm text-gray-300">{speaker.bio}</p>
                  </div>
                ))}
              </div>
            )}

            {/* Map Placeholder */}
            {hasAddress && (
              <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
                <h2 className="text-2xl font-semibold mb-4">Location Map</h2>
                <div className="bg-gray-700 h-48 rounded flex items-center justify-center text-gray-400">
                  Map integration placeholder (e.g., Google Maps)
                </div>
              </div>
            )}

            {/* Additional Info */}
            <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
              <h2 className="text-2xl font-semibold mb-4">Event Info</h2>
              <p><strong>Type:</strong> {event.eventType ?? '—'}</p>
              {/* Add more if available, e.g., capacity */}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EventDetails;
import React, { useContext, useEffect, useMemo, useState } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
 
const UpcomingEvents = () => {
  const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
  const { user } = useContext(AuthContext);
  const [searchTerm, setSearchTerm] = useState('');
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const normalizedUser = useMemo(() => (user && user.user ? user.user : user), [user]);

  useEffect(() => {
    const fetchEvents = async () => {
      setLoading(true);
      setError('');

      try {
        const role = (normalizedUser?.role || '').toUpperCase();
        const userId = normalizedUser?.userId;

        let endpoint = `${API_BASE}/api/events/all`;
        if (role === 'ORGANIZER' && userId) {
          endpoint = `${API_BASE}/api/events/user/${userId}`;
        }

        const response = await fetch(endpoint);
        if (!response.ok) {
          throw new Error('Failed to load events');
        }

        const data = await response.json();
        setEvents(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || 'Unable to load events right now');
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, [API_BASE, normalizedUser]);

  const filteredEvents = events.filter((event) =>
    (event.eventName || '').toLowerCase().includes(searchTerm.toLowerCase())
  );
 
  return (
    <section className="py-20 text-center text-white">
      <h2 className="text-3xl md:text-4xl font-bold mb-6">Upcoming Events</h2>
 
      {/* Search Bar */}
      <div className="mb-8">
        <input
          type="text"
          placeholder="Search by event name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="px-4 py-2 rounded-full text-black w-64"
        />
      </div>
 
      {loading && <p className="text-white/80">Loading events...</p>}
      {!loading && error && <p className="text-red-200">{error}</p>}

      {/* Event Cards */}
      <div className="flex flex-col md:flex-row justify-center gap-8 px-6">
        {!loading && !error && filteredEvents.map((event) => (
          <div
            key={event.eventId}
            className="bg-white/10 backdrop-blur-md rounded-xl shadow-md p-6 w-full md:w-80 text-white"
          >
            <h3 className="text-lg font-semibold mb-2">{event.eventName}</h3>
            <p className="text-sm text-gray-200 mb-3">{event.date}</p>
            <div className="flex flex-wrap gap-2 justify-center mb-4">
              {[event.eventType, event.activeStatus].filter(Boolean).map((tag, i) => (
                <span
                  key={i}
                  className="text-xs bg-white/20 px-2 py-1 rounded-full text-white"
                >
                  {tag}
                </span>
              ))}
            </div>
            <button className="mt-2 px-4 py-2 border border-white text-white rounded-full hover:bg-white hover:text-purple-600 transition-colors">
              Learn More
            </button>
          </div>
        ))}
      </div>

      {!loading && !error && filteredEvents.length === 0 && (
        <p className="mt-6 text-white/80">No events found for this organizer.</p>
      )}
 
      <div className="mt-12">
        <h3 className="text-xl font-semibold mb-4">Ready to get started?</h3>
        <button className="bg-white text-purple-600 px-6 py-3 rounded-full hover:bg-purple-200 transition-colors">
          Sign Up Now
        </button>
      </div>
    </section>
  );
};
 
export default UpcomingEvents;
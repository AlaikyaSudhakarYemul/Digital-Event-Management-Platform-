import React, { useState, useEffect } from 'react';
// If you want the whole card clickable later, you can also import Link:
// import { Link } from 'react-router-dom';
import { deleteEvent } from '../../services/eventService';

const UpcomingEvents = ({ navigate }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [events, setEvents] = useState([]);
  const [deletingId, setDeletingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const res = await fetch('http://localhost:8080/api/events/all');
        if (res.status === 404 || res.status === 500) {
          // If no events or server error, treat as empty
          setEvents([]);
        } else if (!res.ok) {
          throw new Error('Failed to fetch events');
        } else {
          const data = await res.json();
          setEvents(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        console.error(err);
        setError('Could not load events');
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  const filteredEvents = events.filter((event) =>
    event.eventName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Helper: Safely resolve the event id field name
  const getId = (event) => event?.eventId ?? event?.id ?? event?.eventID;

  // (Optional) Date formatting
  const formatDate = (d) => {
    if (!d) return '';
    const dt = new Date(d);
    if (Number.isNaN(dt.getTime())) return d; // fallback to raw if parse fails
    return dt.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  };

  // Navigate to details
  const goToDetails = (id) => {
    if (!id) return;
    navigate(`/events/${id}`);
  };

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
          aria-label="Search events by name"
        />
      </div>

      {/* Event Cards */}
      {loading ? (
        <div className="text-center text-gray-300">Loading events...</div>
      ) : error ? (
        <div className="text-center text-red-400">{error}</div>
      ) : (
        <div className="flex flex-col md:flex-row justify-center gap-8 px-6">
          {filteredEvents.length === 0 ? (
            <div className="text-center text-gray-300 w-full">No events found.</div>
          ) : (
            filteredEvents.map((event, idx) => {
              const id = getId(event);
              return (
                <div
                  key={id ?? idx}
                  className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg w-full md:w-80 text-white relative overflow-hidden p-0 group"
                  style={
                    event.image
                      ? {
                          backgroundImage: `url(${event.image})`,
                          backgroundSize: 'cover',
                          backgroundPosition: 'center',
                          minHeight: '340px',
                        }
                      : { minHeight: '340px' }
                  }
                >
                  <div className="relative flex flex-col justify-end h-full min-h-[340px] rounded-xl overflow-hidden shadow-lg group">
                    {/* Gradient overlay for text readability */}
                    <div
                      className="absolute inset-0 z-0"
                      style={{
                        background:
                          'linear-gradient(180deg, rgba(0,0,0,0.05) 40%, rgba(0,0,0,0.7) 100%)',
                      }}
                    />
                    <div className="relative z-10 p-6 flex flex-col justify-end h-full">
                      <div>
                        <h3
                          className="text-3xl font-extrabold mb-3 text-cyan-200 bg-cyan-700 px-4 py-2 rounded-full shadow-lg tracking-wide transition-all duration-200"
                          style={{ textShadow: '0 2px 8px rgba(0,0,0,0.5)' }}
                          title={event.eventName}
                        >
                          {event.eventName}
                        </h3>

                        <p className="text-lg font-semibold mb-2 text-white/90 bg-black/60 px-3 py-1 rounded-full inline-block shadow">
                          <span className="mr-2">📅</span>
                          {formatDate(event.date)}
                        </p>

                        <div className="flex flex-col gap-2 justify-center mb-3 items-center">
                          {event.eventType && (
                            <span className="text-sm font-bold bg-cyan-900/80 px-3 py-1 rounded-full text-cyan-100 shadow w-fit mb-1">
                              {event.eventType}
                            </span>
                          )}
                          {event.address && (
                            <span className="text-sm font-bold bg-purple-900/80 px-3 py-1 rounded-full text-purple-100 shadow w-fit">
                              {event.address.address}, {event.address.state}, {event.address.country}
                            </span>
                          )}
                        </div>

                        {event.speakers && event.speakers.length > 0 && (
                          <div className="mb-2 text-lg font-bold text-yellow-100 bg-yellow-900/80 px-3 py-1 rounded-full inline-block shadow">
                            <span className="mr-2">🎤 Speaker:</span> {event.speakers[0].name}
                          </div>
                        )}
                      </div>

                      {/* Register Button → navigate to details */}
                      <button
                        className="mt-4 mb-2 px-6 py-2 bg-blue-700 text-white font-bold rounded-full shadow hover:bg-blue-900 transition-colors"
                        onClick={() => goToDetails(id)}
                        aria-label={`View details for ${event.eventName}`}
                      >
                        Register
                      </button>

                      {/* Countdown */}
                      {event.date ? (
                        (() => {
                          const eventDate = new Date(event.date);
                          const today = new Date();
                          // normalize to midnight to avoid partial-day rounding issues
                          const diffTime =
                            eventDate.setHours(0, 0, 0, 0) - today.setHours(0, 0, 0, 0);
                          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

                          if (diffDays > 0) {
                            return (
                              <div className="px-4 py-2 bg-green-900/80 text-green-100 rounded-full font-bold shadow border border-green-700">
                                {diffDays} day{diffDays > 1 ? 's' : ''} to go
                              </div>
                            );
                          } else if (diffDays === 0) {
                            return (
                              <div className="px-4 py-2 bg-yellow-900/80 text-yellow-100 rounded-full font-bold shadow border border-yellow-700">
                                Today!
                              </div>
                            );
                          } else {
                            return (
                              <div className="px-4 py-2 bg-gray-900/80 text-gray-100 rounded-full font-bold shadow border border-gray-700">
                                Event Ended
                              </div>
                            );
                          }
                        })()
                      ) : null}
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
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
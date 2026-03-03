import React, { useEffect, useMemo, useState, useCallback } from 'react';
// If you want the whole card clickable later, you can also import Link:
// import { Link } from 'react-router-dom';
import { deleteEvent } from '../../services/eventService';

const API_BASE = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8080';

const buildQuery = (params) =>
  Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');

const UpcomingEvents = ({ navigate }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [pageIndex, setPageIndex] = useState(0); 
  const pageSize = 3; 

  const [page, setPage] = useState({
    content: [],
    number: 0,
    size: pageSize,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const [deletingId, setDeletingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [noContent, setNoContent] = useState(false);

  // Build paginated URL (always size=2)
  const listUrl = useMemo(() => {
    const qs = buildQuery({
      page: pageIndex,
      size: pageSize,
      eventName: searchTerm || undefined, // backend optional filter
    });
    return `${API_BASE}/api/events/paginated?${qs}`;
  }, [pageIndex, searchTerm]);

  const fetchEvents = useCallback(async () => {
    setLoading(true);
    setError('');
    setNoContent(false);
    try {
      const res = await fetch(listUrl, {
        headers: { 'Content-Type': 'application/json' },
      });

      if (res.status === 204) {
        // No Content
        setNoContent(true);
        setPage((p) => ({
          ...p,
          content: [],
          number: pageIndex,
          size: pageSize,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        }));
        return;
      }

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const data = await res.json();
      setPage({
        content: Array.isArray(data.content) ? data.content : [],
        number: data.number ?? pageIndex,
        size: pageSize,
        totalElements: data.totalElements ?? 0,
        totalPages: data.totalPages ?? 0,
        first: data.first ?? (pageIndex === 0),
        last: data.last ?? (pageIndex >= (data.totalPages ?? 1) - 1),
      });
    } catch (err) {
      console.error(err);
      setError(err?.message || 'Could not load events');
      setPage((p) => ({
        ...p,
        content: [],
        number: pageIndex,
        size: pageSize,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
      }));
    } finally {
      setLoading(false);
    }
  }, [listUrl, pageIndex]);

  // Fetch on mount and whenever query changes
  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  useEffect(() => {
    setPageIndex(0);
  }, [searchTerm]);

  const getId = (event) => event?.eventId ?? event?.id ?? event?.eventID;

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

  // Pagination handlers
  const gotoFirst = () => setPageIndex(0);
  const gotoPrev = () => setPageIndex((p) => Math.max(0, p - 1));
  const gotoNext = () => setPageIndex((p) => Math.min((page.totalPages || 1) - 1, p + 1));
  const gotoLast = () => setPageIndex(Math.max(0, (page.totalPages || 1) - 1));
  const goto = (i) => setPageIndex(i);

  // Simple page window (5 buttons max)
  const pageWindow = React.useMemo(() => {
    const total = page.totalPages || 0;
    const current = page.number || 0;
    const windowSize = 5;
    const half = Math.floor(windowSize / 2);
    const start = Math.max(0, Math.min(current - half, Math.max(0, total - windowSize)));
    const end = Math.max(0, Math.min(total, start + windowSize));
    return Array.from({ length: end - start }, (_, i) => start + i);
  }, [page.totalPages, page.number]);

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
        <>
          <div className="flex flex-col md:flex-row justify-center gap-8 px-6">
            {noContent || (page.content || []).length === 0 ? (
              <div className="text-center text-gray-300 w-full">No events found.</div>
            ) : (
              page.content.map((event, idx) => {
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

          {/* Pagination bar */}
          {page.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 flex-wrap mt-8">
              <button
                className="px-3 py-1 rounded bg-gray-900 border border-gray-700 disabled:opacity-50"
                disabled={page.first}
                onClick={gotoFirst}
              >
                « First
              </button>
              <button
                className="px-3 py-1 rounded bg-gray-900 border border-gray-700 disabled:opacity-50"
                disabled={page.first}
                onClick={gotoPrev}
              >
                ‹ Prev
              </button>

              {pageWindow.map((i) => (
                <button
                  key={i}
                  onClick={() => goto(i)}
                  className={`px-3 py-1 rounded border ${
                    i === page.number ? 'bg-cyan-600 border-cyan-600' : 'bg-gray-900 border-gray-700'
                  }`}
                >
                  {i + 1}
                </button>
              ))}

              <button
                className="px-3 py-1 rounded bg-gray-900 border border-gray-700 disabled:opacity-50"
                disabled={page.last}
                onClick={gotoNext}
              >
                Next ›
              </button>
              <button
                className="px-3 py-1 rounded bg-gray-900 border border-gray-700 disabled:opacity-50"
                disabled={page.last}
                onClick={gotoLast}
              >
                Last »
              </button>

              <span className="ml-2 opacity-80">
                Page {page.number + 1} of {page.totalPages} • {page.totalElements} items
              </span>
            </div>
          )}
        </>
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
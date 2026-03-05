// src/pages/EventDetails.jsx
import React, { useEffect, useState, useContext, useMemo, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import { registerForEvent } from '../../services/eventService';
import { getToken } from '../../services/authService';

const API_BASE = process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080";

const buildQuery = (params) =>
  Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join("&");

const EventDetails = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  // ----- Event detail state -----
  const [event, setEvent] = useState(null);
  const [error, setError] = useState(null);

  // Register state
  const [registerLoading, setRegisterLoading] = useState(false);
  const [registerMessage, setRegisterMessage] = useState('');
  const [isRegistered, setIsRegistered] = useState(false);
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);
  const [registrationInfo, setRegistrationInfo] = useState(null);

  const DEFAULT_PAYMENT_AMOUNT_RUPEES = 499;

  // ----- Fetch the event (HOOK #1) -----
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch(`${API_BASE}/api/events/${encodeURIComponent(eventId)}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!cancelled) setEvent(data);
      } catch (e) {
        console.error('Error fetching event:', e);
        if (!cancelled) setError(e.message);
      }
    })();
    return () => { cancelled = true; };
  }, [eventId]);

  // ----- Pagination state for "More Events" -----
  const [pageIndex, setPageIndex] = useState(0);   // 0-based
  const [pageSize, setPageSize] = useState(2);
  const [searchName, setSearchName] = useState("");

  const [page, setPage] = useState({
    content: [],
    number: 0,
    size: 2,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });
  const [listLoading, setListLoading] = useState(false);
  const [listErr, setListErr] = useState("");
  const [noContent, setNoContent] = useState(false);

  // Build the list URL (HOOK #2)
  const listUrl = useMemo(() => {
    const qs = buildQuery({
      page: pageIndex,
      size: 2,
      eventName: searchName || undefined,
    });
    return `${API_BASE}/api/events/paginated?${qs}`;
  }, [pageIndex, searchName]);

  // Fetch the page (HOOK #3)
  const fetchPage = useCallback(async () => {
    setListLoading(true);
    setListErr("");
    setNoContent(false);
    try {
      const res = await fetch(listUrl, { headers: { "Content-Type": "application/json" } });

      if (res.status === 204) {
        setNoContent(true);
        setPage((p) => ({
          ...p,
          content: [],
          number: pageIndex,
          size: 2,
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
        size: data.size ?? pageSize,
        totalElements: data.totalElements ?? 0,
        totalPages: data.totalPages ?? 0,
        first: data.first ?? (pageIndex === 0),
        last: data.last ?? (pageIndex >= (data.totalPages ?? 1) - 1),
      });
    } catch (e) {
      console.error("Failed to fetch events:", e);
      setListErr(e?.message || "Failed to fetch events");
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
      setListLoading(false);
    }
  }, [listUrl, pageIndex, pageSize]);

  // Trigger fetch when listUrl changes (HOOK #4)
  useEffect(() => {
    fetchPage();
  }, [fetchPage]);

  // Reset to first page when search or size changes (HOOK #5)
  useEffect(() => {
    setPageIndex(0);
  }, [searchName]);

  // Page window (HOOK #6)
  const pageWindow = useMemo(() => {
    const total = page.totalPages || 0;
    const current = page.number || 0;
    const windowSize = 5;
    const half = Math.floor(windowSize / 2);
    const start = Math.max(0, Math.min(current - half, Math.max(0, total - windowSize)));
    const end = Math.max(0, Math.min(total, start + windowSize));
    return Array.from({ length: end - start }, (_, i) => start + i);
  }, [page.totalPages, page.number]);

  // ----- Early returns AFTER ALL HOOKS (safe) -----
  if (error) {
    return <div className="p-6 text-red-400">Failed to load: {error}</div>;
  }
  if (!event) {
    return <div className="flex justify-center items-center h-screen text-white">Loading...</div>;
  }

  // ----- Non-hook helpers / computations -----
  const formatDate = (d) => {
    if (!d) return '—';
    const dt = new Date(d);
    return isNaN(dt) ? d : dt.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  };

  const formatTime = (t) => {
    if (!t) return '—';
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
      const registration = await registerForEvent(eventId, user);
      setRegistrationInfo(registration);
      setIsRegistered(true);
      setShowSuccessPopup(true);
    } catch (e) {
      setRegisterMessage(e.message);
    } finally {
      setRegisterLoading(false);
    }
  };

  const handlePopupClose = async () => {
    try {
      const token = getToken();
      if (token && registrationInfo?.registrationId) {
        await fetch(`${API_BASE}/api/payments/pending`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            registrationId: registrationInfo.registrationId,
            amountRupees: DEFAULT_PAYMENT_AMOUNT_RUPEES,
          }),
        });
      }
    } catch (e) {
      console.error("Failed to mark pay-later status:", e);
    } finally {
      setShowSuccessPopup(false);
      navigate('/');
    }
  };

  const handleProceedToPayment = () => {
    setShowSuccessPopup(false);
    navigate('/payments', {
      state: {
        registrationId: registrationInfo?.registrationId ?? null,
        eventId: Number(eventId),
        amountRupees: DEFAULT_PAYMENT_AMOUNT_RUPEES,
      },
    });
  };

  // Safely compute full address only if event.address is present
  const a = event?.address;
  const hasAddress = !!a;
  const fullAddress = hasAddress
    ? [
        a.address ?? a.addressLine ?? a.street ?? '',
        a.state,
        a.country,
      ].filter(Boolean).join(', ')
      + (a.pincode || a.zip || a.postalCode ? ` - ${a.pincode ?? a.zip ?? a.postalCode}` : '')
    : '—';

  // Pagination handlers
  const gotoFirst = () => setPageIndex(0);
  const gotoPrev  = () => setPageIndex((p) => Math.max(0, p - 1));
  const gotoNext  = () => setPageIndex((p) => Math.min((page.totalPages || 1) - 1, p + 1));
  const gotoLast  = () => setPageIndex(Math.max(0, (page.totalPages || 1) - 1));
  const goto      = (i) => setPageIndex(i);

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

            {/* ---------- More Events (Paginated) ---------- */}
            <div className="bg-gray-800 p-6 rounded-lg shadow-lg mt-8">
              <div className="flex items-center justify-between flex-wrap gap-3 mb-4">
                <h2 className="text-2xl font-semibold">More Events</h2>
                <div className="flex items-center gap-3">
                  
                  <input
                    type="text"
                    value={searchName}
                    onChange={(e) => setSearchName(e.target.value)}
                    placeholder="Search by name"
                    className="px-3 py-2 rounded bg-gray-700 border border-gray-600 text-white"
                  />

                  <select
                    value={pageSize}
                    onChange={(e) => setPageSize(Number(e.target.value))}
                    className="px-3 py-2 rounded bg-gray-700 border border-gray-600 text-white"
                    title="Page size"
                  >
                    {[5, 10, 20, 50].map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* List */}
              {listLoading && <div className="text-cyan-300 mb-2">Loading events…</div>}
              {listErr && <div className="text-red-400 mb-2">{listErr}</div>}

              <div className="grid gap-3">
                {!listLoading && (noContent || (page.content || []).length === 0) && (
                  <div className="text-gray-300">No events found.</div>
                )}

                {(page.content || [])
                  // Optional: hide current event from this list
                  .filter(ev => String(ev.eventId ?? ev.id) !== String(eventId))
                  .map((ev) => (
                    <div
                      key={ev.eventId ?? ev.id}
                      className="bg-gray-900 border border-gray-700 rounded p-4 flex items-center justify-between"
                    >
                      <div>
                        <div className="font-bold text-lg">{ev.eventName}</div>
                        {/* Add more fields if available */}
                        {/* <div className="text-sm opacity-80">Date: {formatDate(ev.date)}</div> */}
                      </div>
                      <button
                        onClick={() => navigate(`/events/${ev.eventId ?? ev.id}`)}
                        className="px-3 py-2 rounded bg-cyan-600 hover:bg-cyan-700 text-white"
                      >
                        View
                      </button>
                    </div>
                  ))}
              </div>

              {/* Pagination bar */}
              {page.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 flex-wrap mt-4">
                  <button
                    className="pg-btn bg-gray-900 border border-gray-700 rounded px-3 py-1 disabled:opacity-50"
                    disabled={page.first}
                    onClick={gotoFirst}
                  >
                    « First
                  </button>
                  <button
                    className="pg-btn bg-gray-900 border border-gray-700 rounded px-3 py-1 disabled:opacity-50"
                    disabled={page.first}
                    onClick={gotoPrev}
                  >
                    ‹ Prev
                  </button>

                  {pageWindow.map((i) => (
                    <button
                      key={i}
                      onClick={() => goto(i)}
                      className={`pg-btn rounded px-3 py-1 border ${i === page.number ? 'bg-cyan-600 border-cyan-600' : 'bg-gray-900 border-gray-700'}`}
                    >
                      {i + 1}
                    </button>
                  ))}

                  <button
                    className="pg-btn bg-gray-900 border border-gray-700 rounded px-3 py-1 disabled:opacity-50"
                    disabled={page.last}
                    onClick={gotoNext}
                  >
                    Next ›
                  </button>
                  <button
                    className="pg-btn bg-gray-900 border border-gray-700 rounded px-3 py-1 disabled:opacity-50"
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
            </div>
            {/* ---------- /More Events (Paginated) ---------- */}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Success Popup */}
            {showSuccessPopup && (
              <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60">
                <div className="bg-white rounded-lg shadow-lg p-8 max-w-sm w-full mx-4">
                  <h2 className="text-2xl font-bold text-green-600 mb-4">Success!</h2>
                  <p className="text-gray-600 mb-6">You have successfully registered for the event.</p>
                  <button
                    onClick={handleProceedToPayment}
                    className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition duration-300"
                  >
                    Proceed to Payment
                  </button>
                  <button
                    onClick={handlePopupClose}
                    className="w-full mt-3 bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded-lg transition duration-300"
                  >
                    Later
                  </button>
                </div>
              </div>
            )}

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
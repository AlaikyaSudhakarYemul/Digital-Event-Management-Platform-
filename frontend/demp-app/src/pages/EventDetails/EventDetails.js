// src/pages/EventDetails.jsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

const EventDetails = () => {
  const { eventId } = useParams();
  const [event, setEvent] = useState(null);
  const [error, setError] = useState(null);

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

  if (error) return <div className="p-6 text-red-400">Failed to load: {error}</div>;
  if (!event) return <div>Loading...</div>;

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
    <div className="p-6 text-white bg-gray-800">
      <h1 className="text-3xl font-bold mb-4">{event.eventName}</h1>

      <p><strong>Date:</strong> {formatDate(event.date)}</p>
      <p><strong>Time:</strong> {formatTime(event.time)}</p> {/* 👈 Added time */}
      <p><strong>Event Type:</strong> {event.eventType ?? '—'}</p>

      {/* If address is null (e.g., VIRTUAL), show — instead of undefined */}
      <p><strong>Location:</strong> {hasAddress ? (a.address ?? a.addressLine ?? a.street ?? '—') : '—'}</p>
      <p><strong>Full Address:</strong> {fullAddress}</p>

      <p><strong>Description:</strong> {event.description ?? '—'}</p>

      {event.speakers?.length > 0 && (
        <div className="mt-4">
          <h2 className="text-xl font-semibold">Speaker</h2>
          <p><strong>Name:</strong> {event.speakers[0].name}</p>
          <p><strong>Bio:</strong> {event.speakers[0].bio}</p>
        </div>
      )}

      {event.image && (
        <img src={event.image} alt={event.eventName} className="mt-4 w-full max-w-md rounded" />
      )}
    </div>
  );
};

export default EventDetails;
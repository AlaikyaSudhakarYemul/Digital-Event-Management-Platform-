import React, { useEffect, useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8080';

const getUser = () => {
  try {
    const raw = localStorage.getItem('user');
    if (!raw) return null;
    const u = JSON.parse(raw);
    const id = u?.id ?? u?.userId ?? null;
    return id ? { ...u, id } : null;
  } catch {
    return null;
  }
};

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    if (Number.isNaN(d.getTime())) return val;
    return d.toLocaleString();
  } catch {
    return val;
  }
};

const formatOnlyDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    if (Number.isNaN(d.getTime())) return val;
    return d.toLocaleDateString();
  } catch {
    return val;
  }
};

const formatTime = (val) => {
  if (!val) return '-';
  try {
    if (typeof val === 'string') {
      const isoLike = /\d{4}-\d{2}-\d{2}T/;
      const timeOnly = /^\d{1,2}:\d{2}(:\d{2})?$/;
      if (isoLike.test(val)) {
        const d = new Date(val);
        if (!Number.isNaN(d.getTime())) {
          return d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', second: '2-digit' });
        }
      }
      if (timeOnly.test(val)) {
        const parts = val.split(':').map((p) => Number(p));
        const d = new Date();
        d.setHours(parts[0] || 0, parts[1] || 0, parts[2] || 0, 0);
        return d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', second: '2-digit' });
      }
    }
    const d = new Date(val);
    if (Number.isNaN(d.getTime())) return val;
    return d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', second: '2-digit' });
  } catch {
    return val;
  }
};


const parseLocalDateTimeString = (s) => {
  if (!s || typeof s !== 'string') return null;
  const m = s.trim().match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{1,2}):(\d{2})(?::(\d{2}))?/);
  if (!m) return null;
  const year = Number(m[1]);
  const month = Number(m[2]);
  const day = Number(m[3]);
  const hour = Number(m[4]);
  const minute = Number(m[5]);
  const second = Number(m[6] || 0);

  return new Date(year, month - 1, day, hour, minute, second);
};

const formatTimeFromDate = (date) => {
  if (!date || !(date instanceof Date)) return '-';
  return date.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', second: '2-digit' });
};

const getEventTime = (eventObj, ticketObj) => {
  
  const candidates = [];
  if (eventObj) {
    candidates.push(eventObj.time, eventObj.eventTime, eventObj.startTime, eventObj.date, eventObj.startDateTime, eventObj.eventDateTime);
  }
  if (ticketObj && ticketObj.event) {
    candidates.push(ticketObj.event.time, ticketObj.event.eventTime, ticketObj.event.startTime, ticketObj.event.date);
  }

  for (let candidate of candidates) {
    if (candidate == null) continue;
    if (typeof candidate === 'number' && Number.isFinite(candidate)) {
      const d = new Date();
      d.setHours(candidate, 0, 0, 0);
      return formatTimeFromDate(d);
    }
    if (typeof candidate === 'string') {
      const num = Number(candidate);
      if (!Number.isNaN(num) && String(candidate).trim().length <= 2) {
        const d = new Date();
        d.setHours(num, 0, 0, 0);
        return formatTimeFromDate(d);
      }
      
      const parsed = parseLocalDateTimeString(candidate);
      if (parsed) return formatTimeFromDate(parsed);
      
      const timeOnly = candidate.trim().match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?$/);
      if (timeOnly) {
        const h = Number(timeOnly[1]);
        const m = Number(timeOnly[2]);
        const s = Number(timeOnly[3] || 0);
        const d = new Date();
        d.setHours(h, m, s, 0);
        return formatTimeFromDate(d);
      }
    }
    
    if (candidate instanceof Date) {
      return formatTimeFromDate(candidate);
    }
  }

  return formatTime(eventObj?.date ?? ticketObj?.event?.date ?? ticketObj?.createdOn);
};

const formatAddress = (addr) => {
  if (!addr) return '-';
  
  const parts = [];
  if (addr.address) parts.push(addr.address);
  if (addr.addressLine) parts.push(addr.addressLine);
  if (addr.street) parts.push(addr.street);
  if (addr.city) parts.push(addr.city);
  if (addr.state) parts.push(addr.state);
  if (addr.postalCode) parts.push(addr.postalCode);
  if (addr.zip) parts.push(addr.zip);
  if (addr.country) parts.push(addr.country);
  return parts.length ? parts.join(', ') : '-';
};

const loadHtml2Canvas = () => new Promise((resolve, reject) => {
  if (typeof window === 'undefined') return reject(new Error('No window'));
  if (window.html2canvas) return resolve(window.html2canvas);
  const existing = document.querySelector('script[data-html2canvas]');
  if (existing) {
    existing.addEventListener('load', () => resolve(window.html2canvas));
    existing.addEventListener('error', () => reject(new Error('Failed to load html2canvas')));
    return;
  }
  const s = document.createElement('script');
  s.src = 'https://unpkg.com/html2canvas@1.4.1/dist/html2canvas.min.js';
  s.setAttribute('data-html2canvas', 'true');
  s.onload = () => resolve(window.html2canvas);
  s.onerror = () => reject(new Error('Failed to load html2canvas'));
  document.body.appendChild(s);
});


const buildTicketElement = (ticket, event) => {
  const wrapper = document.createElement('div');
  wrapper.style.position = 'absolute';
  wrapper.style.left = '-9999px';
  wrapper.style.top = '0';
  
  wrapper.innerHTML = `
    <div style="box-sizing:border-box; width:760px; padding:24px; border-radius:12px; background:#fff; box-shadow:0 8px 20px rgba(0,0,0,0.18); display:flex; gap:24px; font-family: Inter, system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial; color:#111827;">
      <div style="flex:1; text-align:left;">
        <h2 style="margin:0 0 8px; font-size:28px; font-weight:800;">${(event?.eventName ?? ticket?.event?.eventName ?? '-')}</h2>
        <div style="margin:8px 0; font-weight:700">Date: ${(formatOnlyDate(event?.date ?? ticket?.event?.date ?? ticket?.createdOn))}</div>
        <div style="margin:8px 0; font-weight:700">Time: ${(getEventTime(event, ticket))}</div>
        <div style="margin:8px 0; font-weight:700">Venue: ${(formatAddress(event?.address ?? ticket?.event?.address))}</div>
      </div>
      <div style="flex:1; text-align:left;">
        <div style="margin:0 0 12px; font-weight:700">Seat Type: ${ticket?.ticketType ?? '-'}</div>
        <div style="margin:0 0 12px; font-weight:700">Ticket ID: #${ticket?.ticketId ?? '-'}</div>
        <div style="margin:0 0 12px; font-weight:700">Payment Status: ${ticket?.isPaid ? 'Paid' : 'Not Paid'}</div>
      </div>
    </div>
  `;
  return wrapper;
};

const downloadCanvasAsPng = (canvas, filename) => {
  const link = document.createElement('a');
  link.download = filename || 'ticket.png';
  link.href = canvas.toDataURL('image/png');
  document.body.appendChild(link);
  link.click();
  link.remove();
};

const TicketDetails = () => {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [userData, setUserData] = useState(null);
  const [loadError, setLoadError] = useState('');

  const [selectedTicket, setSelectedTicket] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showTicketProfile, setShowTicketProfile] = useState(false);

  useEffect(() => {
    const u = getUser();
    if (!u) {
      setLoadError('User not found in this browser. Please sign in again.');
      setUserData(null);
      return;
    }
    setUserData({
      userName: u.userName ?? u.name ?? '',
      email: u.email ?? '',
      contactNo: u.contactNo ?? u.phone ?? '',
      id: u.id,
    });
  }, []);

  const hasUser = !!userData?.id;

  useEffect(() => {
    let cancelled = false;
    const fetchTickets = async () => {
      setLoading(true);
      setError('');
      try {
        const token = localStorage.getItem('auth_token');
        const res = await fetch(`${API_BASE}/api/tickets/all`, {
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        });
        if (!res.ok) throw new Error(`Failed to load tickets (HTTP ${res.status})`);
        const data = await res.json();
        if (cancelled) return;
        // If we have a signed-in user, filter tickets for that user. Otherwise show all.
        const list = Array.isArray(data) ? data : [];
        const filtered = hasUser ? list.filter((t) => Number(t.userId) === Number(userData.id)) : list;
        setTickets(filtered);
      } catch (err) {
        console.error('Error fetching tickets:', err);
        if (!cancelled) setError(err?.message || 'Failed to fetch tickets');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchTickets();
    return () => { cancelled = true; };
  }, [userData, hasUser]);

  // When a ticket is selected, fetch its event if not already nested
  useEffect(() => {
    let cancelled = false;
    const fetchEvent = async () => {
      if (!selectedTicket) return;
      // if nested event present, use it
      if (selectedTicket.event) {
        setSelectedEvent(selectedTicket.event);
        return;
      }
      const eventId = selectedTicket.eventId ?? null;
      if (!eventId) return;
      try {
        const token = localStorage.getItem('auth_token');
        const res = await fetch(`${API_BASE}/api/events/${encodeURIComponent(eventId)}`, {
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        });
        if (!res.ok) throw new Error('Failed to fetch event details');
        const ev = await res.json();
        if (!cancelled) setSelectedEvent(ev);
      } catch (err) {
        console.warn('Could not load event details for ticket modal:', err);
      }
    };

    fetchEvent();
    return () => { cancelled = true; };
  }, [selectedTicket]);

  const handleDownload = async (t) => {
    try {
      let ev = t.event ?? null;
      if (!ev && t.eventId) {
        const token = localStorage.getItem('auth_token');
        const res = await fetch(`${API_BASE}/api/events/${encodeURIComponent(t.eventId)}`, {
          headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        });
        if (res.ok) ev = await res.json();
      }
      const el = buildTicketElement(t, ev);
      document.body.appendChild(el);
      await loadHtml2Canvas();
      const canvas = await window.html2canvas(el, { useCORS: true, scale: 2 });
      downloadCanvasAsPng(canvas, `ticket-${t.ticketId || 'download'}.png`);
      el.remove();
    } catch (err) {
      console.error('Download failed', err);
      alert('Failed to download ticket image');
    }
  };

  const closeModal = () => {
    setShowTicketProfile(false);
    setSelectedTicket(null);
    setSelectedEvent(null);
  };

  return (
    <div className="dashboard-card">
      <div className="card-header">My Tickets</div>
      <div className="card-body">
        {loading && <div className="info-banner">Loading tickets…</div>}
        {error && <div className="error-banner">{error}</div>}
        {loadError && <div className="error-banner">{loadError}</div>}

        {!loading && !error && tickets.length === 0 && (
          <p>No tickets found.</p>
        )}

        {!loading && tickets.length > 0 && (
          <div className="table-wrapper">
            <table className="events-table">
              <thead>
                <tr>
                  <th>S.No.</th>
                  <th>Ticket ID</th>
                  <th>Type</th>
                  <th>Price</th>
                  <th>Created</th>
                  <th>View</th>
                  <th>Download</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map((t, idx) => (
                  <tr key={t.ticketId ?? `${idx}`}>
                    <td>{idx + 1}</td>
                    <td>{t.ticketId ?? '-'}</td>
                    <td>{t.ticketType ?? '-'}</td>
                    <td>{t.price != null ? String(t.price) : '-'}</td>
                    <td>{formatDate(t.creationTime ?? t.createdOn)}</td>
                    <td>
                    <div className="flex justify-center">
                      <button
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-md shadow mx-auto"
                        onClick={() => { setSelectedTicket(t); setShowTicketProfile(true); }}
                        disabled={!hasUser}
                        title={!hasUser ? 'Sign in to view profile' : 'Open ticket'}
                        type="button"
                      >
                        View
                      </button>
                      </div>
                    </td>
                    <td>
                        <div className="flex justify-center">
                            <button className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-md shadow" onClick={() => handleDownload(t)}>
                            Download
                            </button>
                        </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {showTicketProfile && selectedTicket && (
          <div className="ticket-backdrop flex items-center justify-center" onClick={closeModal}>
            <div className="ticket-card relative flex items-center justify-center max-w-2xl w-full p-4" onClick={(e) => e.stopPropagation()}>
              <button className="ticket-close absolute top-3 right-3 rounded-full bg-black text-white w-7 h-7 flex items-center justify-center text-sm" onClick={closeModal} aria-label="Close ticket">
                x
              </button>

              <div className="ticket-left flex-1 text-center px-4">
                <h2 className="ticket-title text-2xl font-bold mb-2">{selectedEvent?.eventName ?? selectedTicket?.event?.eventName ?? '-'}</h2>
                <p className="ticket-row mb-1 text-sm"><strong>Date:</strong> {formatOnlyDate(selectedEvent?.date ?? selectedTicket?.event?.date ?? selectedTicket?.createdOn)}</p>
                <p className="ticket-row mb-1 text-sm"><strong>Time:</strong> {getEventTime(selectedEvent, selectedTicket)}</p>
                <p className="ticket-row text-sm"><strong>Venue:</strong> {formatAddress(selectedEvent?.address ?? selectedTicket?.event?.address)}</p>
              </div>
              <div className="ticket-divider" aria-hidden="true" />
              <div className="ticket-right flex-1 text-center px-4">
                <p className="ticket-row mb-2 text-sm"><strong>Seat Type: </strong>{selectedTicket?.ticketType ?? '-'}</p>
                <p className="ticket-row mb-2 text-sm"><strong>Ticket ID: </strong> #{selectedTicket?.ticketId ?? '-'}</p>
                <p className="ticket-row text-sm"><strong>Payment Status: </strong>{selectedTicket?.isPaid ? 'Paid' : 'Not Paid'}</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TicketDetails;

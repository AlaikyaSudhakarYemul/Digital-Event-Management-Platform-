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

const formatAddress = (addr) => {
  if (!addr) return '-';
  // Normalize possible address fields and join non-empty parts
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
                      <button
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-md shadow"
                        onClick={() => { setSelectedTicket(t); setShowTicketProfile(true); }}
                        disabled={!hasUser}
                        title={!hasUser ? 'Sign in to view profile' : 'Open ticket'}
                        type="button"
                      >
                        View
                      </button>
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
                <p className="ticket-row mb-1 text-sm"><strong>Date:</strong> {formatDate(selectedEvent?.date ?? selectedTicket?.event?.date)}</p>
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

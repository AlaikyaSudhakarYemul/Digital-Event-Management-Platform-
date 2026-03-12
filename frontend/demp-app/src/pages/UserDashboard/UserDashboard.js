import React, { useState, useEffect, useMemo, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import "./UserDashboard.css";

const getUser = () => {
  try {
    const raw = localStorage.getItem("user");
    if (!raw) return null;
    const u = JSON.parse(raw);
    const id = u?.id ?? u?.userId ?? null;
    return id ? { ...u, id } : null;
  } catch {
    return null;
  }
};

const toJSDateFromLocalDate = (val) => {
  if (!val) return null;
  try {
    if (Array.isArray(val) && val.length >= 3) {
      const [y, m, d] = val;
      return new Date(y, (m ?? 1) - 1, d ?? 1);
    }
    const d = new Date(val);
    return Number.isNaN(d.getTime()) ? null : d;
  } catch {
    return null;
  }
};

const formatDate = (val) => {
  const d = toJSDateFromLocalDate(val);
  if (!d) return "-";
  return d.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
  });
};

const formatTime = (val) => {
  if (!val) return "-";
  try {
    if (Array.isArray(val) && val.length >= 2) {
      const [h, m] = val;
      return String(h).padStart(2, "0") + ":" + String(m).padStart(2, "0");
    }
    const parts = val.toString().split(":");
    const h = parts[0] ?? "00";
    const m = parts[1] ?? "00";
    return String(h).padStart(2, "0") + ":" + String(m).padStart(2, "0");
  } catch {
    return val.toString();
  }
};


const normalizeRegistration = (reg) => ({
  registrationId: reg.registrationId,
  eventName: reg?.event?.eventName ?? "-",
  date: reg?.event?.date ?? null,
  time: reg?.event?.time ?? null,
  location: [
    reg?.event?.address?.address,
    reg?.event?.address?.state,
    reg?.event?.address?.pincode,
    reg?.event?.address?.country,
  ]
    .filter(Boolean)
    .join(", "),
});

const normalizeCopiedEvent = (event) => ({
  copiedEventId: event?.copiedEventId,
  sourceEventId: event?.eventId ?? event?.event?.eventId ?? null,
  eventName: event?.eventName ?? "-",
  date: event?.date ?? null,
  time: event?.time ?? null,
  eventType: event?.eventType ?? "-",
  maxAttendees: event?.maxAttendees ?? "-",
  location: [
    event?.address?.address,
    event?.address?.state,
    event?.address?.pincode,
    event?.address?.country,
  ]
    .filter(Boolean)
    .join(", "),
  userId: event?.user?.userId ?? event?.user?.id ?? null,
  raw: event,
});


const isUpcoming = (isoDate) => {
  if (!isoDate) return false;
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const d = new Date(isoDate);
    d.setHours(0, 0, 0, 0);
    return d >= today;
  } catch {
    return false;
  }
};

/** ---- Component ---- **/

const API_BASE =
  process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080";

const UserDashboard = () => {
  const navigate = useNavigate();
  const [showProfile, setShowProfile] = useState(false);
  const [userData, setUserData] = useState(null);
  const [registeredEvents, setRegisteredEvents] = useState([]); 
  const [copiedEvents, setCopiedEvents] = useState([]);
  const [activeTab, setActiveTab] = useState("home");
  const [showTicketProfile,setTicketProfile] = useState(false);

  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [copyLoading, setCopyLoading] = useState(false);
  const [copyLoadError, setCopyLoadError] = useState("");
  const [editingCopiedEvent, setEditingCopiedEvent] = useState(null);
  const [editCopiedForm, setEditCopiedForm] = useState({
    eventName: "",
    description: "",
    date: "",
    time: "",
    eventType: "IN_PERSON",
    maxAttendees: "",
  });
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState("");

  const handleLogout = useCallback(() => {
  try {
    
    ['user','userToken','token','adminToken','access_token','refresh_token']
      .forEach(k => localStorage.removeItem(k));
    sessionStorage.clear();
    document.cookie.split(';').forEach(c => {
      const [name] = c.split('=');
      if (name) {
        document.cookie = `${name.trim()}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
      }
    });
  } catch (e) {
    console.warn('Logout cleanup warning:', e);
  }

  window.location.replace('/'); 
}, []);

  const goHome = useCallback(() => {
    navigate("/");
  }, [navigate]);


  useEffect(() => {
    const u = getUser();
    if (!u) {
      setLoadError(
        "User not found in this browser. Please sign in again so we can load your records."
      );
      setUserData(null);
      return;
    }
    setUserData({
      userName: u.userName ?? u.name ?? "",
      email: u.email ?? "",
      contactNo: u.contactNo ?? u.phone ?? "",
      id: u.id,
    });
  }, []);


  useEffect(() => {
    if (!userData?.id) return;
    let cancelled = false;

    const fetchRegisteredEvents = async () => {
      setLoading(true);
      setLoadError("");
      try {
        const url = `${API_BASE}/api/registrations/user/${encodeURIComponent(
          userData.id
        )}`;
        const token = localStorage.getItem("auth_token");

        const response = await fetch(url, {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        });

        if (!response.ok) {
          const msg = `Failed to fetch registrations (HTTP ${response.status})`;
          console.error(msg);
          if (!cancelled) setLoadError(msg);
          return;
        }

        const raw = await response.json();
        const arr = Array.isArray(raw) ? raw : raw ? [raw] : [];
        const normalized = arr.map(normalizeRegistration).filter(Boolean);

        if (!cancelled) {
          setRegisteredEvents(normalized);
        }
      } catch (err) {
        console.error("Error fetching registered events:", err);
        if (!cancelled)
          setLoadError(
            "Could not load registrations. Please try again or check your network / backend."
          );
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchRegisteredEvents();
    return () => {
      cancelled = true;
    };
  }, [userData?.id]);

  const fetchCopiedEvents = useCallback(async () => {
    if (!userData?.id) return;
    setCopyLoading(true);
    setCopyLoadError("");
    try {
      const url = `${API_BASE}/api/copied/events/all`;
      const token = localStorage.getItem("auth_token");
      const response = await fetch(url, {
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      });

      if (!response.ok) {
        const msg = `Failed to fetch copied events (HTTP ${response.status})`;
        setCopyLoadError(msg);
        return;
      }

      const raw = await response.json();
      const arr = Array.isArray(raw) ? raw : raw ? [raw] : [];
      const normalized = arr.map(normalizeCopiedEvent).filter(Boolean);
      const mine = normalized.filter((e) =>
        e.userId ? Number(e.userId) === Number(userData.id) : true
      );
      setCopiedEvents(mine);
    } catch (err) {
      console.error("Error fetching copied events:", err);
      setCopyLoadError("Could not load copied events. Please try again.");
    } finally {
      setCopyLoading(false);
    }
  }, [userData?.id]);

  useEffect(() => {
    fetchCopiedEvents();
  }, [fetchCopiedEvents]);

  const openEditCopiedEvent = (eventRow) => {
    setEditingCopiedEvent(eventRow);
    setEditError("");
    setEditCopiedForm({
      eventName: eventRow?.raw?.eventName || "",
      description: eventRow?.raw?.description || "",
      date: eventRow?.raw?.date || "",
      time: eventRow?.raw?.time || "",
      eventType: eventRow?.raw?.eventType || "IN_PERSON",
      maxAttendees: String(eventRow?.raw?.maxAttendees ?? ""),
    });
  };

  const handleEditCopiedFieldChange = (e) => {
    const { name, value } = e.target;
    setEditCopiedForm((prev) => ({ ...prev, [name]: value }));
  };

  const closeEditCopiedEvent = () => {
    if (editSaving) return;
    setEditingCopiedEvent(null);
    setEditError("");
  };

  const saveCopiedEventEdit = async () => {
    if (!editingCopiedEvent?.copiedEventId) return;
    setEditError("");

    if (!editCopiedForm.eventName.trim()) {
      setEditError("Event name is required.");
      return;
    }
    if (!editCopiedForm.date) {
      setEditError("Date is required.");
      return;
    }
    if (!editCopiedForm.time) {
      setEditError("Time is required.");
      return;
    }

    const max = Number(editCopiedForm.maxAttendees);
    if (!Number.isFinite(max) || max < 10 || max > 500) {
      setEditError("Max attendees must be between 10 and 500.");
      return;
    }

    const sourceEventId =
      editingCopiedEvent?.sourceEventId ?? editingCopiedEvent?.raw?.eventId ?? null;
    if (!sourceEventId) {
      setEditError("Source event id is missing for this copied event.");
      return;
    }

    const base = editingCopiedEvent.raw || {};
    const payload = {
      ...base,
      eventId: sourceEventId,
      eventName: editCopiedForm.eventName.trim(),
      description: editCopiedForm.description,
      date: editCopiedForm.date,
      time: editCopiedForm.time,
      eventType: editCopiedForm.eventType,
      maxAttendees: max,
    };

    if (payload.eventType === "VIRTUAL") {
      payload.address = null;
    }

    try {
      setEditSaving(true);
      const token = localStorage.getItem("auth_token");
      const response = await fetch(
        `${API_BASE}/api/copied/events/${editingCopiedEvent.copiedEventId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          body: JSON.stringify(payload),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        setEditError(errorText || "Failed to update copied event.");
        return;
      }

      closeEditCopiedEvent();
      await fetchCopiedEvents();
    } catch (err) {
      console.error("Error updating copied event:", err);
      setEditError("Could not update copied event. Please try again.");
    } finally {
      setEditSaving(false);
    }
  };

  const hasUser = !!userData?.id;


  const upcomingMyEvents = useMemo(() => {
    return (registeredEvents || [])
      .filter((r) => isUpcoming(r.date))
      .sort((a, b) => new Date(a.date) - new Date(b.date));
  }, [registeredEvents]);

  return (
    <div className="dashboard-container">

      <aside className="sidebar sidebar--with-bottom">
        <div>
          <div className="sidebar-header">User Dashboard</div>
          <ul className="sidebar-menu">
            <li
              className={activeTab === "home" ? "active" : ""}
              onClick={() => setActiveTab("home")}
            >
              Home
            </li>
            <li
              className={activeTab === "myEvents" ? "active" : ""}
              onClick={() => setActiveTab("myEvents")}
            >
              My Events
            </li>
            <li
              className={activeTab === "tickets" ? "active" : ""}
              onClick={() => setActiveTab("tickets")}
            >
              My Tickets
            </li>
            <li
              className={activeTab === "copiedEvents" ? "active" : ""}
              onClick={() => setActiveTab("copiedEvents")}
            >
              My Copied Events
            </li>
            <li
              className={activeTab === "settings" ? "active" : ""}
              onClick={() => setActiveTab("settings")}
            >
              Settings
            </li>
          </ul>
        </div>


        <div className="sidebar-bottom no-sep">
          <button
            type="button"
            className="home-btn"
            onClick={goHome}
            title="Go to Home"
          >
            Go to Home
          </button>

          <button
            type="button"
            className="logout-btn"
            onClick={handleLogout}  
            title="Logout and go to Home"
          >
            Logout
          </button>
        </div>
      </aside>

      <main className="dashboard-main">
        <header className="dashboard-header">
          <div>
            <div className="user-name">{userData?.userName || "-"}</div>
            <div className="user-email">{userData?.email || "-"}</div>
          </div>
          <button
            className="profile-btn"
            onClick={() => setShowProfile(true)}
            disabled={!hasUser}
            title={!hasUser ? "Sign in to view profile" : "Open profile"}
          >
            Profile
          </button>
        </header>

        <section className="dashboard-content">

          {loading && (
            <div className="info-banner">Loading your registrations…</div>
          )}
          {loadError && (
            <div className="error-banner">
              {loadError}
              {!hasUser ? (
                <>
                  {" "}
                  <span className="hint">
                    (Expected localStorage key: "user" with fields including
                    "id" or "userId")
                  </span>
                </>
              ) : null}
            </div>
          )}

          {activeTab === "home" && (
            <>
              <div className="dashboard-cards">

                <div className="dashboard-card" style={{ padding: 0, textAlign: "left" }}>
                  <div className="card-header">Upcoming Events</div>
                  <div className="card-body">
                    {!hasUser && (
                      <p className="muted">
                        Please sign in again—user ID missing in localStorage.
                      </p>
                    )}

                    {hasUser && (
                      <>
                        {upcomingMyEvents.length > 0 ? (
                          <ol className="upcoming-inline">
                            {upcomingMyEvents.slice(0, 5).map((e, idx) => {
                              const dateStr = formatDate(e.date);
                              const timeStr = e.time ? formatTime(e.time) : "";
                              const parts = [dateStr, timeStr].filter(Boolean);
                              const rightSide = parts.join(" — ");
                              const fullText = `${idx + 1}) ${e.eventName} — ${rightSide}`;

                              return (
                                <li
                                  key={`${e.eventName}-${e.date}-${idx}`}
                                  className="upcoming-inline__item"
                                  title={fullText}
                                  tabIndex={0}
                                >
                                  <span className="upcoming-inline__num">{idx + 1}</span>
                                  <span className="upcoming-inline__title">{e.eventName}</span>
                                  <span className="upcoming-inline__sep"> — </span>
                                  <span className="upcoming-inline__meta">{rightSide}</span>
                                </li>
                              );
                            })}
                          </ol>
                        ) : !loading ? (
                          <p>No upcoming events in your registrations.</p>
                        ) : null}
                      </>
                    )}
                  </div>
                </div>
              </div>
            </>
          )}

          {activeTab === "myEvents" && (
            <div className="dashboard-card">
              <div className="card-header">My Registered Events</div>

              <div className="card-body">
                {!hasUser && (
                  <p className="muted">
                    Please sign in again—user ID missing in localStorage.
                  </p>
                )}

                {hasUser && (
                  <>
                    {registeredEvents.length > 0 ? (
                      <div className="table-wrapper">
                        <table className="events-table">
                          <thead>
                            <tr>
                              <th>S.No.</th>
                              <th>Event Name</th>
                              <th>Date</th>
                              <th>Time</th>
                              <th>Location</th>
                            </tr>
                          </thead>
                          <tbody>
                            {registeredEvents.map((r, idx) => (
                              <tr key={r.registrationId ?? `${r.eventId}-${idx}`}>
                                <td>{idx + 1}</td>
                                <td>{r.eventName}</td>
                                <td>{formatDate(r.date)}</td>
                                <td>{r.time ? formatTime(r.time) : "-"}</td>
                                <td className="location-cell">{r.location || "-"}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : !loading ? (
                      <p>No events registered yet.</p>
                    ) : null}
                  </>
                )}
              </div>
            </div>
          )}
            {/*TICKETS*/}
          {activeTab === "tickets" && (
            <div className="dashboard-card">
              <h4>My Tickets</h4>
              <button
              className="view-btn"
              onClick={() => setTicketProfile(true)}
              disabled={!hasUser}
              title={!hasUser ? "Sign in to view profile" : "Open ticket profile"}>
              View
            </button>
            </div>
          )}

          {activeTab === "copiedEvents" && (
            <div className="dashboard-card">
              <div className="card-header">Copied Events</div>
              <div className="card-body">
                {!hasUser && (
                  <p className="muted">
                    Please sign in again-user ID missing in localStorage.
                  </p>
                )}

                {copyLoading && <p>Loading copied events...</p>}
                {copyLoadError && <p className="muted">{copyLoadError}</p>}

                {hasUser && !copyLoading && !copyLoadError && (
                  <>
                    {copiedEvents.length > 0 ? (
                      <div className="table-wrapper">
                        <table className="events-table">
                          <thead>
                            <tr>
                              <th>S.No.</th>
                              <th>Source Event ID</th>
                              <th>Copied Event Name</th>
                              <th>Schedule</th>
                              <th>Details</th>
                              <th>Location</th>
                              <th>Edit</th>
                            </tr>
                          </thead>
                          <tbody>
                            {copiedEvents.map((e, idx) => (
                              <tr key={e.copiedEventId ?? `${e.eventName}-${idx}`}>
                                <td>{idx + 1}</td>
                                <td>{e.sourceEventId ?? "-"}</td>
                                <td>{e.eventName}</td>
                                <td>
                                  {formatDate(e.date)}
                                  <br />
                                  <span className="muted">{e.time ? formatTime(e.time) : "-"}</span>
                                </td>
                                <td>
                                  {e.eventType || "-"}
                                  <br />
                                  <span className="muted">Max: {e.maxAttendees ?? "-"}</span>
                                </td>
                                <td className="location-cell">{e.location || "-"}</td>
                                <td>
                                  <button
                                    type="button"
                                    className="table-edit-btn"
                                    onClick={() => openEditCopiedEvent(e)}
                                  >
                                    Edit
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <p>No copied events found.</p>
                    )}
                  </>
                )}
              </div>
            </div>
          )}

          {activeTab === "settings" && (
            <div className="dashboard-card">
              <h4>Settings</h4>
              <p>[Settings content goes here]</p>
            </div>
          )}
        </section>

        {showProfile && (
          <div className="profile-modal" onClick={() => setShowProfile(false)}>
            <div
              className="profile-modal-content"
              onClick={(e) => e.stopPropagation()}
            >
              <h3>User Profile</h3>
              <p>
                <strong>Name:</strong> {userData?.userName || "-"}
              </p>
              <p>
                <strong>Email:</strong> {userData?.email || "-"}
              </p>
              <p>
                <strong>Contact No:</strong> {userData?.contactNo || "-"}
              </p>
              <button className="close-btn" onClick={() => setShowProfile(false)}>
                Close
              </button>
            </div>
          </div>
        )}

        {editingCopiedEvent && (
          <div className="profile-modal" onClick={closeEditCopiedEvent}>
            <div className="profile-modal-content edit-modal" onClick={(e) => e.stopPropagation()}>
              <h3>Edit Copied Event</h3>

              <div className="edit-grid">
                <label>
                  Event Name
                  <input
                    name="eventName"
                    value={editCopiedForm.eventName}
                    onChange={handleEditCopiedFieldChange}
                  />
                </label>

                <label>
                  Description
                  <input
                    name="description"
                    value={editCopiedForm.description}
                    onChange={handleEditCopiedFieldChange}
                  />
                </label>

                <label>
                  Date
                  <input
                    type="date"
                    name="date"
                    value={editCopiedForm.date}
                    onChange={handleEditCopiedFieldChange}
                  />
                </label>

                <label>
                  Time
                  <input
                    type="time"
                    name="time"
                    value={editCopiedForm.time}
                    onChange={handleEditCopiedFieldChange}
                  />
                </label>

                <label>
                  Event Type
                  <select
                    name="eventType"
                    value={editCopiedForm.eventType}
                    onChange={handleEditCopiedFieldChange}
                  >
                    <option value="IN_PERSON">IN_PERSON</option>
                    <option value="VIRTUAL">VIRTUAL</option>
                    <option value="HYBRID">HYBRID</option>
                  </select>
                </label>

                <label>
                  Max Attendees
                  <input
                    type="number"
                    name="maxAttendees"
                    min="10"
                    max="500"
                    value={editCopiedForm.maxAttendees}
                    onChange={handleEditCopiedFieldChange}
                  />
                </label>
              </div>

              {editError && <p className="muted">{editError}</p>}

              <div className="edit-actions">
                <button className="edit-action-btn" onClick={closeEditCopiedEvent} disabled={editSaving}>
                  Cancel
                </button>
                <button className="edit-action-btn" onClick={saveCopiedEventEdit} disabled={editSaving}>
                  {editSaving ? "Saving..." : "Update"}
                </button>
              </div>
            </div>
          </div>
        )}

{showTicketProfile && (
  <div className="ticket-backdrop" onClick={() => setTicketProfile(false)}>
    <div className="ticket-card" onClick={(e) => e.stopPropagation()}>   
      <button className="ticket-close" onClick={() => setTicketProfile(false)} aria-label="Close ticket">
        x
      </button>
  
      <div className="ticket-left">
        <h2 className="ticket-title">Event Name</h2>
        <p className="ticket-row"><strong>Date:</strong> 12 March 2026</p>
        <p className="ticket-row"><strong>Venue:</strong> Hyderabad</p>
      </div>
      <div className="ticket-divider" aria-hidden="true" />
      <div className="ticket-right">
        <p className="ticket-row"><strong>Seat Type: </strong>PREMIUM</p>
        <p className="ticket-row"><strong>Ticket ID: </strong> #12345</p>
        <p className="ticket-row"><strong>Payment Status: </strong>Not Paid</p>
      </div>
    </div>
  </div>
)}
      </main>
    </div>
  );
};

export default UserDashboard;

import React, { useState, useEffect, useMemo, useCallback } from "react";
import TicketDetails from '../../components/TicketDetails/TicketDetails';
import { useNavigate } from "react-router-dom";
import {
  fetchUserProfile,
  updateUserContactNo,
  updateUserPassword,
} from "../../services/authService";
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

const maskMobile = (val) => {
  const digits = (val || "").toString().replace(/\D/g, "");
  if (digits.length < 2) return "Hidden";
  return `*******${digits.slice(-2)}`;
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
  const [activeTab, setActiveTab] = useState("home");
  const [isEditingMobile, setIsEditingMobile] = useState(false);
  const [mobileInput, setMobileInput] = useState("");
  const [contactActionMessage, setContactActionMessage] = useState("");
  const [isSavingContact, setIsSavingContact] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [passwordMessage, setPasswordMessage] = useState("");
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [showPasswordForm, setShowPasswordForm] = useState(false);

  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  const handleLogout = useCallback(() => {
  try {
    
    ['user','userToken','token','authToken','auth_token','adminToken','access_token','refresh_token']
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
    let isCancelled = false;

    const loadUserProfile = async () => {
      try {
        const profile = await fetchUserProfile(userData.id);
        if (isCancelled || !profile) return;
        const resolved = {
          userName: profile.userName ?? userData.userName ?? "",
          email: profile.email ?? userData.email ?? "",
          contactNo: profile.contactNo ?? userData.contactNo ?? "",
          id: profile.userId ?? profile.id ?? userData.id,
        };
        setUserData(resolved);
        setMobileInput(resolved.contactNo ?? "");

        const localUser = getUser();
        if (localUser) {
          localStorage.setItem(
            "user",
            JSON.stringify({
              ...localUser,
              contactNo: resolved.contactNo,
              id: resolved.id,
              userId: resolved.id,
            })
          );
        }
      } catch (error) {
        if (!isCancelled) {
          console.error("Failed to load user profile", error);
        }
      }
    };

    loadUserProfile();
    return () => {
      isCancelled = true;
    };
  }, [userData?.id]);


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

  const hasUser = !!userData?.id;

  const openProfileModal = useCallback(() => {
    setShowProfile(true);
  }, []);

  const handleContactUpdate = useCallback(async () => {
    const cleaned = (mobileInput || "").trim();
    if (!/^\d{10}$/.test(cleaned)) {
      setContactActionMessage("Mobile number must be exactly 10 digits.");
      return;
    }

    if (!userData?.id) {
      setContactActionMessage("User session not found. Please login again.");
      return;
    }

    setIsSavingContact(true);
    setContactActionMessage("");
    try {
      const updated = await updateUserContactNo(userData.id, cleaned);
      const nextData = {
        ...userData,
        contactNo: updated?.contactNo ?? cleaned,
      };
      setUserData(nextData);
      setMobileInput(nextData.contactNo ?? "");
      setIsEditingMobile(false);
      setContactActionMessage("Mobile number updated successfully.");

      const localUser = getUser();
      if (localUser) {
        localStorage.setItem(
          "user",
          JSON.stringify({ ...localUser, contactNo: nextData.contactNo })
        );
      }
    } catch (error) {
      setContactActionMessage(error?.message || "Unable to update mobile number.");
    } finally {
      setIsSavingContact(false);
    }
  }, [mobileInput, userData]);

  const handlePasswordChange = useCallback(async () => {
    const currentPassword = passwordForm.currentPassword.trim();
    const newPassword = passwordForm.newPassword.trim();
    const confirmPassword = passwordForm.confirmPassword.trim();

    if (!currentPassword) {
      setPasswordMessage("Current password is required.");
      return;
    }
    if (newPassword.length < 6) {
      setPasswordMessage("New password must be at least 6 characters.");
      return;
    }
    if (!confirmPassword) {
      setPasswordMessage("Confirm password is required.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordMessage("New password and confirm password must match.");
      return;
    }
    if (!userData?.id) {
      setPasswordMessage("User session not found. Please login again.");
      return;
    }

    setIsChangingPassword(true);
    setPasswordMessage("");
    try {
      await updateUserPassword(userData.id, currentPassword, newPassword);
      setPasswordMessage("Password changed. Logging you out...");
      setTimeout(() => {
        handleLogout();
      }, 700);
    } catch (error) {
      setPasswordMessage(error?.message || "Unable to change password.");
    } finally {
      setIsChangingPassword(false);
    }
  }, [passwordForm, userData?.id, handleLogout]);


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
            onClick={openProfileModal}
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
                                <td>{r.location || "-"}</td>
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
            <TicketDetails />
          )}

          {activeTab === "settings" && (
            <div className="dashboard-card">
              <div className="card-header">Settings</div>
              <div className="card-body settings-panel">
                <div className="settings-grid">
                  <div className="settings-section">
                    <h4 className="profile-subtitle">Mobile Number</h4>
                    <div className="profile-field-group">
                      {/* <label htmlFor="settingsMobileInput"><strong>Contact</strong></label> */}
                      {isEditingMobile ? (
                        <div className="profile-row">
                          <input
                            id="settingsMobileInput"
                            className="profile-input compact-input"
                            value={mobileInput}
                            maxLength={10}
                            onChange={(e) => setMobileInput(e.target.value.replace(/\D/g, ""))}
                            placeholder="Enter 10 digit mobile"
                          />
                          <button
                            type="button"
                            className="profile-action-btn"
                            onClick={handleContactUpdate}
                            disabled={isSavingContact}
                          >
                            {isSavingContact ? "Saving..." : "Save"}
                          </button>
                          <button
                            type="button"
                            className="profile-action-btn muted-btn"
                            onClick={() => {
                              setIsEditingMobile(false);
                              setMobileInput(userData?.contactNo ?? "");
                              setContactActionMessage("");
                            }}
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <div className="profile-row">
                          <span>{maskMobile(userData?.contactNo)}</span>
                          <button
                            type="button"
                            className="profile-action-btn small-password-btn"
                            onClick={() => {
                              setIsEditingMobile(true);
                              setContactActionMessage("");
                            }}
                            disabled={!hasUser}
                          >
                            Edit Mobile
                          </button>
                        </div>
                      )}
                      {contactActionMessage && <p className="profile-message">{contactActionMessage}</p>}
                    </div>
                  </div>

                  <div className="settings-section">
                    <h4 className="profile-subtitle">Change Password</h4>
                    <div className="profile-field-group">
                      {!showPasswordForm ? (
                        <button
                          type="button"
                          className="profile-action-btn small-password-btn"
                          onClick={() => {
                            setShowPasswordForm(true);
                            setPasswordMessage("");
                          }}
                          disabled={!hasUser}
                        >
                          Change Password
                        </button>
                      ) : (
                        <div className="compact-password-form">
                          <input
                            type="password"
                            className="profile-input compact-input"
                            placeholder="Current password"
                            value={passwordForm.currentPassword}
                            onChange={(e) => setPasswordForm((prev) => ({ ...prev, currentPassword: e.target.value }))}
                            disabled={!hasUser}
                          />
                          <input
                            type="password"
                            className="profile-input compact-input"
                            placeholder="New password"
                            value={passwordForm.newPassword}
                            onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))}
                            disabled={!hasUser}
                          />
                          <input
                            type="password"
                            className="profile-input compact-input"
                            placeholder="Confirm password"
                            value={passwordForm.confirmPassword}
                            onChange={(e) => setPasswordForm((prev) => ({ ...prev, confirmPassword: e.target.value }))}
                            disabled={!hasUser}
                          />
                          <div className="profile-row">
                            <button
                              type="button"
                              className="profile-action-btn small-password-btn"
                              onClick={handlePasswordChange}
                              disabled={isChangingPassword || !hasUser}
                            >
                              {isChangingPassword ? "Updating..." : "Change Password"}
                            </button>
                            <button
                              type="button"
                              className="profile-action-btn muted-btn small-password-btn"
                              onClick={() => {
                                setShowPasswordForm(false);
                                setPasswordMessage("");
                                setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
                              }}
                            >
                              Cancel
                            </button>
                          </div>
                        </div>
                      )}
                      {passwordMessage && <p className="profile-message">{passwordMessage}</p>}
                    </div>
                  </div>
                </div>
              </div>
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
                <strong>Mobile:</strong> {userData?.contactNo || "-"}
              </p>

              <button className="close-btn" onClick={() => setShowProfile(false)}>
                Close
              </button>
            </div>
          </div>
        )}

      </main>
    </div>
  );
};

export default UserDashboard;

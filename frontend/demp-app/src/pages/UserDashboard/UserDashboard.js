import React, { useState, useEffect } from "react";
import "./UserDashboard.css";

// Helper to get user info from localStorage
const getUser = () => {
  const user = localStorage.getItem("user");
  return user
    ? JSON.parse(user)
    : {
        userName: "",
        email: "",
        contactNo: "",
      };
};


// const toJSDateFromLocalDate = (val) => {
//   if (!val) return null;
//   try {
//     if (Array.isArray(val) && val.length >= 3) {
//       const [y, m, d] = val;
//       return new Date(y, (m ?? 1) - 1, d ?? 1);
//     }
//     // string/Date fallback
//     const d = new Date(val);
//     return Number.isNaN(d.getTime()) ? null : d;
//   } catch {
//     return null;
//   }
// };


// const formatDate = (val) => {
//   const d = toJSDateFromLocalDate(val);
//   if (!d) return "-";
//   return d.toLocaleDateString(undefined, {
//     year: "numeric",
//     month: "short",
//     day: "2-digit",
//   });
// };

// const formatTime = (val) => {
//   if (!val) return "-";
//   try {
//     if (Array.isArray(val) && val.length >= 2) {
//       const [h, m] = val;
//       return String(h).padStart(2, "0") + ":" + String(m).padStart(2, "0");
//     }
//     const parts = val.toString().split(":");
//     const h = parts[0] ?? "00";
//     const m = parts[1] ?? "00";
//     return String(h).padStart(2, "0") + ":" + String(m).padStart(2, "0");
//   } catch {
//     return val.toString();
//   }
// };


// const formatAddress = (addr) => {
//   if (!addr) return "-";
//   const parts = [
//     addr.line1,
//     addr.line2,
//     addr.city,
//     addr.state,
//     addr.pincode,
//     addr.country,
//   ]
//     .filter(Boolean)
//     .join(", ");
//   return parts || "-";
// };


// const normalizeRegistration = (reg) => {
//   const e = reg?.event || {};
//   return {
//     registrationId: reg?.registrationId,
//     registrationStatus: reg?.status, // from Registrations.status (enum)
//     eventId: e?.eventId,
//     eventName: e?.eventName,
//     description: e?.description,
//     date: e?.date,
//     time: e?.time,
//     location: formatAddress(e?.address),
//     eventType: e?.eventType,
//     eventStatus: e?.activeStatus,

//   };
// };



const UserDashboard = () => {
  const [showProfile, setShowProfile] = useState(false);
  const [userData, setUserData] = useState({
    userName: "",
    email: "",
    contactNo: "",
  });
  const [registeredEvents, setRegisteredEvents] = useState([]);
  const [activeTab, setActiveTab] = useState("home");

  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState({ key: "date", dir: "asc" });


  useEffect(() => {
    const user = getUser();
    setUserData(user);

    
  const uid = user.id || user.userId;
    if (!uid) {
      setLoadError("User ID not found. Please sign in again.");
      return;
    }


    const fetchRegisteredEvents = async () => {
      try {
        const response = await fetch(`/api/registrations/user/${encodeURIComponent(uid)}`);
        if (response.ok) {
          const data = await response.json();
          setRegisteredEvents(data);
        } else {
          console.error("Failed to fetch registered events");
        }
      } catch (error) {
        console.error("Error fetching registered events:", error);
      }
    };

    fetchRegisteredEvents();
  }, []);

  return (
    <div className="dashboard-container">
      <aside className="sidebar">
        <div className="sidebar-header">User Dashboard</div>
        <ul className="sidebar-menu">
          <li onClick={() => setActiveTab("home")}>Home</li>
          <li onClick={() => setActiveTab("myEvents")}>My Events</li>
          <li onClick={() => setActiveTab("tickets")}>My Tickets</li>
          <li onClick={() => setActiveTab("settings")}>Settings</li>
        </ul>
      </aside>

      <main className="dashboard-main">
        <header className="dashboard-header">
          <div>
            <div className="user-name">{userData.userName}</div>
            <div className="user-email">{userData.email}</div>
          </div>
          <button className="profile-btn" onClick={() => setShowProfile(true)}>
            Profile
          </button>
        </header>

        <section className="dashboard-content">
          {activeTab === "home" && (
            <>
              <div className="dashboard-cards">
                <div className="dashboard-card">[Upcoming Events Placeholder]</div>
                <div className="dashboard-card">[Tickets Summary Placeholder]</div>
              </div>
              <div className="recent-activities">
                <h4>Recent Activities</h4>
                <ul>
                  <li>
                    <span className="activity-icon">📅</span>
                    Registered for Tech Meetup 2025{" "}
                    <span className="activity-date">18 May 2025</span>
                  </li>
                  <li>
                    <span className="activity-icon">🎫</span>
                    Downloaded ticket for Music Fest{" "}
                    <span className="activity-date">16 May 2025</span>
                  </li>
                  <li>
                    <span className="activity-icon">✉️</span>
                    Sent inquiry to Organizer{" "}
                    <span className="activity-date">14 May 2025</span>
                  </li>
                </ul>
              </div>
            </>
          )}

          {activeTab === "myEvents" && (
            <div className="dashboard-card">
              <h4>My Registered Events</h4>
              {
                <p>registeredEvents: {registeredEvents.length}</p>
              }
              {registeredEvents.length > 0 ? (
                <ul>
                  {registeredEvents.map((event) => (
                    <li key={event.id}>
                      <strong>{event.title}</strong> – {event.date}
                    </li>
                  ))}
                </ul>
              ) : (
                <p>No events registered yet.</p>
              )}
            </div>
          )}

          {activeTab === "tickets" && (
            <div className="dashboard-card">
              <h4>My Tickets</h4>
              <p>[Tickets content goes here]</p>
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
          <div className="profile-modal">
            <div className="profile-modal-content">
              <h3>User Profile</h3>
              <p>
                <strong>Name:</strong> {userData.userName}
              </p>
              <p>
                <strong>Email:</strong> {userData.email}
              </p>
              <p>
                <strong>Contact No:</strong> {userData.contactNo}
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

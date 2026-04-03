import React, { useEffect, useMemo, useState } from "react";
import OrganizerEventChatBot from "../../components/EventCard/OrganizerEventChatBot";

const API_BASE = process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080";

/* ===================== UTILITIES (define before usage) ===================== */

// “Mar 22, 2026”
function displayDate(val) {
  if (!val) return "—";
  try {
    const d = new Date(val);
    if (Number.isNaN(d.getTime())) return String(val);
    return d.toLocaleDateString(undefined, {
      year: "numeric",
      month: "short",
      day: "2-digit",
    });
  } catch {
    return String(val);
  }
}

// “09:00”
function displayTime(val) {
  if (!val) return "—";
  try {
    const parts = String(val).split(":");
    if (parts.length >= 2) {
      const d = new Date();
      d.setHours(Number(parts[0]) || 0, Number(parts[1]) || 0, 0, 0);
      return d.toLocaleTimeString(undefined, {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
      });
    }
    const d = new Date(val);
    if (Number.isNaN(d.getTime())) return String(val);
    return d.toLocaleTimeString(undefined, {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
  } catch {
    return String(val);
  }
}

// Build a readable location from event object
function buildLocation(ev) {
  if (ev?.location) return ev.location;
  const a = ev?.address || {};
  const parts = [a.address, a.city, a.state, a.pincode, a.country].filter(Boolean);
  return parts.length ? parts.join(", ") : "—";
}

// Table cells (light UI)
function Th({ children, className = "" }) {
  return (
    <th className={`px-4 py-2 text-left text-sm font-semibold text-gray-700 border-b border-gray-200 ${className}`}>
      {children}
    </th>
  );
}
function Td({ children, className = "" }) {
  return (
    <td className={`px-4 py-2 text-sm text-gray-800 border-b border-gray-200 ${className}`}>
      {children}
    </td>
  );
}

// Status chip (REGISTERED highlight)
function StatusChip({ status }) {
  const s = String(status || "REGISTERED").toUpperCase();
  const cls =
    s === "REGISTERED"
      ? "bg-emerald-50 text-emerald-700 border-emerald-200"
      : s === "CHECKED-IN"
      ? "bg-blue-50 text-blue-700 border-blue-200"
      : s === "CANCELLED"
      ? "bg-rose-50 text-rose-700 border-rose-200"
      : "bg-slate-50 text-slate-700 border-slate-200";
  return <span className={`px-2 py-0.5 rounded border text-xs ${cls}`}>{s}</span>;
}

// --- simple light stat card (Home three metrics) ---
function StatCard({ label, value }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className="text-[11px] tracking-wider uppercase text-gray-500">
        {label}
      </div>
      <div className="mt-1 text-2xl font-semibold text-gray-900">
        {value}
      </div>
    </div>
  );
}

/* ============== Profile Modal (Name, Email, Contact No) ============== */
function ProfileModal({ open, onClose, organizer, organizerId }) {
  if (!open) return null;

  const name = organizer?.name || organizer?.userName || "—";
  const email = organizer?.email || "—";
  const contact =
    organizer?.contactNo ||
    organizer?.phone ||
    organizer?.mobile ||
    organizer?.user?.contactNo ||
    "—";

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
      <div className="w-full max-w-sm rounded-xl bg-white border border-gray-200 shadow-xl">
        <div className="px-5 py-4">
          <h3 className="text-base font-semibold text-gray-900">User Profile</h3>
          <div className="mt-3 text-sm text-gray-800 space-y-2">
            <div><span className="font-semibold">Name:</span> {name}</div>
            <div><span className="font-semibold">Email:</span> {email}</div>
            <div><span className="font-semibold">Contact No:</span> {contact}</div>
            <div className="text-xs text-gray-500 pt-2">Organizer ID: {organizerId ?? "—"}</div>
          </div>
          <div className="mt-4">
            <button
              onClick={onClose}
              className="inline-flex items-center justify-center rounded-md bg-[#0f1b2d] text-white px-4 py-2 text-sm hover:opacity-90"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ===================== ORGANIZER DASHBOARD ===================== */
export default function OrganizerDashboard() {
  const [activeTab, setActiveTab] = useState("home"); // "home" | "events" | "registrations" | "eventbot"
  const [organizer, setOrganizer] = useState(null);
  const [token, setToken] = useState("");

  const [events, setEvents] = useState([]);
  const [eventsAllCache, setEventsAllCache] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const [selectedEventId, setSelectedEventId] = useState(null);
  const [regsByEvent, setRegsByEvent] = useState({}); // { [eventId]: Registration[] }

  const [profileOpen, setProfileOpen] = useState(false);

  /** Load session (organizer + token) */
  useEffect(() => {
    try {
      const raw = localStorage.getItem("organizer") || localStorage.getItem("user");
      if (raw) setOrganizer(JSON.parse(raw));

      // token fallbacks: authToken -> auth_token -> token
      const t =
        localStorage.getItem("authToken") ||
        localStorage.getItem("auth_token") ||
        localStorage.getItem("token") ||
        "";
      if (t) setToken(t);

      // self-heal userRole if missing
      if (!localStorage.getItem("userRole") && raw) {
        const u = JSON.parse(raw);
        const r = (u?.role || (Array.isArray(u?.authorities) && u.authorities[0]) || "")
          .toString()
          .replace(/^ROLE_/, "")
          .toUpperCase();
        if (r) localStorage.setItem("userRole", r);
      }
    } catch {}
  }, []);

  /** Organizer ID (number) */
  const organizerId = useMemo(() => {
    if (!organizer) return null;
    const id =
      organizer?.userId ??
      organizer?.id ??
      organizer?.user?.userId ??
      organizer?.user?.id ??
      null;
    const n = Number(id);
    return Number.isFinite(n) ? n : null;
  }, [organizer]);

  /** Fetch events for organizer */
  useEffect(() => {
    if (!organizerId) return;

    let stop = false;
    (async () => {
      setLoading(true);
      setErr("");
      try {
        const headers = token ? { Authorization: `Bearer ${token}` } : {};
        // Preferred: organizer-specific endpoint
        const r1 = await fetch(
          `${API_BASE}/api/events/organizer/${encodeURIComponent(organizerId)}`,
          { headers }
        );

        if (r1.ok) {
          const data = await r1.json();
          const list = Array.isArray(data) ? data : data ? [data] : [];
          if (!stop) {
            setEvents(list);
            setEventsAllCache(list);
          }
        } else {
          // Fallback: /all and filter by owner
          const r2 = await fetch(`${API_BASE}/api/events/all`);
          if (!r2.ok) throw new Error();
          const all = await r2.json();
          const mine = (all || []).filter(
            (e) =>
              Number(e?.user?.userId ?? e?.user?.id ?? e?.organizerId) ===
              Number(organizerId)
          );
          if (!stop) {
            setEvents(mine);
            setEventsAllCache(mine);
          }
        }
      } catch {
        if (!stop) setErr("Could not load events.");
      } finally {
        if (!stop) setLoading(false);
      }
    })();

    return () => {
      stop = true;
    };
  }, [organizerId, token]);

  /** View registrations (toggle to registrations tab) */
  const viewRegistrations = async (eventId, opts = { switchTab: true }) => {
    const key = String(eventId);

    if (opts.switchTab) {
      setActiveTab("registrations");
      setSelectedEventId(Number(eventId));
    } else {
      setSelectedEventId((prev) => (prev == null ? Number(eventId) : prev));
    }

    if (regsByEvent[key]) return;

    try {
      setLoading(true);
      setErr("");

      const t =
        localStorage.getItem("authToken") ||
        localStorage.getItem("auth_token") ||
        localStorage.getItem("token") ||
        "";
      const headers = {
        "Content-Type": "application/json",
        ...(t ? { Authorization: `Bearer ${t}` } : {}),
      };

      const res = await fetch(
        `${API_BASE}/api/registrations/event/${encodeURIComponent(eventId)}`,
        { headers }
      );

      if (!res.ok) {
        if (res.status === 404) {
          setRegsByEvent((prev) => ({ ...prev, [key]: [] }));
          return;
        }
        if (res.status === 403)
          throw new Error("Forbidden (403). Organizer token required or token expired.");
        if (res.status === 401)
          throw new Error("Unauthorized (401). Invalid/expired token.");
        throw new Error(`Registrations fetch failed (HTTP ${res.status})`);
      }

      const data = await res.json();
      const list = Array.isArray(data) ? data : data ? [data] : [];
      setRegsByEvent((prev) => ({ ...prev, [key]: list }));
    } catch (e) {
      setErr(e?.message || "Could not load registrations.");
    } finally {
      setLoading(false);
    }
  };

  /** Silent prefetch first event registrations so Home’s “Registrations” stat isn’t empty */
  useEffect(() => {
    if (events?.length > 0 && selectedEventId == null) {
      const firstId = events[0]?.eventId ?? events[0]?.id;
      if (firstId) viewRegistrations(firstId, { switchTab: false });
    }

  }, [events]);

  /** Derived */
  const selectedEvent = useMemo(
    () =>
      events.find((e) => Number(e.eventId ?? e.id) === Number(selectedEventId)),
    [events, selectedEventId]
  );
  const registrations = regsByEvent[String(selectedEventId)] || [];

  /** Upcoming count for Home */
  const upcomingCount = useMemo(() => {
    const today = new Date(); today.setHours(0,0,0,0);
    return (events || []).filter(e => {
      if (!e?.date) return false;
      const d = new Date(e.date); d.setHours(0,0,0,0);
      return d >= today;
    }).length;
  }, [events]);

  /** Search events by name/location */
  const onSearchEvents = (q) => {
    const query = (q || "").toLowerCase();
    if (!query) return setEvents(eventsAllCache);
    setEvents(
      (eventsAllCache || []).filter((e) => {
        const name = (e.eventName ?? e.name ?? "").toLowerCase();
        const loc = buildLocation(e).toLowerCase();
        return name.includes(query) || loc.includes(query);
      })
    );
  };

  const onEventCreatedByBot = (createdEvent) => {
    if (!createdEvent) return;
    setEvents((prev) => [createdEvent, ...(prev || [])]);
    setEventsAllCache((prev) => [createdEvent, ...(prev || [])]);
    setActiveTab("events");
  };

  /* ===================== UI ===================== */
  return (
    <div className="min-h-screen bg-[#f5f7fb] text-gray-900">
      <div className="flex">
        {/* Sidebar — Organizer */}
        <aside className="w-64 min-h-screen bg-[#0f1b2d] text-white flex flex-col justify-between border-r border-black/20">
          <div>
            <div className="px-5 py-5 text-lg font-semibold">Organizer Dashboard</div>
            <ul className="px-3 py-2 space-y-1">
              {[
                { id: "home", label: "Home" },
                { id: "events", label: "My Events" },
                { id: "registrations", label: "Registrations" },
                { id: "eventbot", label: "Create Event (ChatBot)" },
              ].map((tab) => (
                <li key={tab.id}>
                  <button
                    onClick={() => setActiveTab(tab.id)}
                    className={`w-full text-left px-3 py-2 rounded-md transition ${
                      activeTab === tab.id ? "bg-white/10" : "hover:bg-white/5"
                    }`}
                  >
                    {tab.label}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          <div className="px-4 py-4 space-y-3">
            <button
              onClick={() => (window.location.href = "/")}
              className="w-full rounded-md bg-[#11c5d5] hover:bg-[#0fb4c3] text-white font-medium py-2 shadow"
            >
              Go to Home
            </button>
            <button
              onClick={() => {
                localStorage.clear();
                window.location.href = "/";
              }}
              className="w-full rounded-md bg-[#11c5d5] hover:bg-[#0fb4c3] text-white font-medium py-2 shadow"
            >
              Logout
            </button>
          </div>
        </aside>

        {/* Main */}
        <main className="flex-1 p-6">
          {/* Profile card with “Profile” modal */}
          <div className="rounded-xl border border-gray-200 bg-white shadow-sm p-5 flex items-center justify-between mb-4">
            <div>
              <div className="text-lg font-semibold">
                {organizer?.name || organizer?.userName || "Organizer"}
              </div>
              <div className="text-sm text-gray-500">
                {organizer?.email || "-"}
              </div>
            </div>

            <button
              onClick={() => setProfileOpen(true)}
              className="px-4 py-2 rounded-md bg-[#0f1b2d] text-white hover:opacity-90"
            >
              Profile
            </button>
          </div>

          {/* Error / Loading */}
          {loading && (
            <div className="rounded-md border border-gray-200 bg-white p-3 mb-4 text-gray-600">
              Loading…
            </div>
          )}
          {err && (
            <div className="rounded-md border border-gray-200 bg-[#fff6f6] p-3 mb-4 text-[#b91c1c]">
              {err}
            </div>
          )}

          {/* ================= HOME (ONLY the three stat cards) ================= */}
          {activeTab === "home" && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <StatCard label="Total Events" value={events.length} />
                <StatCard label="Upcoming" value={upcomingCount} />
                <StatCard label="Registrations" value={registrations.length} />
              </div>
            </div>
          )}

          {/* ================= MY EVENTS ================= */}
          {activeTab === "events" && (
            <div className="rounded-xl border border-gray-200 bg-white shadow-sm">
              <div className="px-4 py-2 rounded-t-xl bg-[#eef2f7] text-center font-semibold text-gray-700 border-b border-gray-200">
                My Events
              </div>

              <div className="p-4 flex items-center justify-between">
                <div className="text-sm text-gray-600">
                  Total: <span className="font-semibold text-gray-800">{events.length}</span>
                </div>
                <input
                  type="text"
                  placeholder="Search by name or location…"
                  onChange={(e) => onSearchEvents(e.target.value)}
                  className="w-64 bg-white border border-gray-300 px-3 py-2 rounded-md text-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#11c5d5]"
                />
              </div>

              <div className="px-4 pb-4">
                <div className="overflow-x-auto rounded-md border border-gray-200">
                  <table className="min-w-full text-sm">
                    <thead className="bg-white">
                      <tr>
                        <Th>S.No.</Th>
                        <Th>Event Name</Th>
                        <Th>Date</Th>
                        <Th>Time</Th>
                        <Th>Location</Th>
                        <Th className="text-right">Action</Th>
                      </tr>
                    </thead>
                    <tbody className="[&>tr:nth-child(even)]:bg-[#f8fafc]">
                      {(events || []).map((ev, idx) => {
                        const eventId = ev?.eventId ?? ev?.id;
                        const name = ev?.eventName ?? ev?.name ?? "—";
                        return (
                          <tr key={eventId ?? idx} className="hover:bg-[#eef2f7]">
                            <Td>{idx + 1}</Td>
                            <Td className="font-medium text-[#0f1b2d]">{name}</Td>
                            <Td>{displayDate(ev?.date)}</Td>
                            <Td>{displayTime(ev?.time)}</Td>
                            <Td className="max-w-[480px]">{buildLocation(ev)}</Td>
                            <Td className="text-right">
                              <button
                                onClick={() => viewRegistrations(eventId, { switchTab: true })}
                                className="inline-flex items-center gap-2 px-3 py-1.5 rounded-md border border-gray-300 text-sm hover:bg-gray-50"
                              >
                                View Registrations
                              </button>
                            </Td>
                          </tr>
                        );
                      })}
                      {events.length === 0 && (
                        <tr>
                          <Td colSpan={6} className="text-center text-gray-500 py-6">
                            No events found.
                          </Td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* ================= REGISTRATIONS ================= */}
          {activeTab === "registrations" && (
            <div className="rounded-xl border border-gray-200 bg-white shadow-sm">
              <div className="px-4 py-2 rounded-t-xl bg-[#eef2f7] text-center font-semibold text-gray-700 border-b border-gray-200 relative">
                <span>
                  {selectedEvent
                    ? `Registrations — ${selectedEvent.eventName ?? selectedEvent.name}`
                    : "Registrations"}
                </span>
                {selectedEventId && (
                  <button
                    onClick={() => setActiveTab("events")}
                    className="absolute right-4 top-1/2 -translate-y-1/2 px-3 py-1.5 rounded-md border border-gray-300 text-sm bg-white hover:bg-gray-50"
                  >
                    Back to Events
                  </button>
                )}
              </div>

              {!selectedEventId ? (
                <div className="p-8 text-center text-gray-500">
                  Select an event from <span className="font-medium text-gray-700">My Events</span>.
                </div>
              ) : (
                <div className="p-4">
                  <div className="overflow-x-auto rounded-md border border-gray-200">
                    <table className="min-w-full text-sm">
                      <thead className="bg-white">
                        <tr>
                          <Th>S.No.</Th>
                          <Th>Name</Th>
                          <Th>Email</Th>
                          <Th>Phone</Th>
                          <Th>Status</Th>
                          <Th>Registered On</Th>
                        </tr>
                      </thead>
                      <tbody className="[&>tr:nth-child(even)]:bg-[#f8fafc]">
                        {registrations.length === 0 ? (
                          <tr>
                            <Td colSpan={6} className="text-center text-gray-500 py-6">
                              No registrations found.
                            </Td>
                          </tr>
                        ) : (
                          registrations.map((r, idx) => {
                            const name = r?.user?.userName || r?.user?.name || "—";
                            const email = r?.user?.email || "—";
                            const phone = r?.user?.contactNo || "—";
                            const status = r?.status || "REGISTERED";
                            const created = r?.creationTime || r?.createdOn || "—";
                            return (
                              <tr key={r?.registrationId ?? idx} className="hover:bg-[#eef2f7]">
                                <Td>{idx + 1}</Td>
                                <Td className="font-medium text-[#0f1b2d]">{name}</Td>
                                <Td>{email}</Td>
                                <Td>{phone}</Td>
                                <Td>
                                  <StatusChip status={status} />
                                </Td>
                                <Td>{displayDate(created)}</Td>
                              </tr>
                            );
                          })
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === "eventbot" && (
            <OrganizerEventChatBot
              organizerId={organizerId}
              token={token}
              onEventCreated={onEventCreatedByBot}
            />
          )}
        </main>
      </div>

      {/* Profile Modal (Name, Email, Contact) */}
      <ProfileModal
        open={profileOpen}
        onClose={() => setProfileOpen(false)}
        organizer={organizer}
        organizerId={organizerId}
      />
    </div>
  );
}
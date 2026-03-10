import React, { useEffect, useMemo, useState } from "react";

const API_BASE = process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080";

function isUpcoming(iso) {
  if (!iso) return false;
  try {
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const d = new Date(iso);  d.setHours(0, 0, 0, 0);
    return d >= today;
  } catch { return false; }
}

function displayDate(val) {
  if (!val) return "—";
  try {
    const d = new Date(val);
    if (Number.isNaN(d.getTime())) return String(val);
    return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "2-digit" });
  } catch { return String(val); }
}

function Th({ children }) {
  return <th className="px-4 py-3 text-left text-sm font-medium border-b border-white/10">{children}</th>;
}

function Td({ children, className = "" }) {
  return <td className={`px-4 py-3 text-sm border-b border-white/10 ${className}`}>{children}</td>;
}

function Card({ title, children, right }) {
  return (
    <section className="bg-black rounded-xl border border-white/15 shadow-sm">
      <div className="flex items-center justify-between px-4 py-3 border-b border-white/10">
        <h3 className="text-base font-semibold text-white">{title}</h3>
        {right ?? null}
      </div>
      <div className="p-4 text-white/90">{children}</div>
    </section>
  );
}

function Stat({ label, value }) {
  return (
    <div className="rounded-xl border border-white/15 bg-black p-4">
      <div className="text-xs uppercase tracking-wide text-white/60">{label}</div>
      <div className="mt-1 text-2xl font-semibold text-white">{value}</div>
    </div>
  );
}

/** ===================== ORGANIZER DASHBOARD ===================== **/
export default function OrganizerDashboard() {
  const [activeTab, setActiveTab] = useState("home"); // "home" | "events" | "registrations" | "settings"
  const [organizer, setOrganizer] = useState(null);
  const [token, setToken] = useState("");

  const [events, setEvents] = useState([]);
  const [eventsAllCache, setEventsAllCache] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const [selectedEventId, setSelectedEventId] = useState(null);
  const [regsByEvent, setRegsByEvent] = useState({}); // { [eventId]: Registration[] }

  const organizerId = organizer?.id ?? organizer?.userId ?? null;

  /** Read organizer + token once */
  useEffect(() => {
    try {
      const raw = localStorage.getItem("organizer") || localStorage.getItem("user");
      if (raw) setOrganizer(JSON.parse(raw));
      const t = localStorage.getItem("token") || "";
      if (t) setToken(t);
    } catch { /* ignore */ }
  }, []);

  /** Fetch events (preferred endpoint + fallback to /all) */
  useEffect(() => {
    if (!organizerId) return;

    let aborted = false;
    const load = async () => {
      setLoading(true);
      setErr("");

      try {
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

        // 1) Try organizer-specific endpoint
        const r1 = await fetch(
          `${API_BASE}/api/events/organizer/${encodeURIComponent(organizerId)}`,
          { headers: authHeaders }
        );

        if (r1.ok) {
          const data = await r1.json();
          if (!aborted) {
            const list = Array.isArray(data) ? data : data ? [data] : [];
            setEvents(list);
            setEventsAllCache(list);
          }
        } else {
          // 2) Fallback: /all then filter
          const r2 = await fetch(`${API_BASE}/api/events/all`);
          if (!r2.ok) throw new Error(`HTTP ${r2.status}`);
          const all = await r2.json();
          const mine = (all || []).filter(
            (e) => (e?.user?.userId ?? e?.user?.id) === organizerId
          );
          if (!aborted) {
            setEvents(mine);
            setEventsAllCache(mine);
          }
        }
      } catch {
        if (!aborted) setErr("Could not load events. Please retry.");
      } finally {
        if (!aborted) setLoading(false);
      }
    };

    load();
    return () => { aborted = true; };
  }, [organizerId, token]);

  /** Load registrations for an event */
  const viewRegistrations = async (eventId) => {
    setSelectedEventId(eventId);
    setActiveTab("registrations");

    if (regsByEvent[eventId]) return; // cached

    try {
      setLoading(true);
      setErr("");

      const headers = { "Content-Type": "application/json" };
      if (token) headers["Authorization"] = `Bearer ${token}`;

      const res = await fetch(
        `${API_BASE}/api/registrations/event/${encodeURIComponent(eventId)}`,
        { headers }
      );

      if (!res.ok) throw new Error(`Registrations fetch failed (HTTP ${res.status})`);

      const data = await res.json();
      const list = Array.isArray(data) ? data : data ? [data] : [];

      setRegsByEvent((prev) => ({ ...prev, [eventId]: list }));
    } catch {
      setErr("Could not load registrations. Ensure organizer token is set.");
    } finally {
      setLoading(false);
    }
  };

  /** Derived */
  const selectedEvent = useMemo(
    () => events.find((e) => (e.eventId ?? e.id) === selectedEventId),
    [events, selectedEventId]
  );
  const registrations = regsByEvent[selectedEventId] || [];
  const upcomingCount = useMemo(
    () => (events || []).filter((e) => isUpcoming(e.date)).length,
    [events]
  );

  /** Actions */
  const goHome = () => (window.location.href = "/");
  const onSearch = (q) => {
    const query = q.toLowerCase();
    if (!query) return setEvents(eventsAllCache);
    setEvents(
      (eventsAllCache || []).filter((e) => {
        const name = (e.eventName ?? e.name ?? "").toLowerCase();
        const loc = (e.location ?? e?.address?.address ?? "").toLowerCase();
        return name.includes(query) || loc.includes(query);
      })
    );
  };

  return (
    <div className="min-h-screen bg-black text-white">
      <div className="flex">
        {/* SIDEBAR: Settings pinned at bottom */}
        <aside className="w-60 min-h-screen bg-black border-r border-white/10 flex flex-col justify-between">
          {/* Top nav */}
          <div>
            <div className="px-4 py-5 text-lg font-semibold">Organizer</div>
            <ul className="px-2 py-2 space-y-1">
              {[
                { id: "home", label: "Home" },
                { id: "events", label: "My Events" },
                { id: "registrations", label: "Registrations" },
              ].map((tab) => (
                <li key={tab.id}>
                  <button
                    onClick={() => setActiveTab(tab.id)}
                    className={`w-full text-left px-3 py-2 rounded-lg transition ${
                      activeTab === tab.id ? "bg-white text-black" : "hover:bg-white/10"
                    }`}
                  >
                    {tab.label}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          {/* Settings at bottom */}
          <div className="px-2 py-4 border-t border-white/10">
            <button
              onClick={() => setActiveTab("settings")}
              className={`w-full text-left px-3 py-2 rounded-lg transition ${
                activeTab === "settings" ? "bg-white text-black" : "hover:bg-white/10"
              }`}
            >
              Settings
            </button>
          </div>
        </aside>

        {/* MAIN */}
        <main className="flex-1">
          {/* HEADER: only Go to Home */}
          <header className="flex items-center justify-between px-6 py-4 bg-black border-b border-white/10">
            <div>
              <div className="text-lg font-semibold">
                {organizer?.name || organizer?.userName || "Organizer"}
              </div>
              <div className="text-sm text-white/60">{organizer?.email || "-"}</div>
            </div>

            <button
              className="px-3 py-2 rounded-lg border border-white/30 hover:bg-white hover:text-black"
              onClick={goHome}
            >
              Go to Home
            </button>
          </header>

          {/* CONTENT */}
          <section className="p-6 space-y-6">
            {loading && (
              <div className="rounded-md bg-white/5 px-4 py-2 border border-white/10">Loading…</div>
            )}
            {err && (
              <div className="rounded-md bg-white/5 px-4 py-3 border border-white/10">{err}</div>
            )}

            {/* HOME */}
            {activeTab === "home" && (
              <>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <Stat label="Total Events" value={events.length} />
                  <Stat label="Upcoming" value={upcomingCount} />
                  <Stat label="Registrations" value={registrations.length} />
                  <Stat label="Revenue" value={"₹0"} />
                </div>

                <Card title="Quick Info">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                    <p>
                      <span className="text-white/60">Organizer ID:</span> {organizerId ?? "—"}
                    </p>
                    <p>
                      <span className="text-white/60">Email:</span> {organizer?.email ?? "—"}
                    </p>
                    
                  </div>
                </Card>
              </>
            )}

            {/* EVENTS */}
            {activeTab === "events" && (
              <Card
                title="My Events"
                right={
                  <input
                    type="text"
                    placeholder="Search…"
                    onChange={(e) => onSearch(e.target.value)}
                    className="w-44 md:w-64 bg-black border border-white/20 px-3 py-2 rounded-lg text-sm text-white placeholder-white/40 outline-none focus:ring-2 focus:ring-white/20"
                  />
                }
              >
                <div className="overflow-x-auto rounded-xl border border-white/15">
                  <table className="min-w-full text-white text-sm">
                    <thead className="bg-white/10">
                      <tr>
                        <Th>ID</Th>
                        <Th>Name</Th>
                        <Th>Date</Th>
                        <Th>Location</Th>
                        <Th>Actions</Th>
                      </tr>
                    </thead>
                    <tbody>
                      {events.length === 0 ? (
                        <tr>
                          <td colSpan="5" className="px-4 py-8 text-center text-white/70">
                            No events found for this organizer.
                          </td>
                        </tr>
                      ) : (
                        events.map((ev) => {
                          const eventId = ev.eventId ?? ev.id;
                          const name = ev.eventName ?? ev.name ?? "—";
                          const loc =
                            ev.location ??
                            ev?.address?.address ??
                            ev?.address?.state ??
                            "—";

                          return (
                            <tr key={eventId} className="hover:bg-white/5">
                              <Td>{eventId}</Td>
                              <Td className="font-medium">{name}</Td>
                              <Td>{displayDate(ev.date)}</Td>
                              <Td>{loc}</Td>
                              <Td>
                                <button
                                  onClick={() => viewRegistrations(eventId)}
                                  className="px-2 py-1 rounded-md border border-white/30 hover:bg-white hover:text-black"
                                >
                                  View Registrations
                                </button>
                              </Td>
                            </tr>
                          );
                        })
                      )}
                    </tbody>
                  </table>
                </div>
              </Card>
            )}

            {/* REGISTRATIONS */}
            {activeTab === "registrations" && (
              <Card
                title={
                  selectedEvent
                    ? `Registrations — ${selectedEvent.eventName ?? selectedEvent.name}`
                    : "Registrations"
                }
                right={
                  selectedEventId && (
                    <button
                      onClick={() => setActiveTab("events")}
                      className="px-3 py-2 rounded-lg border border-white/30 hover:bg-white hover:text-black text-sm"
                    >
                      Back to Events
                    </button>
                  )
                }
              >
                {!selectedEventId ? (
                  <div className="p-6 text-center text-white/70 border border-dashed border-white/20 rounded-xl">
                    Select an event from <span className="text-white">My Events</span>.
                  </div>
                ) : registrations.length === 0 ? (
                  <div className="p-6 text-center text-white/70 border border-dashed border-white/20 rounded-xl">
                    No registrations found for this event.
                  </div>
                ) : (
                  <div className="overflow-x-auto rounded-xl border border-white/15">
                    <table className="min-w-full text-white text-sm">
                      <thead className="bg-white/10">
                        <tr>
                          <Th>#</Th>
                          <Th>Name</Th>
                          <Th>Email</Th>
                          <Th>Phone</Th>
                        </tr>
                      </thead>
                      <tbody>
                        {registrations.map((r, idx) => (
                          <tr
                            key={r.id ?? r.registrationId ?? `${selectedEventId}-${idx}`}
                            className="hover:bg-white/5"
                          >
                            <Td>{idx + 1}</Td>
                            <Td className="font-medium">{r.userName ?? r.name ?? "—"}</Td>
                            <Td>{r.email ?? "—"}</Td>
                            <Td>{r.contactNo ?? r.phone ?? "—"}</Td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </Card>
            )}

            {/* SETTINGS */}
            {activeTab === "settings" && (
              <Card title="Settings">
                <div className="text-sm space-y-2">
                  <p>
                    <span className="text-white/60">Organizer Name:</span>{" "}
                    {organizer?.name || organizer?.userName || "—"}
                  </p>
                  <p>
                    <span className="text-white/60">Email:</span>{" "}
                    {organizer?.email || "—"}
                  </p>
                  <p className="text-white/60">Profile & preferences coming later.</p>
                </div>
              </Card>
            )}
          </section>
        </main>
      </div>
    </div>
  );
}
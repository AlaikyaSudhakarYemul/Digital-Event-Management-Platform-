import React from "react";

/**
 * Usage:
 * <OrganizerDashboard
 *   stats={{ total: 0, upcoming: 0, registrations: 0, revenue: 0 }}
 *   upcoming={[]} // [{ id, name, date, location }]
 *   events={[]}   // [{ id, name, date, registrations, status }]
 * />
 */
export default function OrganizerDashboard({
  stats = { total: 0, upcoming: 0, registrations: 0, revenue: 0 },
  upcoming = [],
  events = [],
}) {
  return (
    <div className="mx-auto max-w-6xl px-4 md:px-6 py-6 space-y-6">

      {/* Page Header */}
      <header>
        <h1 className="text-2xl md:text-3xl font-semibold text-gray-900">Organizer Dashboard</h1>
        <p className="text-gray-600 text-sm md:text-base">Manage your events at a glance.</p>
      </header>

      {/* Stats */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <StatCard label="Total Events" value={stats.total ?? 0} />
        <StatCard label="Upcoming" value={stats.upcoming ?? 0} />
        <StatCard label="Registrations" value={stats.registrations ?? 0} />
        <StatCard label="Revenue" value={`₹${stats.revenue ?? 0}`} />
      </section>

      {/* Upcoming Events */}
      <Section title="Upcoming Events">
        {upcoming.length === 0 ? (
          <EmptyState text="No upcoming events yet." />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {upcoming.map((u) => (
              <div
                key={u.id}
                className="rounded-xl border border-gray-200 bg-white p-3 hover:bg-gray-50 transition"
              >
                <div className="font-medium text-gray-900">{u.name}</div>
                <div className="text-sm text-gray-600">
                  {u.date} {u.location ? `• ${u.location}` : ""}
                </div>
              </div>
            ))}
          </div>
        )}
      </Section>

      {/* My Events */}
      <Section
        title="My Events"
        right={
          <input
            type="text"
            placeholder="Search…"
            className="w-44 md:w-64 rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-100"
          />
        }
      >
        <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white">
          <table className="min-w-full text-sm">
            <thead className="bg-gray-50 text-gray-700">
              <tr>
                <Th>ID</Th>
                <Th>Name</Th>
                <Th>Date</Th>
                <Th>Registrations</Th>
                <Th>Status</Th>
                <Th>Actions</Th>
              </tr>
            </thead>
            <tbody>
              {events.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-4 py-8 text-center text-gray-500">
                    No events created yet.
                  </td>
                </tr>
              ) : (
                events.map((ev) => (
                  <tr key={ev.id} className="border-t hover:bg-gray-50">
                    <Td>{ev.id}</Td>
                    <Td className="font-medium text-gray-900">{ev.name}</Td>
                    <Td>{ev.date}</Td>
                    <Td>{ev.registrations ?? 0}</Td>
                    <Td><StatusBadge value={ev.status} /></Td>
                    <Td>
                      <div className="flex gap-2">
                        <button className="px-2 py-1 rounded-md bg-gray-100 text-gray-800 hover:bg-gray-200">
                          View
                        </button>
                        <button className="px-2 py-1 rounded-md bg-gray-100 text-gray-800 hover:bg-gray-200">
                          Edit
                        </button>
                      </div>
                    </Td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Section>
    </div>
  );
}

/* ───────── helpers ───────── */

function Section({ title, right, children }) {
  return (
    <section className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
        {right ?? null}
      </div>
      {children}
    </section>
  );
}

function StatCard({ label, value }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      <div className="text-sm text-gray-500">{label}</div>
      <div className="mt-1 text-2xl font-semibold text-gray-900">{value}</div>
    </div>
  );
}

function EmptyState({ text }) {
  return (
    <div className="rounded-xl border border-dashed border-gray-300 bg-white p-6 text-center text-sm text-gray-600">
      {text}
    </div>
  );
}

function StatusBadge({ value }) {
  const v = (value || "").toLowerCase();
  const cls =
    v === "published"
      ? "bg-green-100 text-green-700"
      : v === "draft"
      ? "bg-amber-100 text-amber-700"
      : v === "closed"
      ? "bg-gray-100 text-gray-700"
      : "bg-blue-100 text-blue-700";
  return <span className={`inline-block rounded-md px-2 py-0.5 text-xs ${cls}`}>{value || "—"}</span>;
}

function Th({ children }) {
  return <th className="px-4 py-3 text-left text-sm font-medium">{children}</th>;
}

function Td({ children, className = "" }) {
  return <td className={`px-4 py-3 text-gray-700 ${className}`}>{children}</td>;
}
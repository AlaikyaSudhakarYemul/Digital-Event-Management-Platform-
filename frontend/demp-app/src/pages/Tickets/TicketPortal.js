import React, { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  bookTicket,
  cancelTicket,
  getAllEvents,
  getCurrentUserId,
  getPaymentByTicketId,
  getTicketsByUserId,
  processPayment,
  refundPayment,
} from "../../services/ticketService";

function useQuery() {
  const location = useLocation();
  return useMemo(() => new URLSearchParams(location.search), [location.search]);
}

const TicketPortal = () => {
  const navigate = useNavigate();
  const query = useQuery();

  const [events, setEvents] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const currentUserId = getCurrentUserId();

  const defaultEventFromUrl = Number(query.get("eventId"));

  const [bookingForm, setBookingForm] = useState({
    eventId: Number.isFinite(defaultEventFromUrl) && defaultEventFromUrl > 0 ? defaultEventFromUrl : "",
    quantity: 1,
    totalAmount: "",
  });

  const [paymentForm, setPaymentForm] = useState({
    ticketId: "",
    amount: "",
    transactionId: "",
    paymentStatus: "SUCCESS",
  });

  const [paymentByTicket, setPaymentByTicket] = useState({});

  const loadEvents = async () => {
    try {
      const data = await getAllEvents();
      setEvents(data);
    } catch (err) {
      setError(err.message || "Failed to load events");
    }
  };

  const loadUserTickets = async () => {
    if (!currentUserId) {
      setTickets([]);
      return;
    }

    try {
      const userTickets = await getTicketsByUserId(currentUserId);
      setTickets(Array.isArray(userTickets) ? userTickets : []);

      const payments = {};
      await Promise.all(
        (Array.isArray(userTickets) ? userTickets : []).map(async (ticket) => {
          try {
            const payment = await getPaymentByTicketId(ticket.ticketId);
            payments[ticket.ticketId] = payment;
          } catch {
            payments[ticket.ticketId] = null;
          }
        })
      );
      setPaymentByTicket(payments);
    } catch (err) {
      setError(err.message || "Failed to load tickets");
    }
  };

  useEffect(() => {
    loadEvents();
    loadUserTickets();
    // loadEvents and loadUserTickets are local closures tied to current render state.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUserId]);

  const handleBookTicket = async (event) => {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!currentUserId) {
      setError("Please login first to book tickets.");
      return;
    }

    if (!bookingForm.eventId || !bookingForm.totalAmount) {
      setError("Event and total amount are required.");
      return;
    }

    try {
      setLoading(true);
      const booked = await bookTicket({
        eventId: Number(bookingForm.eventId),
        userId: currentUserId,
        quantity: Number(bookingForm.quantity),
        totalAmount: Number(bookingForm.totalAmount),
      });

      setMessage(`Ticket #${booked.ticketId} booked successfully.`);
      setPaymentForm((prev) => ({
        ...prev,
        ticketId: String(booked.ticketId),
        amount: String(booked.totalAmount),
      }));
      await loadUserTickets();
    } catch (err) {
      setError(err.message || "Failed to book ticket");
    } finally {
      setLoading(false);
    }
  };

  const handleProcessPayment = async (event) => {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!paymentForm.ticketId || !paymentForm.amount) {
      setError("Ticket ID and amount are required for payment.");
      return;
    }

    try {
      setLoading(true);
      const payment = await processPayment({
        ticket: { ticketId: Number(paymentForm.ticketId) },
        amount: Number(paymentForm.amount),
        paymentStatus: paymentForm.paymentStatus,
        transactionId: paymentForm.transactionId || null,
      });

      setMessage(`Payment #${payment.paymentId} processed with status ${payment.paymentStatus}.`);
      await loadUserTickets();
    } catch (err) {
      setError(err.message || "Failed to process payment");
    } finally {
      setLoading(false);
    }
  };

  const handleCancelTicket = async (ticketId) => {
    setError("");
    setMessage("");

    try {
      setLoading(true);
      await cancelTicket(ticketId);
      setMessage(`Ticket #${ticketId} cancelled.`);
      await loadUserTickets();
    } catch (err) {
      setError(err.message || "Failed to cancel ticket");
    } finally {
      setLoading(false);
    }
  };

  const handleRefund = async (ticketId) => {
    setError("");
    setMessage("");

    const payment = paymentByTicket[ticketId];
    if (!payment?.paymentId) {
      setError("No payment found for this ticket.");
      return;
    }

    try {
      setLoading(true);
      await refundPayment(payment.paymentId);
      setMessage(`Payment #${payment.paymentId} refunded.`);
      await loadUserTickets();
    } catch (err) {
      setError(err.message || "Failed to refund payment");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 px-4 py-8">
      <div className="max-w-6xl mx-auto space-y-8">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">Ticket Service Portal</h1>
          <button
            onClick={() => navigate("/")}
            className="px-4 py-2 rounded-md border border-slate-500 hover:bg-slate-800"
          >
            Back to Home
          </button>
        </div>

        {!currentUserId && (
          <div className="p-4 rounded-md bg-amber-900/40 border border-amber-500">
            You are not logged in with a user profile that includes userId. Login first to book tickets.
          </div>
        )}

        {error && <div className="p-3 rounded-md bg-red-900/40 border border-red-500">{error}</div>}
        {message && <div className="p-3 rounded-md bg-emerald-900/40 border border-emerald-500">{message}</div>}

        <div className="grid md:grid-cols-2 gap-6">
          <form onSubmit={handleBookTicket} className="p-5 rounded-xl bg-slate-900 border border-slate-700 space-y-4">
            <h2 className="text-xl font-semibold">Book Ticket</h2>

            <div>
              <label className="block text-sm mb-1">Event</label>
              <select
                value={bookingForm.eventId}
                onChange={(e) => setBookingForm((prev) => ({ ...prev, eventId: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              >
                <option value="">Select event</option>
                {events.map((event) => (
                  <option key={event.eventId} value={event.eventId}>
                    {event.eventName} (#{event.eventId})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm mb-1">Quantity</label>
              <input
                type="number"
                min="1"
                value={bookingForm.quantity}
                onChange={(e) => setBookingForm((prev) => ({ ...prev, quantity: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              />
            </div>

            <div>
              <label className="block text-sm mb-1">Total Amount</label>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={bookingForm.totalAmount}
                onChange={(e) => setBookingForm((prev) => ({ ...prev, totalAmount: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              />
            </div>

            <button
              type="submit"
              disabled={loading || !currentUserId}
              className="px-4 py-2 rounded-md bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50"
            >
              Book Ticket
            </button>
          </form>

          <form onSubmit={handleProcessPayment} className="p-5 rounded-xl bg-slate-900 border border-slate-700 space-y-4">
            <h2 className="text-xl font-semibold">Process Payment</h2>

            <div>
              <label className="block text-sm mb-1">Ticket ID</label>
              <input
                type="number"
                min="1"
                value={paymentForm.ticketId}
                onChange={(e) => setPaymentForm((prev) => ({ ...prev, ticketId: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              />
            </div>

            <div>
              <label className="block text-sm mb-1">Amount</label>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={paymentForm.amount}
                onChange={(e) => setPaymentForm((prev) => ({ ...prev, amount: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              />
            </div>

            <div>
              <label className="block text-sm mb-1">Transaction ID</label>
              <input
                type="text"
                value={paymentForm.transactionId}
                onChange={(e) => setPaymentForm((prev) => ({ ...prev, transactionId: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              />
            </div>

            <div>
              <label className="block text-sm mb-1">Payment Status</label>
              <select
                value={paymentForm.paymentStatus}
                onChange={(e) => setPaymentForm((prev) => ({ ...prev, paymentStatus: e.target.value }))}
                className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700"
              >
                <option value="SUCCESS">SUCCESS</option>
                <option value="PENDING">PENDING</option>
                <option value="FAILED">FAILED</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 rounded-md bg-fuchsia-600 hover:bg-fuchsia-500 disabled:opacity-50"
            >
              Pay Now
            </button>
          </form>
        </div>

        <section className="p-5 rounded-xl bg-slate-900 border border-slate-700">
          <h2 className="text-xl font-semibold mb-4">My Tickets</h2>
          {tickets.length === 0 ? (
            <p className="text-slate-400">No tickets found for current user.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-slate-700">
                    <th className="py-2 pr-3">Ticket</th>
                    <th className="py-2 pr-3">Event</th>
                    <th className="py-2 pr-3">Qty</th>
                    <th className="py-2 pr-3">Amount</th>
                    <th className="py-2 pr-3">Status</th>
                    <th className="py-2 pr-3">Payment</th>
                    <th className="py-2">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {tickets.map((ticket) => {
                    const payment = paymentByTicket[ticket.ticketId];
                    return (
                      <tr key={ticket.ticketId} className="border-b border-slate-800">
                        <td className="py-2 pr-3">#{ticket.ticketId}</td>
                        <td className="py-2 pr-3">{ticket.eventId}</td>
                        <td className="py-2 pr-3">{ticket.quantity}</td>
                        <td className="py-2 pr-3">{ticket.totalAmount}</td>
                        <td className="py-2 pr-3">{ticket.status}</td>
                        <td className="py-2 pr-3">{payment?.paymentStatus || "N/A"}</td>
                        <td className="py-2 space-x-2">
                          {(ticket.status === "RESERVED" || ticket.status === "CONFIRMED") && (
                            <button
                              onClick={() => handleCancelTicket(ticket.ticketId)}
                              className="px-2 py-1 rounded bg-amber-700 hover:bg-amber-600"
                            >
                              Cancel
                            </button>
                          )}

                          {payment?.paymentStatus === "SUCCESS" && (
                            <button
                              onClick={() => handleRefund(ticket.ticketId)}
                              className="px-2 py-1 rounded bg-rose-700 hover:bg-rose-600"
                            >
                              Refund
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default TicketPortal;

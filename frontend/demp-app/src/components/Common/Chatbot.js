import React, { useState, useRef, useEffect } from 'react';

const CHAT_URL = process.env.REACT_APP_CHATBOT_URL || 'http://127.0.0.1:8000/ask';

// FAQ cache — clicking a suggestion returns the answer instantly, no API call
const FAQ_SUGGESTIONS = [
  {
    label: 'What is DEMP?',
    answer:
      '**DEMP** (Digital Event Management Platform) is a full-stack microservices application that manages the complete event lifecycle — user registration, event creation, ticketing, payments, notifications, and analytics. It supports three roles: **Admin**, **Organizer**, and **User**.',
  },
  {
    label: 'How do I book a ticket?',
    answer:
      'To book a ticket:\n1. Browse **Events** and select one.\n2. Click **Book Ticket** on the event page.\n3. Confirm and proceed to **Payment**.\n4. After payment, find your ticket in **My Tickets**.',
  },
  {
    label: 'How do I create an event?',
    answer:
      'Creating an event requires the **Organizer** role:\n1. Log in as an Organizer.\n2. Go to **Organizer Dashboard → Create Event**.\n3. Fill in title, date, venue, capacity, and price.\n4. Click **Publish** to go live.',
  },
  {
    label: 'What roles are available?',
    answer:
      'DEMP has three roles:\n- **Admin** — full platform control, user & event management, reports.\n- **Organizer** — create/manage events, track registrations & revenue.\n- **User** — browse events, book tickets, make payments.',
  },
  {
    label: 'What is the tech stack?',
    answer:
      'DEMP uses:\n- **Frontend**: React\n- **Backend**: Java Spring Boot Microservices\n- **Database**: MySQL\n- **Gateway**: Spring Cloud Gateway (port 8080)\n- **Discovery**: Eureka Server (port 8761)\n- **Chatbot**: Python FastAPI + RAG + ChromaDB',
  },
  {
    label: 'How do I register?',
    answer:
      'To register:\n1. Click **Sign Up** on the homepage.\n2. Enter your name, email, password, contact number, and role.\n3. Submit — you are logged in automatically.',
  },
  {
    label: 'How do I view my tickets?',
    answer:
      'To view tickets:\n1. Log in as a **User**.\n2. Navigate to **My Tickets** in your dashboard.\n3. All confirmed bookings and tickets are listed there.',
  },
  {
    label: 'How does the chatbot work?',
    answer:
      'EventMate uses **RAG** (Retrieval-Augmented Generation):\n1. Your question is embedded as a vector.\n2. Relevant DEMP document chunks are retrieved from **ChromaDB**.\n3. The **Ollama LLM** generates a contextual answer.\nFor common questions, a **cache layer** gives instant answers without any LLM call.',
  },
  {
    label: 'What microservices are there?',
    answer:
      'DEMP has 10 microservices:\n- api-gateway (8080)\n- eureka-server (8761)\n- user-service (8084)\n- admin-service (8083)\n- event-service\n- registration-service\n- tickets-service\n- payment-service\n- notification-service\n- organizer-service',
  },
  {
    label: 'How do I contact support?',
    answer:
      'For support:\n- Use **EventMate chatbot** for instant help.\n- Contact the platform **Admin** for account or access issues.\n- Reach out to the **Organizer** for event-specific queries.',
  },
];

// Tiny markdown -> HTML converter (bold, italic, lists, line breaks).
function renderMarkdown(text) {
  const escape = (s) =>
    s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  let html = escape(text);
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  // bullet lists
  html = html.replace(/(^|\n)- (.*)/g, '$1<li>$2</li>');
  html = html.replace(/(<li>.*<\/li>)(?!<li>)/gs, '<ul class="list-disc ml-5">$1</ul>');
  html = html.replace(/\n/g, '<br/>');
  return html;
}

const Chatbot = () => {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState(null);
  const [messages, setMessages] = useState([
    {
      from: 'bot',
      text:
        "Hi! I'm **EventMate**, your DEMP assistant. Ask me anything about events, tickets, payments — or any general question.",
    },
  ]);
  const scrollRef = useRef(null);
  const [showSuggestions, setShowSuggestions] = useState(true);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, open]);

  const send = async (text) => {
    const message = (text ?? input).trim();
    if (!message || loading) return;
    setMessages((m) => [...m, { from: 'user', text: message }]);
    setInput('');
    setLoading(true);
    // Check FAQ cache first — instant response, no API call needed
    const faqHit = FAQ_SUGGESTIONS.find(
      (f) => f.label.toLowerCase() === message.toLowerCase()
    );
    if (faqHit) {
      setMessages((m) => [...m, { from: 'bot', text: faqHit.answer }]);
      setLoading(false);
      return;
    }
    try {
      // Forward the logged-in user's JWT and ID so the bot can act on
      // their behalf (e.g. create events) via the backend API.
      let authToken = null;
      let userId = null;
      try {
        authToken = localStorage.getItem('auth_token') || null;
        const u = localStorage.getItem('user');
        if (u) userId = JSON.parse(u)?.userId ?? null;
      } catch (_) { /* ignore parse errors */ }

      const res = await fetch(CHAT_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          question: message,
          // Optionally, you can add model_name and top_k if you want to expose them in UI
        }),
      });
      if (!res.ok) {
        let errorDetails = '';
        try {
          errorDetails = await res.text();
        } catch (_) {
          // Keep fallback error when response body cannot be parsed.
        }
        throw new Error(`HTTP ${res.status}${errorDetails ? ` - ${errorDetails}` : ''}`);
      }
      const data = await res.json();
      setMessages((m) => [...m, { from: 'bot', text: data.answer }]);
    } catch (err) {
      const details = err?.message || 'Unknown error';
      setMessages((m) => [
        ...m,
        {
          from: 'bot',
          text:
            `⚠️ Chatbot request failed: **${details}**.\n\nIf the frontend is running on another device, replace localhost with your machine IP in \`REACT_APP_CHATBOT_URL\` and restart the frontend. Default endpoint: \`${CHAT_URL}\`.`,
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const onKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  return (
    <>
      {/* Floating launcher button */}
      <button
        onClick={() => setOpen((o) => !o)}
        aria-label={open ? 'Close chat' : 'Open chat'}
        className="fixed bottom-6 right-6 z-50 h-14 w-14 rounded-full bg-gradient-to-br from-pink-500 to-indigo-600 text-white shadow-lg hover:scale-105 transition flex items-center justify-center"
      >
        {open ? (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        ) : (
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        )}
      </button>

      {/* Chat panel */}
      {open && (
        <div className="fixed bottom-24 right-6 z-50 w-[360px] max-w-[calc(100vw-2rem)] h-[520px] max-h-[calc(100vh-7rem)] bg-white rounded-2xl shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          {/* Header */}
          <div className="px-4 py-3 bg-gradient-to-r from-pink-500 to-indigo-600 text-white">
            <div className="font-semibold">EventMate</div>
            <div className="text-xs opacity-90">Your DEMP guide — events, tickets, payments…</div>
          </div>

          {/* Messages */}
          <div ref={scrollRef} className="flex-1 overflow-y-auto p-3 space-y-2 bg-gray-50">
            {messages.map((m, i) => (
              <div
                key={i}
                className={`flex ${m.from === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[85%] px-3 py-2 rounded-2xl text-sm leading-relaxed ${
                    m.from === 'user'
                      ? 'bg-indigo-600 text-white rounded-br-sm'
                      : 'bg-white text-gray-800 border border-gray-200 rounded-bl-sm'
                  }`}
                  dangerouslySetInnerHTML={{ __html: renderMarkdown(m.text) }}
                />
              </div>
            ))}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-white border border-gray-200 px-3 py-2 rounded-2xl text-sm text-gray-500">
                  Typing…
                </div>
              </div>
            )}
          </div>

          {/* FAQ Suggestions panel */}
          <div className="border-t border-gray-100 bg-white">
            <button
              onClick={() => setShowSuggestions((s) => !s)}
              className="w-full flex items-center justify-between px-3 py-1.5 text-xs font-medium text-indigo-600 hover:bg-indigo-50 transition"
            >
              <span>💡 Suggested Questions</span>
              <span className="text-gray-400">{showSuggestions ? '▲' : '▼'}</span>
            </button>
            {showSuggestions && (
              <div className="px-2 pb-2 flex flex-wrap gap-1.5 max-h-28 overflow-y-auto">
                {FAQ_SUGGESTIONS.map((f) => (
                  <button
                    key={f.label}
                    disabled={loading}
                    onClick={() => {
                      if (loading) return;
                      setMessages((m) => [
                        ...m,
                        { from: 'user', text: f.label },
                        { from: 'bot', text: f.answer },
                      ]);
                    }}
                    className="text-xs px-2 py-1 rounded-full bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-100 transition disabled:opacity-50"
                  >
                    {f.label}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Input */}
          <div className="p-2 border-t border-gray-200 bg-white flex gap-2">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={onKeyDown}
              placeholder="Type your question…"
              className="flex-1 px-3 py-2 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-400 text-sm"
              disabled={loading}
            />
            <button
              onClick={() => send()}
              disabled={loading || !input.trim()}
              className="px-4 py-2 rounded-full bg-indigo-600 text-white text-sm font-medium disabled:opacity-50 hover:bg-indigo-700"
            >
              Send
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default Chatbot;

import React, { useContext, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import { sendChatbotMessage } from '../../services/chatbotService';
import '../../styles/chatbot.css';

const QUICK_PROMPTS = [
  'Show upcoming events',
  'How to register for an event',
  'My registrations',
  'My tickets',
  'Payment help'
];

const BOT_WELCOME = {
  role: 'bot',
  text: 'Hi, I am your Eventra assistant. Ask me about events, registrations, tickets, and payments.',
  suggestions: QUICK_PROMPTS,
  links: [
    { label: 'Browse Home', url: '/' },
    { label: 'My Dashboard', url: '/userdashboard' },
  ],
};

function ChatbotWidget() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const chatBodyRef = useRef(null);
  const inputRef = useRef(null);
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState([BOT_WELCOME]);

  useEffect(() => {
    if (!isOpen) return;

    if (chatBodyRef.current) {
      chatBodyRef.current.scrollTop = chatBodyRef.current.scrollHeight;
    }

    if (inputRef.current && !loading) {
      inputRef.current.focus();
    }
  }, [messages, loading, isOpen]);

  const appendUserMessage = (text) => {
    setMessages((prev) => [...prev, { role: 'user', text }]);
  };

  const appendBotMessage = (text, suggestions = [], links = []) => {
    setMessages((prev) => [...prev, { role: 'bot', text, suggestions, links }]);
  };

  const sendMessage = async (rawMessage) => {
    const message = (rawMessage || '').trim();
    if (!message || loading) return;

    setLoading(true);
    appendUserMessage(message);
    setInput('');

    try {
      const userId = user?.userId ?? user?.id ?? null;
      const payload = await sendChatbotMessage(message, userId);
      appendBotMessage(payload?.reply || 'I am here to help.', payload?.suggestions || [], payload?.links || []);
    } catch (error) {
      appendBotMessage(error?.message || 'Unable to connect to chatbot right now.');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    await sendMessage(input);
  };

  const asNumberOrNull = (value) => {
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
  };

  const handleLinkClick = (item) => {
    if (!item) return;

    const routeType = item.routeType || 'generic';
    const url = item.url || '/';
    const meta = item.meta || {};

    if (routeType === 'payments') {
      const registrationId = asNumberOrNull(meta.registrationId);
      const eventId = asNumberOrNull(meta.eventId);
      const amountRupees = asNumberOrNull(meta.amountRupees) || 499;

      navigate(url, {
        state: {
          registrationId,
          eventId,
          amountRupees,
        },
      });
      setIsOpen(false);
      return;
    }

    if (routeType === 'event') {
      const eventId = asNumberOrNull(meta.eventId);
      if (eventId) {
        navigate(`/events/${eventId}`);
      } else {
        navigate(url);
      }
      setIsOpen(false);
      return;
    }

    navigate(url);
    setIsOpen(false);
  };

  return (
    <>
      <button
        type="button"
        className="chatbot-toggle"
        onClick={() => setIsOpen((prev) => !prev)}
      >
        {isOpen ? 'Close Chat' : 'Need Help?'}
      </button>

      {isOpen && (
        <section className="chatbot-panel" aria-label="Chatbot panel">
          <header className="chatbot-header">
            <h3>Eventra Assistant</h3>
            <span className="chatbot-status">Online</span>
          </header>

          <div className="chatbot-body" ref={chatBodyRef}>
            {messages.map((msg, index) => (
              <div key={`${msg.role}-${index}`} className={`chatbot-msg ${msg.role}`}>
                <p>{msg.text}</p>
                {msg.role === 'bot' && Array.isArray(msg.suggestions) && msg.suggestions.length > 0 && (
                  <div className="chatbot-suggestions">
                    {msg.suggestions.map((item) => (
                      <button
                        type="button"
                        key={item}
                        onClick={() => sendMessage(item)}
                        disabled={loading}
                      >
                        {item}
                      </button>
                    ))}
                  </div>
                )}
                {msg.role === 'bot' && Array.isArray(msg.links) && msg.links.length > 0 && (
                  <div className="chatbot-links">
                    {msg.links.map((item, idx) => (
                      <button
                        type="button"
                        key={`${item.url}-${idx}`}
                        className="chatbot-link-item"
                        onClick={() => handleLinkClick(item)}
                        disabled={loading}
                      >
                        {item.label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {loading && <p className="chatbot-thinking">Thinking...</p>}
          </div>

          <form className="chatbot-input-wrap" onSubmit={onSubmit}>
            <input
              ref={inputRef}
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about events, tickets, or payments"
              disabled={loading}
            />
            <button type="submit" disabled={loading || !input.trim()}>
              Send
            </button>
          </form>
        </section>
      )}
    </>
  );
}

export default ChatbotWidget;

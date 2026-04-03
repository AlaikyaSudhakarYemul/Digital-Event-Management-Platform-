import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  createEventForOrganizer,
  fetchAddresses,
  fetchSpeakers,
} from '../../services/eventService';

const STEPS = {
  EVENT_NAME: 'EVENT_NAME',
  DESCRIPTION: 'DESCRIPTION',
  DATE: 'DATE',
  TIME: 'TIME',
  TYPE: 'TYPE',
  MAX_ATTENDEES: 'MAX_ATTENDEES',
  SPEAKER: 'SPEAKER',
  ADDRESS: 'ADDRESS',
  CONFIRM: 'CONFIRM',
  DONE: 'DONE',
};

const BOT_AVATAR = 'EventBot';

function normalizeType(value) {
  const input = String(value || '').trim().toLowerCase();
  if (['1', 'in_person', 'in-person', 'in person'].includes(input)) return 'IN_PERSON';
  if (['2', 'virtual', 'online'].includes(input)) return 'VIRTUAL';
  if (['3', 'hybrid'].includes(input)) return 'HYBRID';
  return null;
}

function validDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const picked = new Date(`${value}T00:00:00`);
  if (Number.isNaN(picked.getTime())) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return picked >= today;
}

function validTime(value) {
  return /^([01]\d|2[0-3]):[0-5]\d$/.test(value);
}

function addressLabel(address) {
  return [address?.address, address?.city, address?.state, address?.country]
    .filter(Boolean)
    .join(', ');
}

function summary(draft, speaker, address) {
  return [
    `Event Name: ${draft.eventName}`,
    `Description: ${draft.description}`,
    `Date: ${draft.date}`,
    `Time: ${draft.time}`,
    `Type: ${draft.eventType}`,
    `Max Attendees: ${draft.maxAttendees}`,
    `Speaker: ${speaker?.name || '-'}`,
    `Address: ${draft.eventType === 'VIRTUAL' ? 'Not required' : addressLabel(address) || '-'}`,
  ].join('\n');
}

export default function OrganizerEventChatBot({ organizerId, token, onEventCreated }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [step, setStep] = useState(STEPS.EVENT_NAME);
  const [draft, setDraft] = useState({
    eventName: '',
    description: '',
    date: '',
    time: '',
    eventType: '',
    maxAttendees: '',
    speakerId: null,
    addressId: null,
  });
  const [speakers, setSpeakers] = useState([]);
  const [addresses, setAddresses] = useState([]);
  const [busy, setBusy] = useState(false);
  const [loadError, setLoadError] = useState('');

  const scrollRef = useRef(null);

  const selectedSpeaker = useMemo(
    () => speakers.find((s) => Number(s.speakerId) === Number(draft.speakerId)),
    [speakers, draft.speakerId]
  );

  const selectedAddress = useMemo(
    () => addresses.find((a) => Number(a.addressId) === Number(draft.addressId)),
    [addresses, draft.addressId]
  );

  const pushBotMessage = (text) => {
    setMessages((prev) => [...prev, { role: 'bot', text }]);
  };

  const pushUserMessage = (text) => {
    setMessages((prev) => [...prev, { role: 'user', text }]);
  };

  const resetConversation = () => {
    setStep(STEPS.EVENT_NAME);
    setDraft({
      eventName: '',
      description: '',
      date: '',
      time: '',
      eventType: '',
      maxAttendees: '',
      speakerId: null,
      addressId: null,
    });
    setMessages([]);
    setTimeout(() => {
      pushBotMessage('Hi Organizer. I will help you create an event in a few steps.');
      pushBotMessage('Step 1/8: What is the event name?');
    }, 0);
  };

  useEffect(() => {
    let mounted = true;

    const loadData = async () => {
      try {
        const [speakerList, addressList] = await Promise.all([
          fetchSpeakers(),
          fetchAddresses(),
        ]);
        if (!mounted) return;
        setSpeakers(Array.isArray(speakerList) ? speakerList : []);
        setAddresses(Array.isArray(addressList) ? addressList : []);
        resetConversation();
      } catch {
        if (!mounted) return;
        setLoadError('Could not load speakers/addresses. Please refresh and try again.');
      }
    };

    loadData();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const submitEvent = async () => {
    if (!organizerId) {
      pushBotMessage('I could not identify organizer account. Please login again.');
      return;
    }

    const payload = {
      eventName: draft.eventName,
      description: draft.description,
      date: draft.date,
      time: draft.time,
      eventType: draft.eventType,
      maxAttendees: Number(draft.maxAttendees),
      speakers: [{ speakerId: Number(draft.speakerId) }],
      address:
        draft.eventType === 'VIRTUAL'
          ? null
          : { addressId: Number(draft.addressId) },
      image: null,
    };

    try {
      setBusy(true);
      const created = await createEventForOrganizer({
        eventData: payload,
        token,
        userId: organizerId,
      });
      pushBotMessage(`Success. Event created with ID ${created?.eventId ?? '-'}.`);
      pushBotMessage('Type restart to create another event.');
      setStep(STEPS.DONE);
      if (typeof onEventCreated === 'function') onEventCreated(created);
    } catch (error) {
      pushBotMessage(error?.message || 'Event creation failed. Please try again.');
      pushBotMessage('Reply yes to retry submit, or restart to fill details again.');
      setStep(STEPS.CONFIRM);
    } finally {
      setBusy(false);
    }
  };

  const processInput = async (value) => {
    const text = String(value || '').trim();
    if (!text) return;

    pushUserMessage(text);

    if (text.toLowerCase() === 'restart') {
      resetConversation();
      return;
    }

    if (step === STEPS.EVENT_NAME) {
      if (text.length < 3) {
        pushBotMessage('Event name should be at least 3 characters. Try again.');
        return;
      }
      setDraft((prev) => ({ ...prev, eventName: text }));
      setStep(STEPS.DESCRIPTION);
      pushBotMessage('Step 2/8: Enter event description (10 to 100 characters).');
      return;
    }

    if (step === STEPS.DESCRIPTION) {
      if (text.length < 10 || text.length > 100) {
        pushBotMessage('Description must be between 10 and 100 characters. Try again.');
        return;
      }
      setDraft((prev) => ({ ...prev, description: text }));
      setStep(STEPS.DATE);
      pushBotMessage('Step 3/8: Enter event date in YYYY-MM-DD format.');
      return;
    }

    if (step === STEPS.DATE) {
      if (!validDate(text)) {
        pushBotMessage('Please provide a valid date today or in the future as YYYY-MM-DD.');
        return;
      }
      setDraft((prev) => ({ ...prev, date: text }));
      setStep(STEPS.TIME);
      pushBotMessage('Step 4/8: Enter event time in 24-hour HH:mm format (example 14:30).');
      return;
    }

    if (step === STEPS.TIME) {
      if (!validTime(text)) {
        pushBotMessage('Invalid time. Use HH:mm in 24-hour format (example 09:15).');
        return;
      }
      setDraft((prev) => ({ ...prev, time: text }));
      setStep(STEPS.TYPE);
      pushBotMessage('Step 5/8: Enter event type: IN_PERSON, VIRTUAL, or HYBRID.');
      return;
    }

    if (step === STEPS.TYPE) {
      const eventType = normalizeType(text);
      if (!eventType) {
        pushBotMessage('Unknown type. Enter IN_PERSON, VIRTUAL, or HYBRID.');
        return;
      }
      setDraft((prev) => ({ ...prev, eventType, addressId: eventType === 'VIRTUAL' ? null : prev.addressId }));
      setStep(STEPS.MAX_ATTENDEES);
      pushBotMessage('Step 6/8: Enter max attendees (10 to 500).');
      return;
    }

    if (step === STEPS.MAX_ATTENDEES) {
      const num = Number(text);
      if (!Number.isInteger(num) || num < 10 || num > 500) {
        pushBotMessage('Max attendees must be a whole number between 10 and 500.');
        return;
      }
      setDraft((prev) => ({ ...prev, maxAttendees: num }));
      setStep(STEPS.SPEAKER);
      const topSpeakers = speakers.slice(0, 8)
        .map((s) => `${s.speakerId}: ${s.name}`)
        .join('\n');
      pushBotMessage(`Step 7/8: Pick speaker by ID or name.\n${topSpeakers || 'No speakers available.'}`);
      return;
    }

    if (step === STEPS.SPEAKER) {
      let pick = speakers.find((s) => Number(s.speakerId) === Number(text));
      if (!pick) {
        pick = speakers.find((s) => (s.name || '').toLowerCase() === text.toLowerCase());
      }
      if (!pick) {
        pick = speakers.find((s) => (s.name || '').toLowerCase().includes(text.toLowerCase()));
      }
      if (!pick) {
        pushBotMessage('Speaker not found. Reply with a valid speaker ID or exact name.');
        return;
      }

      const nextDraft = { ...draft, speakerId: pick.speakerId };
      setDraft(nextDraft);

      if (nextDraft.eventType === 'VIRTUAL') {
        setStep(STEPS.CONFIRM);
        pushBotMessage(`Step 8/8: Please confirm event creation. Reply YES to create or NO to cancel.\n${summary(nextDraft, pick, null)}`);
      } else {
        setStep(STEPS.ADDRESS);
        const topAddresses = addresses.slice(0, 8)
          .map((a) => `${a.addressId}: ${addressLabel(a)}`)
          .join('\n');
        pushBotMessage(`Step 8/8: Pick address by ID.\n${topAddresses || 'No addresses available.'}`);
      }
      return;
    }

    if (step === STEPS.ADDRESS) {
      const pick = addresses.find((a) => Number(a.addressId) === Number(text));
      if (!pick) {
        pushBotMessage('Address not found. Reply with a valid address ID.');
        return;
      }

      const nextDraft = { ...draft, addressId: pick.addressId };
      setDraft(nextDraft);
      setStep(STEPS.CONFIRM);
      pushBotMessage(`Please confirm event creation. Reply YES to create or NO to cancel.\n${summary(nextDraft, selectedSpeaker, pick)}`);
      return;
    }

    if (step === STEPS.CONFIRM) {
      if (text.toLowerCase() === 'yes') {
        await submitEvent();
        return;
      }
      if (text.toLowerCase() === 'no') {
        pushBotMessage('Creation cancelled. Type restart to start again.');
        setStep(STEPS.DONE);
        return;
      }
      pushBotMessage('Please reply YES or NO.');
      return;
    }

    if (step === STEPS.DONE) {
      pushBotMessage('Type restart to create a new event.');
    }
  };

  const onSend = async () => {
    if (busy) return;
    const text = input.trim();
    if (!text) return;
    setInput('');
    await processInput(text);
  };

  return (
    <div className="rounded-xl border border-gray-200 bg-white shadow-sm h-[560px] flex flex-col">
      <div className="px-4 py-3 border-b border-gray-200 bg-[#eef2f7] rounded-t-xl">
        <h3 className="font-semibold text-gray-800">Organizer EventBot</h3>
        <p className="text-xs text-gray-500 mt-1">Type restart any time to refill the form.</p>
      </div>

      {loadError ? (
        <div className="p-4 text-sm text-red-600">{loadError}</div>
      ) : (
        <>
          <div ref={scrollRef} className="flex-1 overflow-y-auto p-4 space-y-3 bg-[#f9fbff]">
            {messages.map((m, idx) => (
              <div key={`${m.role}-${idx}`} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[80%] px-3 py-2 rounded-lg text-sm whitespace-pre-line ${
                    m.role === 'user'
                      ? 'bg-[#0f1b2d] text-white rounded-br-none'
                      : 'bg-white border border-gray-200 text-gray-800 rounded-bl-none'
                  }`}
                >
                  {m.role === 'bot' ? <div className="text-[11px] text-gray-500 mb-1">{BOT_AVATAR}</div> : null}
                  {m.text}
                </div>
              </div>
            ))}
          </div>

          <div className="p-3 border-t border-gray-200 bg-white">
            <div className="flex items-center gap-2">
              <input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') onSend();
                }}
                placeholder="Type your answer..."
                className="flex-1 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#11c5d5]"
                disabled={busy}
              />
              <button
                onClick={onSend}
                disabled={busy}
                className="px-4 py-2 rounded-md bg-[#11c5d5] text-white text-sm hover:bg-[#0fb4c3] disabled:opacity-70"
              >
                Send
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

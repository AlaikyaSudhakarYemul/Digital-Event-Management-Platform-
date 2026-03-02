import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AddressManager from './AddressManagement';
import SpeakerManager from './SpeakerManagement';

// Helper: safe fetch with auth and consistent error handling
const authorizedFetch = async (url, options = {}) => {
  const token = localStorage.getItem('adminToken');
  const res = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });

  // Try parse JSON if content-type is JSON
  const isJson = res.headers.get('content-type')?.includes('application/json');
  let data = null;
  try {
    data = isJson ? await res.json() : null;
  } catch (_) {
    // ignore parse errors
    data = null;
  }

  if (!res.ok) {
    const error = new Error(
      data?.message ||
      data?.error ||
      `Request failed with ${res.status}`
    );
    error.status = res.status;
    error.body = data;
    throw error;
  }

  return data;
};

const AdminDashboard = () => {
  const [activeSection, setActiveSection] = useState('address');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    navigate('/admin/login');
  };

  // Address state
  const emptyAddress = { address: '', city: '', state: '', country: '', pincode: '' };
  const [addressForm, setAddressForm] = useState(emptyAddress);
  const [addresses, setAddresses] = useState([]);
  const [editingAddressId, setEditingAddressId] = useState(null);
  const [addressMessage, setAddressMessage] = useState('');
  const [addressErrors, setAddressErrors] = useState({}); // NEW

  // Speaker state
  const [speakerForm, setSpeakerForm] = useState({ name: '', bio: '' });
  const [speakers, setSpeakers] = useState([]);
  const [editingSpeakerId, setEditingSpeakerId] = useState(null);
  const [speakerMessage, setSpeakerMessage] = useState('');

  // Fetch data
  useEffect(() => {
    (async () => {
      try {
        const addrData = await authorizedFetch('http://localhost:8080/api/admin/all');
        setAddresses(addrData || []);
      } catch (e) {
        console.error('Load addresses failed:', e);
      }

      try {
        const spkData = await authorizedFetch('http://localhost:8080/api/speakers');
        setSpeakers(spkData || []);
      } catch (e) {
        console.error('Load speakers failed:', e);
      }
    })();
  }, []);

  // Address handlers
  const handleAddressChange = (e) => {
    const { name, value } = e.target;
    setAddressForm((prev) => ({ ...prev, [name]: value }));
    // clear field error on change
    setAddressErrors((prev) => ({ ...prev, [name]: '' }));
    setAddressMessage('');
  };

  // Simple client validation mirroring backend
  const validateAddressForm = (form) => {
    const errors = {};
    const trimmed = {
      address: (form.address ?? '').trim(),
      city: (form.city ?? '').trim(),
      state: (form.state ?? '').trim(),
      country: (form.country ?? '').trim(),
      pincode: (form.pincode ?? '').trim()
    };

    if (trimmed.address.length < 6 || trimmed.address.length > 50) {
      errors.address = 'Address must be 6–50 characters';
    }
    if (!trimmed.city) errors.city = 'City is required';
    if (!trimmed.state) errors.state = 'State is required';
    if (!trimmed.country) errors.country = 'Country is required';
    if (!/^\d{5,6}$/.test(trimmed.pincode)) {
      errors.pincode = 'Pincode must be 5 or 6 digits';
    }

    return { errors, cleaned: trimmed };
  };

  const reloadAddresses = async () => {
    const data = await authorizedFetch('http://localhost:8080/api/admin/all');
    setAddresses(data || []);
  };

  const handleAddressSubmit = async (e) => {
    e.preventDefault();
    setAddressMessage('');
    setAddressErrors({});

    const { errors, cleaned } = validateAddressForm(addressForm);
    if (Object.keys(errors).length > 0) {
      setAddressErrors(errors);
      setAddressMessage('Please fix the highlighted fields.');
      return;
    }

    setSubmitting(true);
    const method = editingAddressId ? 'PUT' : 'POST';
    const url = editingAddressId
      ? `http://localhost:8080/api/admin/${editingAddressId}`
      : 'http://localhost:8080/api/admin/add';

    try {
      await authorizedFetch(url, {
        method,
        body: JSON.stringify(cleaned)
      });

      setAddressMessage(editingAddressId ? 'Address updated!' : 'Address added!');
      setAddressForm(emptyAddress);
      setEditingAddressId(null);
      await reloadAddresses();
    } catch (err) {

      if (err.body?.errors && Array.isArray(err.body.errors)) {       
        const fieldErrors = {};
        err.body.errors.forEach((e) => {
          if (e.field && e.message) fieldErrors[e.field] = e.message;
        });
        setAddressErrors(fieldErrors);
        setAddressMessage('Validation failed. Please review the fields.');
      } else if (err.body?.message) {
        setAddressMessage(err.body.message);
      } else {
        setAddressMessage(err.message || 'Request failed.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddressEdit = (addr) => {
    setAddressForm({
      address: addr.address ?? '',
      city: addr.city ?? '',
      state: addr.state ?? '',
      country: addr.country ?? '',
      pincode: String(addr.pincode ?? '')
    });
    setEditingAddressId(addr.addressId);
    setAddressErrors({});
    setAddressMessage('');
  };

  const handleAddressDelete = async (id) => {
    try {
      await authorizedFetch(`http://localhost:8080/api/admin/${id}`, { method: 'DELETE' });
      await reloadAddresses();
    } catch (e) {
      console.error('Delete failed:', e);
    }
  };

  
  const handleSpeakerChange = (e) => {
    const { name, value } = e.target;
    setSpeakerForm((prev) => ({ ...prev, [name]: value }));
    setSpeakerMessage('');
  };

  const handleSpeakerSubmit = async (e) => {
    e.preventDefault();
    const method = editingSpeakerId ? 'PUT' : 'POST';
    const url = editingSpeakerId
      ? `http://localhost:8080/api/speakers/${editingSpeakerId}`
      : 'http://localhost:8080/api/speakers';

    try {
      await authorizedFetch(url, {
        method,
        body: JSON.stringify(speakerForm)
      });
      setSpeakerMessage(editingSpeakerId ? 'Speaker updated!' : 'Speaker added!');
      setSpeakerForm({ name: '', bio: '' });
      setEditingSpeakerId(null);
      const data = await authorizedFetch('http://localhost:8080/api/speakers');
      setSpeakers(data || []);
    } catch (e2) {
      setSpeakerMessage(e2.body?.message || e2.message || 'Speaker request failed.');
    }
  };

  const handleSpeakerEdit = (spk) => {
    setSpeakerForm({ name: spk.name ?? '', bio: spk.bio ?? '' });
    setEditingSpeakerId(spk.speakerId);
    setSpeakerMessage('');
  };

  const handleSpeakerDelete = async (id) => {
    try {
      await authorizedFetch(`http://localhost:8080/api/speakers/${id}`, { method: 'DELETE' });
      const data = await authorizedFetch('http://localhost:8080/api/speakers');
      setSpeakers(data || []);
    } catch (e) {
      console.error('Speaker delete failed:', e);
    }
  };

  return (
    <div className="flex h-screen bg-gray-900 text-white">
      {/* Sidebar */}
      <div className="w-1/4 bg-gray-800 p-6 space-y-4 flex flex-col h-full">
        <h2 className="text-xl font-bold mb-4">Admin Panel</h2>
        <button
          className={`w-full text-left px-4 py-2 rounded ${activeSection === 'address' ? 'bg-cyan-600' : 'bg-gray-700'}`}
          onClick={() => setActiveSection('address')}
        >
          Address
        </button>
        <button
          className={`w-full text-left px-4 py-2 rounded ${activeSection === 'speaker' ? 'bg-cyan-600' : 'bg-gray-700'}`}
          onClick={() => setActiveSection('speaker')}
        >
          Speaker
        </button>
        <div className="flex-grow" />
        <button
          className="w-full mt-8 px-4 py-2 rounded bg-red-600 hover:bg-red-700 text-white font-semibold"
          onClick={handleLogout}
        >
          Logout
        </button>
      </div>

      {/* Main Content */}
      <div className="w-3/4 p-6 overflow-y-auto">
        {activeSection === 'address' && (
          <AddressManager
            addressForm={addressForm}
            handleAddressChange={handleAddressChange}
            handleAddressSubmit={handleAddressSubmit}
            addresses={addresses}
            handleAddressEdit={handleAddressEdit}
            handleAddressDelete={handleAddressDelete}
            editingAddressId={editingAddressId}
            addressMessage={addressMessage}
            addressErrors={addressErrors}
            submitting={submitting}
          />
        )}
        {activeSection === 'speaker' && (
          <SpeakerManager
            speakerForm={speakerForm}
            handleSpeakerChange={handleSpeakerChange}
            handleSpeakerSubmit={handleSpeakerSubmit}
            speakers={speakers}
            handleSpeakerEdit={handleSpeakerEdit}
            handleSpeakerDelete={handleSpeakerDelete}
            editingSpeakerId={editingSpeakerId}
            speakerMessage={speakerMessage}
          />
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
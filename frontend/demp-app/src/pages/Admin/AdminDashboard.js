import React, { useState, useEffect } from 'react';
import AddressManager from './AddressManagement';
import SpeakerManager from './SpeakerManagement';
 
// Helper function to include Authorization header
const authorizedFetch = (url, options = {}) => {
  const token = localStorage.getItem('adminToken');
  return fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });
};
 
const AdminDashboard = () => {
  const [activeSection, setActiveSection] = useState('address');
 
  // Address state
  const [addressForm, setAddressForm] = useState({
    address: '',
    state: '',
    country: '',
    pincode: ''
  });
  const [addresses, setAddresses] = useState([]);
  const [editingAddressId, setEditingAddressId] = useState(null);
  const [addressMessage, setAddressMessage] = useState('');
 
  // Speaker state
  const [speakerForm, setSpeakerForm] = useState({
    name: '',
    bio: ''
  });
  const [speakers, setSpeakers] = useState([]);
  const [editingSpeakerId, setEditingSpeakerId] = useState(null);
  const [speakerMessage, setSpeakerMessage] = useState('');
 
  // Fetch data
  useEffect(() => {
    authorizedFetch('http://localhost:8080/api/admin/all')
      .then(res => res.json())
      .then(data => setAddresses(data));
 
    authorizedFetch('http://localhost:8080/api/speakers')
      .then(res => res.json())
      .then(data => setSpeakers(data));
  }, []);
 
  // Address handlers
  const handleAddressChange = (e) => {
    const { name, value } = e.target;
    setAddressForm(prev => ({ ...prev, [name]: value }));
  };
 
  const handleAddressSubmit = (e) => {
    e.preventDefault();
    const method = editingAddressId ? 'PUT' : 'POST';
    const url = editingAddressId
      ? `http://localhost:8080/api/admin/${editingAddressId}`
      : 'http://localhost:8080/api/admin/add';
 
    authorizedFetch(url, {
      method,
      body: JSON.stringify(addressForm)
    })
      .then(res => res.json())
      .then(() => {
        setAddressMessage(editingAddressId ? 'Address updated!' : 'Address added!');
        setAddressForm({ address: '', state: '', country: '', pincode: '' });
        setEditingAddressId(null);
        return authorizedFetch('http://localhost:8080/api/admin/all');
      })
      .then(res => res.json())
      .then(data => setAddresses(data));
  };
 
  const handleAddressEdit = (addr) => {
    setAddressForm(addr);
    setEditingAddressId(addr.addressId);
  };
 
  const handleAddressDelete = (id) => {
    authorizedFetch(`http://localhost:8080/api/admin/${id}`, { method: 'DELETE' })
      .then(() => authorizedFetch('http://localhost:8080/api/admin/all'))
      .then(res => res.json())
      .then(data => setAddresses(data));
  };
 
  // Speaker handlers
  const handleSpeakerChange = (e) => {
    const { name, value } = e.target;
    setSpeakerForm(prev => ({ ...prev, [name]: value }));
  };
 
  const handleSpeakerSubmit = (e) => {
    e.preventDefault();
    const method = editingSpeakerId ? 'PUT' : 'POST';
    const url = editingSpeakerId
      ? `http://localhost:8080/api/speakers/${editingSpeakerId}`
      : 'http://localhost:8080/api/speakers';
 
    authorizedFetch(url, {
      method,
      body: JSON.stringify(speakerForm)
    })
      .then(res => res.json())
      .then(() => {
        setSpeakerMessage(editingSpeakerId ? 'Speaker updated!' : 'Speaker added!');
        setSpeakerForm({ name: '', bio: '' });
        setEditingSpeakerId(null);
        return authorizedFetch('http://localhost:8080/api/speakers');
      })
      .then(res => res.json())
      .then(data => setSpeakers(data));
  };
 
  const handleSpeakerEdit = (spk) => {
    setSpeakerForm(spk);
    setEditingSpeakerId(spk.speakerId);
  };
 
  const handleSpeakerDelete = (id) => {
    authorizedFetch(`http://localhost:8080/api/speakers/${id}`, { method: 'DELETE' })
      .then(() => authorizedFetch('http://localhost:8080/api/speakers'))
      .then(res => res.json())
      .then(data => setSpeakers(data));
  };
 
  return (
    <div className="flex h-screen bg-gray-900 text-white">
      {/* Sidebar */}
      <div className="w-1/4 bg-gray-800 p-6 space-y-4">
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
 
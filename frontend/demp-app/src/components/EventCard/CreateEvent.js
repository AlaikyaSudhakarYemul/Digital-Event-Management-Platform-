import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Calendar, ImageIcon, BookText
} from 'lucide-react';

const EventCreatePage = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    eventName: '',
    description: '',
    date: '',
    time: '',
    speakerId: '',
    addressId: '',
    eventTypeId: '',
    maxAttendees: ''
  });

  const [errors, setErrors] = useState({});
  const [imageBase64, setImageBase64] = useState('');
  const [imagePreview, setImagePreview] = useState(null);
  const [speakers, setSpeakers] = useState([]);
  const [addresses, setAddresses] = useState([]);

  useEffect(() => {
    fetch('http://localhost:8080/api/speakers')
      .then(res => res.json())
      .then(data => setSpeakers(data))
      .catch(err => console.error('Error fetching speakers:', err));

    fetch('http://localhost:8080/api/admin/all')
      .then(res => res.json())
      .then(data => setAddresses(data))
      .catch(err => console.error('Error fetching addresses:', err));
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImageBase64(reader.result);
        setImagePreview(URL.createObjectURL(file));
      };
      reader.readAsDataURL(file);
    }
  };

  const validateForm = () => {
    const newErrors = {};
  if (!formData.eventName.trim()) newErrors.eventName = 'Event name is required';
  if (!formData.description.trim()) newErrors.description = 'Description is required';
  if (!formData.date) newErrors.date = 'Event date is required';
  else if (new Date(formData.date) < new Date().setHours(0, 0, 0, 0)) newErrors.date = 'Event date cannot be in the past';
  if (!formData.time) newErrors.time = 'Event time is required';
  if (!formData.speakerId) newErrors.speakerId = 'Speaker selection is required';
  if (!formData.addressId) newErrors.addressId = 'Address selection is required';
  if (!formData.eventTypeId) newErrors.eventTypeId = 'Event type selection is required';
  if (!formData.maxAttendees) newErrors.maxAttendees = 'Max attendees is required';
  else if (isNaN(formData.maxAttendees) || formData.maxAttendees < 10 || formData.maxAttendees > 500) newErrors.maxAttendees = 'Max attendees must be between 10 and 500';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    

  
const selectedAddress = addresses.find(
    (addr) => addr.addressId.toString() === formData.addressId.toString()
  );


const eventTypeMap = {
    "1": "IN_PERSON",
    "2": "VIRTUAL",
    "3": "HYBRID"
  };


    
  // Combine date and time into ISO string for LocalDateTime
  const payload = {
    eventName: formData.eventName,
    description: formData.description,
    date: formData.date, // LocalDate (YYYY-MM-DD)
    time: formData.time, // LocalTime (HH:mm)
    speakers: [
      { speakerId: formData.speakerId }
    ],
    eventType: eventTypeMap[formData.eventTypeId],
    image: imageBase64 || null,
    address: selectedAddress || null, // ✅ Only full address, no addressId separately
    maxAttendees: parseInt(formData.maxAttendees, 10)
  };


    console.log(payload);

    try {
      // Get user and token from localStorage
  const userObj = JSON.parse(localStorage.getItem('user'));
  const token = localStorage.getItem('auth_token');
  // Add user to payload
  payload.user = { userId: userObj?.userId };
      const response = await fetch('http://localhost:8080/api/events/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Backend error:', errorText);
        alert('Something went wrong while creating the event!\n' + errorText);
        return;
      }

      alert('Event created successfully!');
      navigate('/');
    } catch (error) {
      console.error('Error:', error);
      alert('Something went wrong while creating the event!');
    }
  };

  const renderInput = (name, label, icon, type = 'text', readOnly = false) => (
    <div key={name} className="flex flex-col">
      <label htmlFor={name} className="text-sm font-medium mb-1">{label}</label>
      <div className="flex items-center bg-gray-800 rounded-lg border border-gray-700 px-3 py-2">
        <div className="text-gray-400 mr-3">{icon}</div>
        <input
          id={name}
          name={name}
          type={type}
          value={formData[name]}
          onChange={handleChange}
          readOnly={readOnly}
          className="flex-1 bg-transparent outline-none text-white placeholder-gray-400"
          placeholder={label}
        />
      </div>
      {errors[name] && <span className="text-red-400 text-sm mt-1">{errors[name]}</span>}
    </div>
  );

  return (
    <div className="min-h-screen bg-cover bg-center relative" style={{ backgroundImage: 'url("/event image.jpg")' }}>
      <div className="absolute inset-0 bg-black bg-opacity-60 z-0"></div>
      <div className="relative z-10 max-w-4xl mx-auto px-6 py-12">
        <div className="bg-white/10 backdrop-blur-md rounded-2xl p-8 shadow-2xl text-white">
          <h2 className="text-3xl font-bold mb-6 text-center text-cyan-300">Create New Event</h2>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {renderInput('eventName', 'Event Name', <BookText />)}
              {renderInput('description', 'Event Description', <BookText />)}
              {renderInput('date', 'Event Date', <Calendar />, 'date')}
              {renderInput('time', 'Event Time', <Calendar />, 'time')}
              {renderInput('maxAttendees', 'Max Attendees', <BookText />, 'number')}

              <div className="flex flex-col">
                <label htmlFor="speakerId" className="text-sm font-medium mb-1">Select Speaker</label>
                <select
                  id="speakerId"
                  name="speakerId"
                  value={formData.speakerId}
                  onChange={handleChange}
                  className="bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2"
                  required
                >
                  <option value="">-- Select a Speaker --</option>
                  {speakers.map(speaker => (
                    <option key={speaker.speakerId} value={speaker.speakerId}>
                      {speaker.name}
                    </option>
                  ))}
                </select>
                {errors.speakerId && <span className="text-red-400 text-sm mt-1">{errors.speakerId}</span>}
              </div>

              <div className="flex flex-col">
                <label htmlFor="addressId" className="text-sm font-medium mb-1">Select Address</label>
                <select
                  id="addressId"
                  name="addressId"
                  value={formData.addressId}
                  onChange={handleChange}
                  className="bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2"
                  required
                >
                  <option value="">-- Select an Address --</option>
                  {addresses.map(address => (
                    <option key={address.addressId} value={address.addressId}>
                      {address.address}, {address.state}, {address.country} - {address.pincode}
                    </option>
                  ))}
                </select>
                {errors.addressId && <span className="text-red-400 text-sm mt-1">{errors.addressId}</span>}
              </div>

              <div className="flex flex-col">
                <label htmlFor="eventTypeId" className="text-sm font-medium mb-1">Select Event Type</label>
                <select
                  id="eventTypeId"
                  name="eventTypeId"
                  value={formData.eventTypeId}
                  onChange={handleChange}
                  className="bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2"
                  required
                >
                  <option value="">-- Select Event Type --</option>
                  <option value="1">IN_PERSON</option>
                  <option value="2">VIRTUAL</option>
                  <option value="3">HYBRID</option>
                </select>
                {errors.eventTypeId && <span className="text-red-400 text-sm mt-1">{errors.eventTypeId}</span>}
              </div>
            </div>

            <div className="flex flex-col">
              <label htmlFor="eventImage" className="text-sm font-medium mb-1">Upload Event Image (optional)</label>
              <div className="flex items-center bg-gray-800 rounded-lg border border-gray-700 px-3 py-2">
                <div className="text-gray-400 mr-3"><ImageIcon /></div>
                <input
                  type="file"
                  id="eventImage"
                  name="eventImage"
                  accept="image/*"
                  onChange={handleImageChange}
                  className="text-white"
                />
              </div>
              {imagePreview && (
                <img
                  src={imagePreview}
                  alt="Event Preview"
                  className="mt-3 h-40 object-cover rounded shadow-md"
                />
              )}
            </div>

            <button
              type="submit"
              className="w-full bg-cyan-500 hover:bg-cyan-600 text-white font-semibold py-2 px-4 rounded-lg mt-4"
            >
              Create Event
           
</button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default EventCreatePage;

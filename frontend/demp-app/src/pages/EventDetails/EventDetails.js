// src/pages/EventDetails.jsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

const EventDetails = () => {
  const { eventId } = useParams();
  const [event, setEvent] = useState(null);

useEffect(() => {
  console.log("Fetching event with ID:", eventId);
  fetch(`http://localhost:8080/api/events/${eventId}`)
    .then(res => {
      console.log("Fetch response:", res);
      return res.json();
    })
    .then(data => {
      console.log("Fetched event data:", data);
      setEvent(data);
    })
    .catch(err => console.error('Error fetching event:', err));
}, [eventId]);


  if (!event) return <div>Loading...</div>;

return (
  <div className="p-6 text-white bg-gray-800">
    <h1 className="text-3xl font-bold mb-4">{event.eventName}</h1>
    <p><strong>Date:</strong> {event.date}</p>
    <p><strong>Event Type:</strong> {event.eventType}</p>
    <p><strong>Location:</strong> {event.address?.address}</p>
    <p><strong>Full Address:</strong> {`${event.address?.address}, ${event.address?.state}, ${event.address?.country} - ${event.address?.pincode}`}</p>
    <p><strong>Description:</strong> {event.description}</p>
    {event.speakers?.length > 0 && (
      <div className="mt-4">
        <h2 className="text-xl font-semibold">Speaker</h2>
        <p><strong>Name:</strong> {event.speakers[0].name}</p>
        <p><strong>Bio:</strong> {event.speakers[0].bio}</p>
      </div>
    )}
    {event.image && (
      <img src={event.image} alt={event.eventName} className="mt-4 w-full max-w-md rounded" />
    )}
  </div>
);



};

export default EventDetails;

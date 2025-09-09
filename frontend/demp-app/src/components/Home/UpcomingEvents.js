import React, { useState } from 'react';
 
const UpcomingEvents = () => {
 
  const [searchTerm, setSearchTerm] = useState('');
  // Hardcoded events data
  const events = [
    {
      title: 'Tech Conference 2025',
      date: '2025-09-15',
      tags: ['Technology', 'Conference', 'Networking']
    },
    {
      title: 'Art & Music Festival',
      date: '2025-10-01',
      tags: ['Art', 'Music', 'Festival']
    },
    {
      title: 'Startup Pitch Night',
      date: '2025-09-25',
      tags: ['Startup', 'Pitch', 'Entrepreneurship']
    }
  ];
 
  const filteredEvents = events.filter(event =>
    event.title.toLowerCase().includes(searchTerm.toLowerCase())
  );
 
  return (
    <section className="py-20 text-center text-white">
      <h2 className="text-3xl md:text-4xl font-bold mb-6">Upcoming Events</h2>
 
      {/* Search Bar */}
      <div className="mb-8">
        <input
          type="text"
          placeholder="Search by event name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="px-4 py-2 rounded-full text-black w-64"
        />
      </div>
 
      {/* Event Cards */}
      <div className="flex flex-col md:flex-row justify-center gap-8 px-6">
        {filteredEvents.map((event, idx) => (
          <div
            key={idx}
            className="bg-white/10 backdrop-blur-md rounded-xl shadow-md p-6 w-full md:w-80 text-white"
          >
            <h3 className="text-lg font-semibold mb-2">{event.title}</h3>
            <p className="text-sm text-gray-200 mb-3">{event.date}</p>
            <div className="flex flex-wrap gap-2 justify-center mb-4">
              {event.tags?.map((tag, i) => (
                <span
                  key={i}
                  className="text-xs bg-white/20 px-2 py-1 rounded-full text-white"
                >
                  {tag}
                </span>
              ))}
            </div>
            <button className="mt-2 px-4 py-2 border border-white text-white rounded-full hover:bg-white hover:text-purple-600 transition-colors">
              Learn More
            </button>
          </div>
        ))}
      </div>
 
      <div className="mt-12">
        <h3 className="text-xl font-semibold mb-4">Ready to get started?</h3>
        <button className="bg-white text-purple-600 px-6 py-3 rounded-full hover:bg-purple-200 transition-colors">
          Sign Up Now
        </button>
      </div>
    </section>
  );
};
 
export default UpcomingEvents;
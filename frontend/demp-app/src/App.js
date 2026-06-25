import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Home from './pages/Home/Home';
import TicketPortal from './pages/Tickets/TicketPortal';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/tickets" element={<TicketPortal />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
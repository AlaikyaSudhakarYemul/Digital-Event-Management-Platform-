import React from 'react';
import { CalendarDays, Users, CheckCircle } from 'lucide-react';
 
const HowItWorks = () => {
  return (
<section className="py-20 px-4 sm:px-8 md:px-16 text-white">
<h2 className="text-3xl sm:text-4xl font-bold text-center mb-12">How It Works</h2>
<div className="grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
<div className="flex flex-col items-center">
<Users className="w-12 h-12 mb-4 text-white" />
<h3 className="text-xl font-semibold mb-2">Register</h3>
<p className="text-sm opacity-80">Sign up as an organizer or attendee</p>
</div>
<div className="flex flex-col items-center">
<CalendarDays className="w-12 h-12 mb-4 text-white" />
<h3 className="text-xl font-semibold mb-2">Create/View Events</h3>
<p className="text-sm opacity-80">Organize your own events or discover new ones</p>
</div>
<div className="flex flex-col items-center">
<CheckCircle className="w-12 h-12 mb-4 text-white" />
<h3 className="text-xl font-semibold mb-2">Attend/Manage</h3>
<p className="text-sm opacity-80">Register for events and manage your bookings</p>
</div>
</div>
</section>
  );
};
 
export default HowItWorks;
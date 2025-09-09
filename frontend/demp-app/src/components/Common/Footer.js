import React from 'react';
 
const Footer = () => {
 
  return (
<footer className="bg-gray-900 text-gray-300 py-8 mt-16">
<div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row justify-between items-center">
<p className="text-sm">&copy; {new Date().getFullYear()} EVENTRA. All rights reserved.</p>
<div className="flex space-x-6 mt-4 md:mt-0">
<a href="#" className="hover:text-white transition-colors">Privacy Policy</a>
<a href="#" className="hover:text-white transition-colors">Terms of Service</a>
<a href="#" className="hover:text-white transition-colors">Support</a>
</div>
</div>
</footer>
 
  );
 
};
 
export default Footer;
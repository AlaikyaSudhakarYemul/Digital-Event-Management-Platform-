import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import './styles/tailwind.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { AuthProvider } from './contexts/AuthContext';
import { BrowserRouter } from 'react-router-dom'; // ✅ Import this

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <BrowserRouter> {/* ✅ Wrap everything inside this */}
    <AuthProvider>
      <React.StrictMode>
        <App />
      </React.StrictMode>
    </AuthProvider>
  </BrowserRouter>
);

reportWebVitals();
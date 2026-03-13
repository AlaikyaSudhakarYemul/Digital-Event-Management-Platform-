import React from "react";
import { useLocation, Link } from "react-router-dom";

export default function PaymentSuccess() {
  const { state } = useLocation() || {};

  return (
    <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center p-6">
      <div className="bg-gray-800 rounded-lg shadow-lg p-8 max-w-md w-full">
        <h2 className="text-2xl font-bold text-green-400 mb-4">Payment Successful ✅</h2>
        <p className="mb-2">Order ID: {state?.orderId || "-"}</p>
        <p className="mb-6">Payment ID: {state?.paymentId || "-"}</p>
        <Link to="/" className="inline-block bg-green-600 hover:bg-green-700 px-4 py-2 rounded">
          Back to Home
        </Link>
      </div>
    </div>
  );
}

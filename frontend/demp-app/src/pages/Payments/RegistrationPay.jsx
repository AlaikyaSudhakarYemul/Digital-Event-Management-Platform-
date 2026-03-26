import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { getToken } from "../../services/authService";

const API_BASE = process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080";

const loadRazorpayScript = () =>
  new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const existingScript = document.querySelector("script[data-razorpay='true']");
    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(true));
      existingScript.addEventListener("error", () => resolve(false));
      return;
    }

    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    script.dataset.razorpay = "true";
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });

export default function RegistrationPay({ registrationId: propRegistrationId, eventId: propEventId, amountRupees: propAmountRupees = 499 }) {
  const [busy, setBusy] = useState(false);
  const [sdkReady, setSdkReady] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  const state = location?.state;
  const registrationId = propRegistrationId ?? state?.registrationId ?? null;
  const eventId = propEventId ?? state?.eventId ?? null;
  const ticketCount = Number(state?.ticketCount ?? 1);
  const amountRupees = Number(propAmountRupees ?? state?.amountRupees ?? 499);

  useEffect(() => {
    (async () => {
      const ok = await loadRazorpayScript();
      setSdkReady(ok);
      if (!ok) setError("Razorpay SDK failed to load. Please refresh and try again.");
    })();
  }, []);

  async function handlePay() {
    try {
      setBusy(true);
      setError("");

      const token = getToken();
      if (!token) {
        throw new Error("Please login before proceeding to payment.");
      }

      const res = await fetch(`${API_BASE}/api/payments/orders`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          registrationId,
          eventId,
          amountRupees,
        }),
      });

      if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || "Order creation failed");
      }

      const data = await res.json();

      const notifyCancelled = async (cancelReason) => {
        try {
          await fetch(`${API_BASE}/api/payments/cancel`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              registrationId,
              razorpayOrderId: data.razorpayOrderId,
              reason: cancelReason,
            }),
          });
        } catch (cancelErr) {
          console.error("Failed to persist cancelled payment attempt:", cancelErr);
        }
      };

      if (!window.Razorpay) {
        throw new Error("Razorpay SDK unavailable.");
      }

      const options = {
        key: data.keyId,
        amount: data.amountPaise,
        currency: data.currency || "INR",
        name: "Digital Event Management Platform",
        description: "Event Registration Payment",
        order_id: data.razorpayOrderId,
        theme: { color: "#3399cc" },
        handler: async function (response) {
          try {
            const vr = await fetch(`${API_BASE}/api/payments/verify`, {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
              },
              body: JSON.stringify(response),
            });

            if (!vr.ok) {
              const verifyError = await vr.text();
              throw new Error(verifyError || "Payment verification failed");
            }

            const result = await vr.json();
            if (result.signatureValid) {
              navigate("/payment-success", {
                state: {
                  orderId: data.razorpayOrderId,
                  paymentId: response.razorpay_payment_id,
                },
              });
            } else {
              navigate("/payment-failed", {
                state: {
                  orderId: data.razorpayOrderId,
                  paymentId: response.razorpay_payment_id,
                },
              });
            }
          } catch (verifyErr) {
            setError(verifyErr.message || "Verification failed");
            navigate("/payment-failed", {
              state: {
                orderId: data.razorpayOrderId,
                paymentId: response.razorpay_payment_id,
              },
            });
          }
        },
        modal: {
          ondismiss: async function () {
            await notifyCancelled("USER_DISMISSED_CHECKOUT");
            setBusy(false);
          },
        },
      };

      const rzp = new window.Razorpay(options);

      rzp.on("payment.failed", async function () {
        await notifyCancelled("PAYMENT_FAILED");
        navigate("/payment-failed", { state: { orderId: data.razorpayOrderId } });
      });

      rzp.open();
    } catch (err) {
      setError(err.message || "Something went wrong while initiating payment.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center p-6">
      <div className="bg-gray-800 rounded-lg shadow-lg p-8 max-w-md w-full">
        <h2 className="text-2xl font-bold mb-4">Registration Payment</h2>
        <p className="mb-2">Event ID: {eventId ?? "-"}</p>
        <p className="mb-2">Registration ID: {registrationId ?? "-"}</p>
        <p className="mb-2">Tickets: {ticketCount}</p>
        <p className="mb-6">Amount: ₹{amountRupees}</p>

        {error && <p className="text-red-400 mb-4">{error}</p>}

        <div className="flex gap-3">
          <button
            onClick={handlePay}
            disabled={busy || !sdkReady}
            className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white font-bold py-2 px-4 rounded"
          >
            {busy ? "Processing..." : "Proceed to Pay"}
          </button>

          <button
            onClick={() => navigate(-1)}
            type="button"
            className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded"
          >
            Back
          </button>
        </div>
      </div>
    </div>
  );
}

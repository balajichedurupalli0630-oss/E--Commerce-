"use client";

import { loadStripe } from "@stripe/stripe-js";
import { CheckoutProvider } from "@stripe/react-stripe-js";
import { useAuth } from "@clerk/nextjs";
import { useEffect, useState } from "react";
import { ShippingFormInputs } from "@repo/types";
import CheckoutForm from "./CheckoutForm";
import useCartStore from "@/stores/cartStore";
import { useRouter } from "next/navigation";
import { CheckCircle2, CreditCard, ShieldCheck, Loader2 } from "lucide-react";

const stripePromise = loadStripe(
  process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY ||
  "pk_test_51MdCLkDhkeDdZct5FkM9qMlMvAzsJpObS6eUy44jYLuVMhUFjYjzr4VLodA0GiUj0WBaOSzm38QJ8ju3SAYhdNkF00myyAyh6M"
);

const StripePaymentForm = ({
  shippingForm,
}: {
  shippingForm: ShippingFormInputs;
}) => {
  const { cart, clearCart } = useCartStore();
  const [token, setToken] = useState<string | null>(null);
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submittingMock, setSubmittingMock] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const { getToken } = useAuth();
  const router = useRouter();

  useEffect(() => {
    let isMounted = true;
    const initPayment = async () => {
      try {
        const userToken = await getToken();
        if (isMounted) setToken(userToken);

        const res = await fetch(
          `${process.env.NEXT_PUBLIC_PAYMENT_SERVICE_URL}/sessions/create-checkout-session`,
          {
            method: "POST",
            body: JSON.stringify({ cart }),
            headers: {
              "Content-Type": "application/json",
              ...(userToken ? { Authorization: `Bearer ${userToken}` } : {}),
            },
          }
        );

        if (res.ok) {
          const data = await res.json();
          if (
            data.checkoutSessionClientSecret &&
            typeof data.checkoutSessionClientSecret === "string"
          ) {
            if (isMounted) {
              setClientSecret(data.checkoutSessionClientSecret);
              setLoading(false);
              return;
            }
          }
        }
        if (isMounted) {
          setLoading(false);
        }
      } catch (err: any) {
        console.warn("Payment init notice:", err?.message);
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    initPayment();
    return () => {
      isMounted = false;
    };
  }, [cart]);

  const totalAmount = cart.reduce(
    (acc, item) => acc + item.price * item.quantity,
    0
  );

  const handleMockCheckout = async () => {
    setSubmittingMock(true);
    setErrorMsg(null);
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_PAYMENT_SERVICE_URL}/sessions/mock-checkout?email=${encodeURIComponent(
          shippingForm.email
        )}`,
        {
          method: "POST",
          body: JSON.stringify({ cart }),
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        }
      );

      const data = await res.json();
      if (res.ok && data.sessionId) {
        clearCart();
        router.push(`/return?session_id=${data.sessionId}`);
      } else {
        setErrorMsg(data.error || "Failed to process payment");
      }
    } catch (e: any) {
      setErrorMsg(e.message || "Network error while completing checkout");
    } finally {
      setSubmittingMock(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-8 gap-3 text-gray-500">
        <Loader2 className="w-6 h-6 animate-spin text-gray-800" />
        <p className="text-sm">Initializing payment gateway...</p>
      </div>
    );
  }

  // If Stripe clientSecret is available, render Stripe Elements
  if (clientSecret) {
    return (
      <CheckoutProvider
        stripe={stripePromise}
        options={{ fetchClientSecret: async () => clientSecret }}
      >
        <CheckoutForm shippingForm={shippingForm} />
      </CheckoutProvider>
    );
  }

  // Graceful fallback checkout UI when Stripe Secret Key is not configured
  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between pb-3 border-b border-gray-100">
        <div className="flex items-center gap-2">
          <CreditCard className="w-5 h-5 text-gray-800" />
          <h3 className="font-semibold text-gray-900">Payment Confirmation</h3>
        </div>
        <span className="text-xs bg-emerald-50 text-emerald-700 font-medium px-2.5 py-1 rounded-full border border-emerald-200 flex items-center gap-1">
          <ShieldCheck className="w-3.5 h-3.5" />
          Test Mode Active
        </span>
      </div>

      <div className="bg-gray-50 p-4 rounded-lg flex flex-col gap-2 text-sm text-gray-600">
        <p className="font-medium text-gray-900">Delivery Address</p>
        <p>
          {shippingForm.name} ({shippingForm.email})
        </p>
        <p>
          {shippingForm.address}, {shippingForm.city}
        </p>
        <p className="text-xs text-gray-500">Phone: {shippingForm.phone}</p>
      </div>

      <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
        <div>
          <p className="text-xs text-gray-500">Total Payable Amount</p>
          <p className="text-xl font-bold text-gray-900">
            ₹{totalAmount.toFixed(2)}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-gray-500">Items</p>
          <p className="text-sm font-medium text-gray-700">
            {cart.reduce((acc, item) => acc + item.quantity, 0)} item(s)
          </p>
        </div>
      </div>

      {errorMsg && (
        <div className="p-3 bg-red-50 text-red-600 text-sm rounded-lg border border-red-200">
          {errorMsg}
        </div>
      )}

      <button
        onClick={handleMockCheckout}
        disabled={submittingMock || cart.length === 0}
        className="w-full bg-gray-900 hover:bg-black text-white font-medium py-3 rounded-lg transition-all duration-200 flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
      >
        {submittingMock ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin" />
            Processing Order...
          </>
        ) : (
          <>
            <CheckCircle2 className="w-4 h-4" />
            Place Order (₹{totalAmount.toFixed(2)})
          </>
        )}
      </button>

      <p className="text-xs text-center text-gray-400">
        Orders placed in test mode are recorded in MongoDB and appear in your
        orders history and the Admin Dashboard.
      </p>
    </div>
  );
};

export default StripePaymentForm;

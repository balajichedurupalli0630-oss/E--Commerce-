import Link from "next/link";
import { CheckCircle2, ArrowRight, ShoppingBag } from "lucide-react";

const ReturnPage = async ({
  searchParams,
}: {
  searchParams: Promise<{ session_id: string }> | undefined;
}) => {
  const session_id = (await searchParams)?.session_id;

  if (!session_id) {
    return (
      <div className="max-w-md mx-auto my-16 p-8 text-center bg-white shadow-lg rounded-xl border border-gray-100">
        <h1 className="text-xl font-semibold text-gray-800">No session found</h1>
        <p className="text-sm text-gray-500 mt-2">Could not retrieve order details.</p>
        <Link
          href="/"
          className="inline-block mt-6 px-6 py-2.5 bg-gray-900 text-white rounded-lg text-sm font-medium"
        >
          Return to Store
        </Link>
      </div>
    );
  }

  const res = await fetch(
    `${process.env.NEXT_PUBLIC_PAYMENT_SERVICE_URL}/sessions/${session_id}`,
    { cache: "no-store" }
  );
  const data = await res.json();

  const isSuccess = data.paymentStatus === "paid" || data.status === "complete";

  return (
    <div className="max-w-lg mx-auto my-16 p-8 bg-white shadow-lg rounded-2xl border border-gray-100 text-center flex flex-col items-center">
      <div className="w-16 h-16 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4">
        <CheckCircle2 className="w-10 h-10" />
      </div>
      <h1 className="text-2xl font-bold text-gray-900">
        {isSuccess ? "Payment Successful!" : `Payment ${data.status}`}
      </h1>
      <p className="text-sm text-gray-500 mt-2">
        Thank you for your purchase. Your order has been placed and is being processed.
      </p>

      <div className="w-full bg-gray-50 p-4 rounded-xl my-6 text-left text-sm space-y-2">
        <div className="flex justify-between">
          <span className="text-gray-500">Session ID:</span>
          <span className="font-mono text-xs text-gray-800 truncate max-w-[200px]">{session_id}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-500">Payment Status:</span>
          <span className="font-medium capitalize text-emerald-600">{data.paymentStatus || "Paid"}</span>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 w-full">
        <Link
          href="/orders"
          className="flex-1 flex items-center justify-center gap-2 px-5 py-3 bg-gray-900 hover:bg-black text-white text-sm font-medium rounded-lg transition-all"
        >
          View Your Orders
          <ArrowRight className="w-4 h-4" />
        </Link>
        <Link
          href="/"
          className="flex-1 flex items-center justify-center gap-2 px-5 py-3 border border-gray-300 hover:bg-gray-50 text-gray-700 text-sm font-medium rounded-lg transition-all"
        >
          <ShoppingBag className="w-4 h-4" />
          Continue Shopping
        </Link>
      </div>
    </div>
  );
};

export default ReturnPage;

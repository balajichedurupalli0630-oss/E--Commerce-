"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { useContext } from "react";
import { AuthProvider } from "@/context/AuthContext";

function SignInForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();

  const AUTH_SERVICE_URL = process.env.NEXT_PUBLIC_AUTH_SERVICE_URL || "http://localhost:8003";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError("Please fill in both fields.");
      return;
    }
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(`${AUTH_SERVICE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json();
      if (res.ok && data.token) {
        localStorage.setItem("omnicart_jwt", data.token);
        document.cookie = `omnicart_jwt=${data.token}; path=/; max-age=86400; SameSite=Lax`;
        const redirect = searchParams.get("redirect_url") || "/";
        window.location.href = redirect;
      } else {
        setError(data.error || "Invalid username or password.");
      }
    } catch (err: any) {
      setError("Unable to connect to authentication service.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md p-8 bg-white border border-gray-200 rounded-2xl shadow-sm">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Sign in to OmniCart</h2>
        <p className="text-sm text-gray-500 mt-1">Enter your credentials to access your account</p>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-600 text-sm rounded-lg">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Username or Email
          </label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
            placeholder="johndoe"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Password
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
            placeholder="••••••••"
            required
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-2.5 px-4 bg-black text-white font-medium rounded-lg hover:bg-gray-800 disabled:opacity-50 transition-colors"
        >
          {loading ? "Signing in..." : "Sign In"}
        </button>
      </form>

      <div className="mt-6 text-center text-sm text-gray-500">
        Don&apos;t have an account?{" "}
        <Link href="/sign-up" className="text-black font-semibold hover:underline">
          Sign up
        </Link>
      </div>

      <div className="mt-6 pt-4 border-t border-gray-100 text-xs text-gray-400 text-center">
        Demo credentials: <strong>johndoe</strong> / <strong>password123</strong>
      </div>
    </div>
  );
}

export default function Page() {
  return (
    <div className="flex items-center justify-center py-12">
      <SignInForm />
    </div>
  );
}

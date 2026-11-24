"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { UserType } from "@repo/types";

interface AuthContextType {
  user: UserType | null;
  token: string | null;
  isLoaded: boolean;
  isSignedIn: boolean;
  login: (identifier: string, password: string) => Promise<boolean>;
  register: (userData: any) => Promise<boolean>;
  logout: () => void;
  getToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  isLoaded: false,
  isSignedIn: false,
  login: async () => false,
  register: async () => false,
  logout: () => {},
  getToken: async () => null,
});

const AUTH_SERVICE_URL =
  process.env.NEXT_PUBLIC_AUTH_SERVICE_URL || "http://localhost:8003";

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserType | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoaded, setIsLoaded] = useState(false);
  const router = useRouter();

  useEffect(() => {
    // Check localStorage & cookie on mount
    const savedToken = localStorage.getItem("omnicart_token");
    if (savedToken) {
      setToken(savedToken);
      fetch(`${AUTH_SERVICE_URL}/auth/me`, {
        headers: { Authorization: `Bearer ${savedToken}` },
      })
        .then((res) => (res.ok ? res.json() : null))
        .then((userData) => {
          if (userData && userData.id) {
            setUser(userData);
          } else {
            // Token expired or invalid
            localStorage.removeItem("omnicart_token");
            document.cookie = "omnicart_token=; path=/; max-age=0";
            setUser(null);
            setToken(null);
          }
        })
        .catch(() => {})
        .finally(() => setIsLoaded(true));
    } else {
      setIsLoaded(true);
    }
  }, []);

  const login = async (identifier: string, password: string): Promise<boolean> => {
    try {
      const res = await fetch(`${AUTH_SERVICE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });
      if (!res.ok) return false;
      const data = await res.json();
      if (data.token && data.user) {
        setToken(data.token);
        setUser(data.user);
        localStorage.setItem("omnicart_token", data.token);
        document.cookie = `omnicart_token=${data.token}; path=/; max-age=86400; SameSite=Lax`;
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  };

  const register = async (userData: any): Promise<boolean> => {
    try {
      const res = await fetch(`${AUTH_SERVICE_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData),
      });
      if (!res.ok) return false;
      const data = await res.json();
      if (data.token && data.user) {
        setToken(data.token);
        setUser(data.user);
        localStorage.setItem("omnicart_token", data.token);
        document.cookie = `omnicart_token=${data.token}; path=/; max-age=86400; SameSite=Lax`;
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("omnicart_token");
    document.cookie = "omnicart_token=; path=/; max-age=0";
    router.push("/");
  };

  const getToken = async () => token;

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoaded,
        isSignedIn: !!user,
        login,
        register,
        logout,
        getToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// Hooks
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  return {
    isLoaded: ctx.isLoaded,
    isSignedIn: ctx.isSignedIn,
    userId: ctx.user?.id || null,
    sessionId: ctx.token,
    login: ctx.login,
    register: ctx.register,
    getToken: ctx.getToken,
    signOut: ctx.logout,
  };
};

export const useUser = () => {
  const ctx = useContext(AuthContext);
  const fullName = ctx.user
    ? `${ctx.user.firstName || ""} ${ctx.user.lastName || ""}`.trim() || ctx.user.username
    : "";
  const primaryEmail =
    ctx.user?.email ||
    ctx.user?.emailAddresses?.[0]?.emailAddress ||
    ctx.user?.emailAddresses?.[0]?.email_address ||
    "";

  return {
    isLoaded: ctx.isLoaded,
    isSignedIn: ctx.isSignedIn,
    user: ctx.user
      ? {
          ...ctx.user,
          fullName,
          primaryEmailAddress: { emailAddress: primaryEmail },
        }
      : null,
  };
};

export const useClerk = () => {
  const ctx = useContext(AuthContext);
  return {
    signOut: ctx.logout,
    openSignIn: () => {},
  };
};

// UI Components
export const ClerkProvider = AuthProvider;

export const SignedIn = ({ children }: { children: React.ReactNode }) => {
  const { isSignedIn, isLoaded } = useContext(AuthContext);
  if (!isLoaded || !isSignedIn) return null;
  return <>{children}</>;
};

export const SignedOut = ({ children }: { children: React.ReactNode }) => {
  const { isSignedIn, isLoaded } = useContext(AuthContext);
  if (!isLoaded || isSignedIn) return null;
  return <>{children}</>;
};

export const SignInButton = () => {
  const router = useRouter();
  return (
    <button
      onClick={() => router.push("/sign-in")}
      className="text-sm font-medium text-gray-700 hover:text-black cursor-pointer px-3 py-1.5 rounded-md hover:bg-gray-100 transition"
    >
      Sign In
    </button>
  );
};

export const SignUpButton = () => {
  const router = useRouter();
  return (
    <button
      onClick={() => router.push("/sign-up")}
      className="text-sm font-medium bg-black text-white px-3 py-1.5 rounded-md hover:bg-gray-800 transition cursor-pointer"
    >
      Sign Up
    </button>
  );
};

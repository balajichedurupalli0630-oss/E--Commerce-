"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { UserType } from "@repo/types";

interface AuthContextType {
  user: UserType | null;
  token: string | null;
  isLoaded: boolean;
  isSignedIn: boolean;
  login: (identifier: string, password: string) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  getToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  isLoaded: false,
  isSignedIn: false,
  login: async () => ({ success: false }),
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
    const savedToken = localStorage.getItem("omnicart_admin_token") || localStorage.getItem("omnicart_token");
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
            localStorage.removeItem("omnicart_admin_token");
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

  const login = async (
    identifier: string,
    password: string
  ): Promise<{ success: boolean; error?: string }> => {
    try {
      const res = await fetch(`${AUTH_SERVICE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        return { success: false, error: errData.error || "Invalid username or password" };
      }

      const data = await res.json();
      if (data.token && data.user) {
        const role = data.user.publicMetadata?.role;
        if (role !== "admin") {
          return { success: false, error: "Access denied. Administrator privileges required." };
        }

        setToken(data.token);
        setUser(data.user);
        localStorage.setItem("omnicart_admin_token", data.token);
        localStorage.setItem("omnicart_token", data.token);
        document.cookie = `omnicart_token=${data.token}; path=/; max-age=86400; SameSite=Lax`;
        return { success: true };
      }
      return { success: false, error: "Login response invalid" };
    } catch (e: any) {
      return { success: false, error: e.message || "Failed to connect to authentication service" };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("omnicart_admin_token");
    localStorage.removeItem("omnicart_token");
    document.cookie = "omnicart_token=; path=/; max-age=0";
    router.push("/sign-in");
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
        logout,
        getToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const ClerkProvider = AuthProvider;

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  return {
    isLoaded: ctx.isLoaded,
    isSignedIn: ctx.isSignedIn,
    userId: ctx.user?.id || null,
    sessionId: ctx.token,
    login: ctx.login,
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
    signOut: (options?: any) => ctx.logout(),
  };
};

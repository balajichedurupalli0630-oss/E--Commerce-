"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { UserType } from "@repo/types";

interface AuthContextType {
  user: UserType | null;
  token: string | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<{ success: boolean; error?: string }>;
  register: (data: {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    email: string;
  }) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  isLoading: true,
  login: async () => ({ success: false }),
  register: async () => ({ success: false }),
  logout: () => {},
});

const AUTH_SERVICE_URL = process.env.NEXT_PUBLIC_AUTH_SERVICE_URL || "http://localhost:8003";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserType | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem("omnicart_jwt");
    if (savedToken) {
      setToken(savedToken);
      fetch(`${AUTH_SERVICE_URL}/auth/me`, {
        headers: { Authorization: `Bearer ${savedToken}` },
      })
        .then((res) => (res.ok ? res.json() : null))
        .then((data) => {
          if (data) {
            setUser(data);
          } else {
            localStorage.removeItem("omnicart_jwt");
            document.cookie = "omnicart_jwt=; path=/; max-age=0";
            setToken(null);
          }
        })
        .catch(() => {})
        .finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const res = await fetch(`${AUTH_SERVICE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json();
      if (res.ok && data.token) {
        setToken(data.token);
        setUser(data.user);
        localStorage.setItem("omnicart_jwt", data.token);
        document.cookie = `omnicart_jwt=${data.token}; path=/; max-age=86400; SameSite=Lax`;
        return { success: true };
      }
      return { success: false, error: data.error || "Invalid username or password" };
    } catch (err: any) {
      return { success: false, error: err.message || "Failed to connect to authentication service" };
    }
  };

  const register = async (formData: {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    email: string;
  }) => {
    try {
      const res = await fetch(`${AUTH_SERVICE_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: formData.username,
          password: formData.password,
          firstName: formData.firstName,
          lastName: formData.lastName,
          emailAddress: [formData.email],
        }),
      });
      const data = await res.json();
      if (res.ok && data.token) {
        setToken(data.token);
        setUser(data.user);
        localStorage.setItem("omnicart_jwt", data.token);
        document.cookie = `omnicart_jwt=${data.token}; path=/; max-age=86400; SameSite=Lax`;
        return { success: true };
      }
      return { success: false, error: data.error || "Registration failed" };
    } catch (err: any) {
      return { success: false, error: err.message || "Failed to connect to authentication service" };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("omnicart_jwt");
    document.cookie = "omnicart_jwt=; path=/; max-age=0";
    window.location.href = "/sign-in";
  };

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  return {
    userId: context.user?.id || null,
    getToken: async () => context.token,
    isSignedIn: !!context.user,
    isLoaded: !context.isLoading,
    signOut: context.logout,
  };
};

export const useUser = () => {
  const context = useContext(AuthContext);
  return {
    user: context.user,
    isSignedIn: !!context.user,
    isLoaded: !context.isLoading,
  };
};

export const useClerk = () => {
  const context = useContext(AuthContext);
  return {
    signOut: context.logout,
  };
};

export const SignedIn: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isLoading } = useContext(AuthContext);
  if (isLoading || !user) return null;
  return <>{children}</>;
};

export const SignedOut: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isLoading } = useContext(AuthContext);
  if (isLoading || user) return null;
  return <>{children}</>;
};

export const SignInButton: React.FC = () => (
  <a
    href="/sign-in"
    className="text-sm font-medium text-gray-700 hover:text-black hover:underline"
  >
    Sign In
  </a>
);

export const SignUpButton: React.FC = () => (
  <a
    href="/sign-up"
    className="text-sm font-medium bg-black text-white px-3 py-1.5 rounded-md hover:bg-gray-800"
  >
    Sign Up
  </a>
);

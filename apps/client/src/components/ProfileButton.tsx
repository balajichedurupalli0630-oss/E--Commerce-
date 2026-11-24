"use client";

import { useUser, useAuth } from "@/lib/auth";
import { ShoppingBag, LogOut } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState, useRef, useEffect } from "react";

const ProfileButton = () => {
  const { user } = useUser();
  const { signOut } = useAuth();
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  if (!user) return null;

  const initial = user.firstName?.[0]
    ? user.firstName[0].toUpperCase()
    : user.username?.[0] ? user.username[0].toUpperCase() : "U";

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center justify-center w-8 h-8 rounded-full bg-slate-900 text-white font-semibold text-xs shadow hover:bg-slate-800 transition cursor-pointer"
        title={user.fullName || user.username}
      >
        {initial}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-xl border border-gray-100 py-1.5 z-50">
          <div className="px-3 py-2 border-b border-gray-100">
            <p className="text-xs font-semibold text-gray-900 truncate">
              {user.fullName || user.username}
            </p>
            <p className="text-xs text-gray-500 truncate">
              {user.primaryEmailAddress?.emailAddress}
            </p>
          </div>
          <button
            onClick={() => {
              setOpen(false);
              router.push("/orders");
            }}
            className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2 cursor-pointer transition"
          >
            <ShoppingBag className="w-4 h-4 text-gray-500" />
            See Orders
          </button>
          <button
            onClick={() => {
              setOpen(false);
              signOut();
            }}
            className="w-full text-left px-3 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2 cursor-pointer transition border-t border-gray-50"
          >
            <LogOut className="w-4 h-4 text-red-500" />
            Sign Out
          </button>
        </div>
      )}
    </div>
  );
};

export default ProfileButton;

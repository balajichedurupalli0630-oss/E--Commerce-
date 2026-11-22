"use client";

import { useAuth, useUser } from "@/context/AuthContext";
import { LogOut, ShoppingBag, User } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

const ProfileButton = () => {
  const { user } = useUser();
  const { signOut } = useAuth();
  const [open, setOpen] = useState(false);

  return (
    <div className="relative">
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center gap-2 p-1.5 rounded-full hover:bg-gray-100 transition-colors"
      >
        <div className="w-8 h-8 rounded-full bg-black text-white flex items-center justify-center text-xs font-semibold">
          {user?.firstName?.slice(0, 1) || user?.username?.slice(0, 1).toUpperCase() || "U"}
        </div>
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-xl shadow-lg py-1 z-50">
          <div className="px-4 py-2 border-b border-gray-100">
            <p className="text-xs font-medium text-gray-900 truncate">
              {user?.firstName ? `${user.firstName} ${user.lastName || ""}` : user?.username}
            </p>
            <p className="text-xs text-gray-500 truncate">
              {user?.emailAddresses?.[0]?.email_address || user?.username}
            </p>
          </div>

          <Link
            href="/orders"
            onClick={() => setOpen(false)}
            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            <ShoppingBag className="w-4 h-4 text-gray-500" />
            <span>My Orders</span>
          </Link>

          <button
            onClick={() => {
              setOpen(false);
              signOut();
            }}
            className="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 text-left"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out</span>
          </button>
        </div>
      )}
    </div>
  );
};

export default ProfileButton;

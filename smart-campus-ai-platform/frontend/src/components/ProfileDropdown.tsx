import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

export default function ProfileDropdown() {
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();
  const { profile, role, refreshProfile } = useAuth();
  const [imageSrc, setImageSrc] = useState<string | null>(null);
  const storedName = useMemo(() => localStorage.getItem("name") || localStorage.getItem("userName"), []);
  const friendlyRole = useMemo(() => {
    const raw = (profile?.role || role)?.replace("ROLE_", "").toUpperCase();
    if (!raw) return "Guest";
    if (raw === "STAFF" || raw === "FACULTY") return "Faculty";
    if (raw === "ADMIN") return "Admin";
    return "Student";
  }, [profile?.role, role]);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);

  useEffect(() => {
    if (!profile?.profileImageUrl) return;
    let active = true;
    let objectUrl: string | null = null;
    const token = localStorage.getItem("authToken") || localStorage.getItem("token");
    fetch(`${API_BASE_URL}${profile.profileImageUrl}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
      .then((response) => (response.ok ? response.blob() : null))
      .then((blob) => {
        if (!blob || !active) return;
        objectUrl = URL.createObjectURL(blob);
        setImageSrc(objectUrl);
      })
      .catch(() => null);

    return () => {
      active = false;
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [profile?.profileImageUrl]);

  useEffect(() => {
    const handler = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    window.addEventListener("mousedown", handler);
    return () => window.removeEventListener("mousedown", handler);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("userRole");
    localStorage.removeItem("role");
    localStorage.removeItem("token");
    localStorage.removeItem("name");
    localStorage.removeItem("userName");
    localStorage.removeItem("email");
    localStorage.removeItem("userEmail");
    sessionStorage.removeItem("authToken");
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("role");
    sessionStorage.removeItem("userRole");
    setOpen(false);
    navigate("/login");
  };

  const handleProfile = () => {
    setOpen(false);
    navigate("/profile");
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className="flex items-center gap-3 rounded-full border border-slate-200/60 bg-white/70 px-3 py-2 text-sm text-slate-700 dark:border-white/10 dark:bg-white/10 dark:text-white/80"
      >
        {imageSrc ? (
          <img
            src={imageSrc}
            alt="Profile"
            className="h-8 w-8 rounded-full object-cover"
          />
        ) : (
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-900/10 text-sm font-semibold text-slate-900 dark:bg-white/20 dark:text-white">
            {(profile?.name || storedName || "SC")
              .split(" ")
              .map((part) => part[0])
              .join("")
              .slice(0, 2)
              .toUpperCase()}
          </span>
        )}
        <span className="hidden text-left md:block">
          <span className="block text-xs text-slate-500 dark:text-white/60">Signed in as</span>
          <span className="block text-sm font-medium text-slate-900 dark:text-white">
            {profile?.name || storedName || "User"} ({friendlyRole})
          </span>
        </span>
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 8 }}
            transition={{ duration: 0.2 }}
            className="absolute right-0 mt-2 w-48 rounded-2xl border border-slate-200/60 bg-white/90 p-2 text-sm text-slate-700 shadow-xl dark:border-white/10 dark:bg-slate-900/90 dark:text-white"
          >
            <button
              className="w-full rounded-xl px-3 py-2 text-left hover:bg-slate-900/5 dark:hover:bg-white/10"
              onClick={handleProfile}
            >
              Profile
            </button>
            <button
              className="w-full rounded-xl px-3 py-2 text-left hover:bg-slate-900/5 dark:hover:bg-white/10"
              onClick={handleLogout}
            >
              Logout
            </button>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

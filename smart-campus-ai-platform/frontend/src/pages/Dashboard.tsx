import { useEffect, useMemo, useState } from "react";
import { Navigate } from "react-router-dom";
import StudentDashboard from "@/pages/StudentDashboard";
import FacultyDashboard from "@/pages/FacultyDashboard";

// ─── Role meta ────────────────────────────────────────────────────────────────

const ROLE_META: Record<
  string,
  { label: string; description: string; color: string; icon: string }
> = {
  STUDENT: {
    label: "Student",
    description: "Loading your learning dashboard…",
    color: "from-indigo-500 via-violet-500 to-purple-600",
    icon: "🎓",
  },
  FACULTY: {
    label: "Faculty",
    description: "Loading your faculty workspace…",
    color: "from-emerald-500 via-teal-500 to-cyan-600",
    icon: "🏛️",
  },
  STAFF: {
    label: "Staff",
    description: "Loading your staff workspace…",
    color: "from-emerald-500 via-teal-500 to-cyan-600",
    icon: "🏛️",
  },
  ADMIN: {
    label: "Admin",
    description: "Redirecting to admin panel…",
    color: "from-rose-500 via-pink-500 to-fuchsia-600",
    icon: "⚙️",
  },
};

// ─── Splash screen ────────────────────────────────────────────────────────────

function RoleSplash({ role }: { role: string }) {
  const meta = ROLE_META[role] ?? ROLE_META["STUDENT"];

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-white dark:bg-zinc-950"
      style={{ animation: "fadeOut 0.3s ease-in 0.7s forwards" }}
    >
      <style>{`
        @keyframes fadeOut {
          to { opacity: 0; pointer-events: none; }
        }
        @keyframes scaleIn {
          from { transform: scale(0.85); opacity: 0; }
          to   { transform: scale(1);    opacity: 1; }
        }
        @keyframes slideUp {
          from { transform: translateY(12px); opacity: 0; }
          to   { transform: translateY(0);    opacity: 1; }
        }
        @keyframes pulse-ring {
          0%   { transform: scale(1);    opacity: 0.4; }
          100% { transform: scale(1.9);  opacity: 0; }
        }
        @keyframes dashIn {
          from { width: 0; }
          to   { width: 100%; }
        }
      `}</style>

      <div className="flex flex-col items-center gap-5" style={{ animation: "scaleIn 0.4s cubic-bezier(0.34,1.56,0.64,1) both" }}>

        {/* Icon with pulsing ring */}
        <div className="relative flex h-20 w-20 items-center justify-center">
          <div
            className={`absolute inset-0 rounded-full bg-gradient-to-br ${meta.color} opacity-20`}
            style={{ animation: "pulse-ring 1.2s ease-out 0.2s infinite" }}
          />
          <div className={`flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br ${meta.color} shadow-xl`}>
            <span className="text-3xl" role="img" aria-label={meta.label}>
              {meta.icon}
            </span>
          </div>
        </div>

        {/* Label */}
        <div className="flex flex-col items-center gap-1" style={{ animation: "slideUp 0.4s ease-out 0.15s both" }}>
          <span className={`bg-gradient-to-r ${meta.color} bg-clip-text text-2xl font-extrabold tracking-tight text-transparent`}>
            {meta.label} Portal
          </span>
          <span className="text-sm text-zinc-400 dark:text-zinc-500">
            {meta.description}
          </span>
        </div>

        {/* Progress bar */}
        <div
          className="h-0.5 w-48 overflow-hidden rounded-full bg-zinc-100 dark:bg-zinc-800"
          style={{ animation: "slideUp 0.4s ease-out 0.2s both" }}
        >
          <div
            className={`h-full rounded-full bg-gradient-to-r ${meta.color}`}
            style={{ animation: "dashIn 0.6s ease-out 0.25s both" }}
          />
        </div>
      </div>
    </div>
  );
}

// ─── Fade-in wrapper ──────────────────────────────────────────────────────────

function FadeIn({ children, delay = 0 }: { children: React.ReactNode; delay?: number }) {
  return (
    <div
      style={{
        animation: `slideUp 0.5s ease-out ${delay}ms both`,
      }}
    >
      <style>{`
        @keyframes slideUp {
          from { transform: translateY(12px); opacity: 0; }
          to   { transform: translateY(0);    opacity: 1; }
        }
      `}</style>
      {children}
    </div>
  );
}

// ─── Dashboard ────────────────────────────────────────────────────────────────

export default function Dashboard() {
  const [ready, setReady] = useState(false);

  const role = useMemo(() => {
    const raw =
      localStorage.getItem("role") ||
      localStorage.getItem("userRole") ||
      "STUDENT";
    return raw.replace("ROLE_", "").toUpperCase();
  }, []);

  // Brief intentional delay so the splash is visible
  useEffect(() => {
    const t = setTimeout(() => setReady(true), 900);
    return () => clearTimeout(t);
  }, []);

  return (
    <>
      {/* Splash shown until ready */}
      {!ready && <RoleSplash role={role} />}

      {/* Actual content, fades in after splash */}
      {ready && (
        <FadeIn delay={50}>
          {role === "ADMIN" ? (
            <Navigate to="/admin-dashboard" replace />
          ) : role === "FACULTY" || role === "STAFF" ? (
            <FacultyDashboard />
          ) : (
            <StudentDashboard />
          )}
        </FadeIn>
      )}
    </>
  );
}
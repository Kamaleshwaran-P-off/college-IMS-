import { motion } from "framer-motion";
import { Moon, PanelLeftClose, PanelLeftOpen, Sun } from "lucide-react";
import NotificationPanel from "@/components/NotificationPanel";
import ProfileDropdown from "@/components/ProfileDropdown";

type NavbarProps = {
  onToggleMobile: () => void;
  onToggleCollapse: () => void;
  collapsed: boolean;
  darkMode: boolean;
  onToggleDark: () => void;
};

export default function Navbar({
  onToggleMobile,
  onToggleCollapse,
  collapsed,
  darkMode,
  onToggleDark
}: NavbarProps) {
  return (
    <header className="sticky top-0 z-30 flex items-center justify-between gap-4 border-b border-slate-200/60 bg-white/70 px-6 py-4 backdrop-blur-xl dark:border-white/10 dark:bg-white/5">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onToggleMobile}
          className="rounded-xl border border-slate-200/60 bg-white/70 px-3 py-2 text-sm text-slate-700 transition hover:text-slate-900 dark:border-white/10 dark:bg-white/10 dark:text-white/80 dark:hover:text-white md:hidden"
        >
          Menu
        </button>
        <button
          type="button"
          onClick={onToggleCollapse}
          className="hidden rounded-xl border border-slate-200/60 bg-white/70 px-3 py-2 text-sm text-slate-700 transition hover:text-slate-900 dark:border-white/10 dark:bg-white/10 dark:text-white/80 dark:hover:text-white md:inline-flex"
          aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
        </button>
        <div>
          <p className="text-sm uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Smart Campus</p>
          <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Overview Dashboard</h2>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onToggleDark}
          className="flex items-center gap-2 rounded-full border border-slate-200/60 bg-white/70 px-3 py-2 text-xs text-slate-700 transition hover:text-slate-900 dark:border-white/10 dark:bg-white/10 dark:text-white/80 dark:hover:text-white"
        >
          {darkMode ? <Sun size={16} /> : <Moon size={16} />}
          {darkMode ? "Light" : "Dark"}
        </button>

        <NotificationPanel />

        <ProfileDropdown />
      </div>
    </header>
  );
}

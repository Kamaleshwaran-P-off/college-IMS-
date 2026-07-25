import { AnimatePresence, motion } from "framer-motion";
import {
  BookOpen,
  Bot,
  Calendar,
  ClipboardCheck,
  Inbox,
  Images,
  LayoutDashboard,
  LogOut,
  NotebookPen,
  MessageSquareText,
  PanelLeftClose,
  PanelLeftOpen,
  ScrollText,
  Sparkles,
  Briefcase,
  Clapperboard,
  Rocket,
  Shuffle,
  UserCheck,
  UserCog
} from "lucide-react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";

const navItems = [
  { label: "Dashboard", to: "/dashboard", icon: LayoutDashboard, roles: ["STUDENT", "FACULTY", "STAFF", "ADMIN"] },
  { label: "Learning Flow", to: "/learning", icon: Sparkles, roles: ["STUDENT"] },
  { label: "Hackathon Hub", to: "/hackathons", icon: Rocket, roles: ["STUDENT"] },
  { label: "Happenstance Engine", to: "/happenstance", icon: Shuffle, roles: ["STUDENT"] },
  { label: "Learning Studio", to: "/faculty-learning", icon: Sparkles, roles: ["FACULTY", "STAFF"] },
  { label: "Career Feed", to: "/career-feed", icon: Briefcase, roles: ["STUDENT"] },
  { label: "Resources", to: "/resources", icon: BookOpen, roles: ["STUDENT"] },
  { label: "Inbox", to: "/inbox", icon: Inbox, roles: ["STUDENT"] },
  { label: "Email Intelligence", to: "/email-dashboard", icon: Inbox, roles: ["STUDENT"] },
  { label: "AI Chat", to: "/chat", icon: Bot, roles: ["STUDENT"] },
  
  { label: "Assignment Planner", to: "/assignment-planner", icon: Calendar, roles: ["STUDENT"] },
  { label: "Habit Tracker", to: "/habit-tracker", icon: Calendar, roles: ["STUDENT"] },
 
  { label: "Quiz", to: "/quiz", icon: BookOpen, roles: ["STUDENT"] },
  { label: "Doubt Forum", to: "/doubts", icon: MessageSquareText, roles: ["STUDENT", "FACULTY", "STAFF"] },
  { label: "Study Planner", to: "/planner", icon: BookOpen, roles: ["STUDENT"] },
  { label: "Peer Mentor Matching", to: "/mentor-matching", icon: UserCheck, roles: ["STUDENT", "FACULTY", "STAFF"] },
  { label: "Leave & OD Requests", to: "/leave-requests", icon: ClipboardCheck, roles: ["STUDENT", "FACULTY", "STAFF"] },
  { label: "Faculty Classes", to: "/admin-assign-classes", icon: UserCheck, roles: ["ADMIN"] },
  { label: "Carousel", to: "/admin-carousel", icon: Images, roles: ["ADMIN"] },
];

type SidebarProps = {
  collapsed: boolean;
  onToggleCollapse: () => void;
  mobileOpen: boolean;
  onCloseMobile: () => void;
};

export default function Sidebar({
  collapsed,
  onToggleCollapse,
  mobileOpen,
  onCloseMobile
}: SidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { role } = useAuth();
  const [isDesktop, setIsDesktop] = useState(
    typeof window !== "undefined" ? window.innerWidth >= 768 : true
  );
  const storedRole = (role || "STUDENT").toUpperCase();

  useEffect(() => {
    const handleResize = () => setIsDesktop(window.innerWidth >= 768);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("userRole");
    navigate("/login");
  };

  const content = (
    <div className="flex h-full min-h-0 flex-col gap-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-900/10 text-slate-900 shadow-lg dark:bg-white/15 dark:text-white">
            <UserCog size={20} />
          </div>
          {!collapsed && (
            <div>
              <p className="text-sm font-semibold text-slate-900 dark:text-white">Smart Campus</p>
              <p className="text-xs text-slate-500 dark:text-white/60">AI Platform</p>
            </div>
          )}
        </div>
        <button
          type="button"
          onClick={onToggleCollapse}
          className="hidden rounded-full border border-slate-200/60 bg-white/60 p-2 text-slate-600 transition hover:text-slate-900 dark:border-white/10 dark:bg-white/10 dark:text-white/70 dark:hover:text-white md:inline-flex"
        >
          {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
        </button>
      </div>

      <nav className="flex flex-1 min-h-0 flex-col gap-1 overflow-y-auto pr-1">
        {navItems
          .filter((item) => item.roles.includes(storedRole))
          .map((item) => {
            const Icon = item.icon;
            const isActive =
              item.to === "/"
                ? location.pathname === "/"
                : location.pathname.startsWith(item.to);
            return (
              <NavLink
                key={item.label}
                to={item.to}
                onClick={() => {
                  if (item.to === "/chat") {
                    sessionStorage.setItem("aiChatTransition", "1");
                  }
                  onCloseMobile();
                }}
                className={`flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition ${
                  isActive
                    ? "bg-slate-900/10 text-slate-900 shadow-[0_12px_30px_-18px_rgba(15,23,42,0.35)] dark:bg-white/20 dark:text-white dark:shadow-[0_12px_30px_-18px_rgba(255,255,255,0.6)]"
                    : "text-slate-600 hover:bg-slate-900/5 hover:text-slate-900 dark:text-white/70 dark:hover:bg-white/10 dark:hover:text-white"
                }`}
              >
                <Icon size={18} />
                {!collapsed && <span>{item.label}</span>}
              </NavLink>
            );
          })}
      </nav>

      <button
        type="button"
        onClick={handleLogout}
        className="flex items-center gap-3 rounded-xl px-3 py-2 text-sm text-slate-600 transition hover:bg-slate-900/5 hover:text-slate-900 dark:text-white/70 dark:hover:bg-white/10 dark:hover:text-white"
      >
        <LogOut size={18} />
        {!collapsed && <span>Logout</span>}
      </button>
    </div>
  );

  return (
    <>
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.7 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-40 bg-slate-900 md:hidden"
            onClick={onCloseMobile}
          />
        )}
      </AnimatePresence>

      <motion.aside
        initial={false}
        animate={{
          width: collapsed && isDesktop ? 80 : 260,
          x: isDesktop ? 0 : mobileOpen ? 0 : -280
        }}
        transition={{ type: "spring", stiffness: 260, damping: 30 }}
        className="fixed left-0 top-0 z-50 h-full overflow-hidden border-r border-slate-200/60 bg-white/70 px-4 py-6 backdrop-blur-2xl dark:border-white/10 dark:bg-white/5 md:translate-x-0"
      >
        {content}
      </motion.aside>
    </>
  );
}

import { Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import Sidebar from "@/components/Sidebar";
import Navbar from "@/components/Navbar";
import ProfileCompletionModal from "@/components/ProfileCompletionModal";

export default function App() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [darkMode, setDarkMode] = useState(() =>
    document.documentElement.classList.contains("dark")
  );

  useEffect(() => {
    document.documentElement.classList.toggle("dark", darkMode);
  }, [darkMode]);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const token = localStorage.getItem("token");
    const authToken = localStorage.getItem("authToken");
    if (token && !authToken) {
      localStorage.setItem("authToken", token);
    }
  }, []);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 768) {
        setMobileOpen(false);
      }
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="dashboard-bg min-h-screen">
        <Sidebar
          collapsed={collapsed}
          onToggleCollapse={() => setCollapsed((prev) => !prev)}
          mobileOpen={mobileOpen}
          onCloseMobile={() => setMobileOpen(false)}
        />
        <div
          className={`min-h-screen transition-all ${
            collapsed ? "md:pl-20" : "md:pl-64"
          }`}
        >
          <Navbar
            onToggleMobile={() => setMobileOpen((prev) => !prev)}
            onToggleCollapse={() => setCollapsed((prev) => !prev)}
            collapsed={collapsed}
            darkMode={darkMode}
            onToggleDark={() => setDarkMode((prev) => !prev)}
          />
          <ProfileCompletionModal />
          <main className="px-6 py-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}

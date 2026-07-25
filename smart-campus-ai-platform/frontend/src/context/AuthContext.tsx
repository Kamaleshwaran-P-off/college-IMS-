import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getAuthToken, getJson } from "@/lib/api";

type UserProfile = {
  name: string;
  email: string;
  role: string;
  profileImageUrl?: string | null;
};

type AuthContextValue = {
  role: string | null;
  loading: boolean;
  profile: UserProfile | null;
  refreshProfile: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const normalizeRole = (raw?: string | null) =>
  raw ? raw.replace("ROLE_", "").toUpperCase() : null;

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [role, setRole] = useState<string | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshProfile = useCallback(async () => {
    const token = getAuthToken();
    if (!token) {
      setRole(null);
      setProfile(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const data = await getJson<UserProfile>("/api/user/profile");
      const normalized = normalizeRole(data.role);
      setProfile(data);
      setRole(normalized);
      if (data.name) {
        localStorage.setItem("name", data.name);
        localStorage.setItem("userName", data.name);
      }
      if (data.role) {
        localStorage.setItem("role", data.role);
        localStorage.setItem("userRole", data.role);
      }
      if (normalized) {
        localStorage.setItem("userRoleNormalized", normalized);
      }
    } catch {
      setRole(null);
      setProfile(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);


  const value = useMemo(
    () => ({ role, loading, profile, refreshProfile }),
    [role, loading, profile, refreshProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}

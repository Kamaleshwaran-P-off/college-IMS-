import { useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Bell, Trash2 } from "lucide-react";
import { API_BASE_URL, getAuthHeaders, getJson, postJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

type NotificationItem = {
  id: number;
  title: string;
  message: string;
  senderRole: string;
  targetRole: string;
  department?: string | null;
  className?: string | null;
  createdAt: string;
  createdById?: number | null;
  createdByName?: string | null;
  read: boolean;
};

type SystemNotificationItem = {
  id: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
};

const DEPARTMENTS = ["CSE", "AI&DS", "ECE", "EEE", "MECH", "CIVIL"];

export default function NotificationPanel() {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [systemItems, setSystemItems] = useState<SystemNotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingItems, setLoadingItems] = useState(false);
  const [loadingSystemItems, setLoadingSystemItems] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [systemError, setSystemError] = useState<string | null>(null);

  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");
  const [targetRole, setTargetRole] = useState("ALL");
  const [department, setDepartment] = useState("");
  const [className, setClassName] = useState("");
  const [selectedClasses, setSelectedClasses] = useState<string[]>([]);

  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();
  const unreadCacheKey = useMemo(
    () => `notificationsHasUnread_${userId || "anon"}`,
    [userId]
  );
  const [hasUnread, setHasUnread] = useState(() =>
    typeof window !== "undefined" ? localStorage.getItem(unreadCacheKey) === "true" : false
  );
  const [hasLoaded, setHasLoaded] = useState(false);

  const isFaculty = normalizedRole === "STAFF" || normalizedRole === "FACULTY";
  const isAdmin = normalizedRole === "ADMIN";

  const systemUnread = useMemo(() => systemItems.filter((item) => !item.read).length, [systemItems]);
  const broadcastUnread = useMemo(() => items.filter((item) => !item.read).length, [items]);
  const unreadCount = broadcastUnread + systemUnread;

  const loadNotifications = async (): Promise<NotificationItem[]> => {
    try {
      setLoadingItems(true);
      const data = await getJson<NotificationItem[]>("/api/notifications");
      setItems(data);
      return data;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load notifications");
      return [];
    } finally {
      setLoadingItems(false);
    }
  };

  const loadSystemNotifications = async (): Promise<SystemNotificationItem[]> => {
    try {
      setLoadingSystemItems(true);
      setSystemError(null);
      const data = await getJson<SystemNotificationItem[]>("/api/notifications/user");
      setSystemItems(data);
      return data;
    } catch (err) {
      setSystemError(err instanceof Error ? err.message : "Failed to load system notifications");
      return [];
    } finally {
      setLoadingSystemItems(false);
    }
  };

  useEffect(() => {
    if (!open) return;
    let active = true;
    const loadAll = async () => {
      const [broadcastData, systemData] = await Promise.all([
        loadNotifications(),
        loadSystemNotifications()
      ]);
      if (!active) return;
      setHasLoaded(true);
        const unreadSystem = systemData.filter((item) => !item.read);
      if (unreadSystem.length > 0) {
        unreadSystem.forEach((item) => {
          fetch(`${API_BASE_URL}/api/notifications/user/mark-read`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              ...getAuthHeaders()
            },
            body: JSON.stringify({ id: item.id })
          }).catch(() => null);
        });
        setSystemItems((prev) => prev.map((item) => ({ ...item, read: true })));
      }

      const unreadBroadcastIds = broadcastData.filter((item) => !item.read).map((item) => item.id);
      if (unreadBroadcastIds.length > 0) {
        fetch(`${API_BASE_URL}/api/notifications/mark-read`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...getAuthHeaders()
          },
          body: JSON.stringify({ ids: unreadBroadcastIds })
        }).catch(() => null);
        setItems((prev) =>
          prev.map((item) =>
            unreadBroadcastIds.includes(item.id) ? { ...item, read: true } : item
          )
        );
      }
    };

    loadAll();
    return () => {
      active = false;
    };
  }, [open]);

  useEffect(() => {
    if (!hasLoaded) return;
    const hasUnreadNow = unreadCount > 0;
    setHasUnread(hasUnreadNow);
    if (typeof window !== "undefined") {
      localStorage.setItem(unreadCacheKey, hasUnreadNow ? "true" : "false");
    }
  }, [unreadCount, unreadCacheKey, hasLoaded]);

  const handleDelete = async (id: number) => {
    try {
      await fetch(`${API_BASE_URL}/api/notifications/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders()
      });
      setItems((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  };

  const sendNotification = async () => {
    setLoading(true);
    setError(null);
    try {
      if (!title.trim() || !message.trim()) {
        throw new Error("Title and message are required.");
      }

      if (isFaculty) {
        if (selectedClasses.length === 0) {
          await postJson("/api/notifications", {
            title,
            message,
            targetRole: "STUDENT",
            department: department || null,
            className: null
          });
        } else {
          for (const classItem of selectedClasses) {
            await postJson("/api/notifications", {
              title,
              message,
              targetRole: "STUDENT",
              department: department || null,
              className: classItem
            });
          }
        }
      } else {
        await postJson("/api/notifications", {
          title,
          message,
          targetRole,
          department: department || null,
          className: className || null
        });
      }

      setTitle("");
      setMessage("");
      setDepartment("");
      setClassName("");
      setSelectedClasses([]);
      await loadNotifications(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to send");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative">
      <motion.button
        type="button"
        whileHover={{ rotate: [0, -8, 8, -8, 0], scale: 1.05 }}
        className="relative rounded-full border border-slate-200/60 bg-white/70 p-2 text-slate-700 dark:border-white/10 dark:bg-white/10 dark:text-white/80"
        onClick={() => setOpen((prev) => !prev)}
      >
        <Bell size={18} />
        {hasUnread && (
          <span className="absolute -right-0.5 -top-0.5 h-2.5 w-2.5 rounded-full bg-rose-500" />
        )}
      </motion.button>

      {open && (
        <div className="absolute right-0 mt-3 w-[360px] rounded-2xl border border-slate-200/60 bg-white/90 p-4 shadow-2xl backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/90">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-slate-900 dark:text-white">Notifications</p>
              <p className="text-xs text-slate-500 dark:text-white/60">
                {items.length + systemItems.length} total - {unreadCount} unread
              </p>
            </div>
            <button
              onClick={() => {
                loadNotifications();
                loadSystemNotifications();
              }}
              className="rounded-full border border-slate-200/60 bg-white/70 px-3 py-1 text-xs text-slate-600 dark:border-white/10 dark:bg-white/10 dark:text-white/70"
            >
              Refresh
            </button>
          </div>

          {error && (
            <div className="mt-3 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-600">
              {error}
            </div>
          )}
          {systemError && (
            <div className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
              {systemError}
            </div>
          )}

          <div className="mt-4 max-h-64 space-y-3 overflow-y-auto">
            {(loadingItems || loadingSystemItems) && (
              <div className="space-y-2">
                {Array.from({ length: 4 }).map((_, index) => (
                  <div
                    key={`skeleton-${index}`}
                    className="h-16 rounded-xl border border-slate-200/60 bg-slate-100/80 animate-pulse dark:border-white/10 dark:bg-white/5"
                  />
                ))}
              </div>
            )}
            {!loadingItems && !loadingSystemItems && systemItems.length === 0 && items.length === 0 && (
              <p className="text-xs text-slate-500 dark:text-white/60">No notifications yet.</p>
            )}
            {systemItems.map((item) => {
              const badgeColor =
                item.type === "SUCCESS"
                  ? "bg-emerald-100 text-emerald-700"
                  : item.type === "ERROR"
                  ? "bg-rose-100 text-rose-700"
                  : "bg-blue-100 text-blue-700";
              return (
                <div
                  key={`system-${item.id}`}
                  className={`rounded-xl border px-3 py-2 text-sm ${
                    item.read
                      ? "border-slate-200/60 bg-white/80 dark:border-white/10 dark:bg-white/5"
                      : "border-emerald-300 bg-emerald-50/70 dark:border-emerald-500/60 dark:bg-emerald-500/10"
                  }`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-semibold text-slate-900 dark:text-white">{item.title}</p>
                    <span className={`rounded-full px-2 py-0.5 text-[10px] uppercase ${badgeColor}`}>
                      {item.type}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 dark:text-white/70">{item.message}</p>
                  <div className="mt-2 text-[10px] uppercase text-slate-400 dark:text-white/40">
                    {new Date(item.createdAt).toLocaleString()}
                  </div>
                </div>
              );
            })}
            {items.map((item) => {
              return (
                <div
                  key={item.id}
                  className={`rounded-xl border px-3 py-2 text-sm ${
                    !item.read
                      ? "border-indigo-300 bg-indigo-50/70 dark:border-indigo-500/60 dark:bg-indigo-500/10"
                      : "border-slate-200/60 bg-white/80 dark:border-white/10 dark:bg-white/5"
                  }`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-semibold text-slate-900 dark:text-white">{item.title}</p>
                    {item.createdById && item.createdById === userId && (
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="text-slate-500 hover:text-rose-500"
                      >
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                  <p className="text-xs text-slate-600 dark:text-white/70">{item.message}</p>
                  <div className="mt-2 flex flex-wrap items-center gap-2 text-[10px] uppercase text-slate-400 dark:text-white/40">
                    <span>{new Date(item.createdAt).toLocaleString()}</span>
                    {item.department && <span>Dept: {item.department}</span>}
                    {item.className && <span>Class: {item.className}</span>}
                  </div>
                </div>
              );
            })}
          </div>

          {(isAdmin || isFaculty) && (
            <div className="mt-4 border-t border-slate-200/60 pt-4 dark:border-white/10">
              <p className="text-xs font-semibold text-slate-700 dark:text-white/80">
                Send Notification
              </p>
              <div className="mt-3 space-y-2">
                <input
                  className="h-9 w-full rounded-lg border border-slate-200/60 bg-white/70 px-3 text-sm text-slate-700 dark:border-white/10 dark:bg-white/5 dark:text-white/80"
                  placeholder="Title"
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                />
                <textarea
                  className="h-20 w-full rounded-lg border border-slate-200/60 bg-white/70 px-3 py-2 text-sm text-slate-700 dark:border-white/10 dark:bg-white/5 dark:text-white/80"
                  placeholder="Message"
                  value={message}
                  onChange={(event) => setMessage(event.target.value)}
                />

                {isAdmin && (
                  <select
                    className="h-9 w-full rounded-lg border border-slate-200/60 bg-white/70 px-3 text-sm text-slate-700 dark:border-white/10 dark:bg-white/5 dark:text-white/80"
                    value={targetRole}
                    onChange={(event) => setTargetRole(event.target.value)}
                  >
                    <option value="ALL">All</option>
                    <option value="STUDENT">Students</option>
                    <option value="FACULTY">Faculty</option>
                  </select>
                )}

                <select
                  className="h-9 w-full rounded-lg border border-slate-200/60 bg-white/70 px-3 text-sm text-slate-700 dark:border-white/10 dark:bg-white/5 dark:text-white/80"
                  value={department}
                  onChange={(event) => setDepartment(event.target.value)}
                >
                  <option value="">All Departments</option>
                  {DEPARTMENTS.map((dept) => (
                    <option key={dept} value={dept}>
                      {dept}
                    </option>
                  ))}
                </select>

                {isFaculty && (
                  <div className="rounded-lg border border-slate-200/60 bg-white/70 p-2 text-xs text-slate-600 dark:border-white/10 dark:bg-white/5 dark:text-white/70">
                    <p className="mb-2 font-semibold">Assigned Classes</p>
                    {["CSE-A", "AI&DS-C", "CSE-B"].map((cls) => (
                      <label key={cls} className="flex items-center gap-2">
                        <input
                          type="checkbox"
                          checked={selectedClasses.includes(cls)}
                          onChange={(event) => {
                            if (event.target.checked) {
                              setSelectedClasses((prev) => [...prev, cls]);
                            } else {
                              setSelectedClasses((prev) => prev.filter((c) => c !== cls));
                            }
                          }}
                        />
                        {cls}
                      </label>
                    ))}
                    <p className="mt-2 text-[10px] text-slate-400 dark:text-white/40">
                      Leave unselected to send to all assigned classes.
                    </p>
                  </div>
                )}

                {isAdmin && (
                  <input
                    className="h-9 w-full rounded-lg border border-slate-200/60 bg-white/70 px-3 text-sm text-slate-700 dark:border-white/10 dark:bg-white/5 dark:text-white/80"
                    placeholder="Class (optional, e.g. CSE-A)"
                    value={className}
                    onChange={(event) => setClassName(event.target.value)}
                  />
                )}

                <button
                  onClick={sendNotification}
                  disabled={loading}
                  className="w-full rounded-lg bg-slate-900 py-2 text-sm text-white transition hover:bg-slate-800 disabled:opacity-60 dark:bg-white dark:text-slate-900"
                >
                  {loading ? "Sending..." : "Send Notification"}
                </button>
              </div>
            </div>
          )}
        </div>
      )}

    </div>
  );
}

import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { API_BASE_URL, getAuthHeaders, getJson, patchJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

type StudentProfile = {
  id: number;
  userId: number;
  fullName: string;
  studentCode: string;
};

type StaffProfile = {
  id: number;
  userId: number;
  fullName: string;
  staffCode: string;
};

type LeaveResponse = {
  id: number;
  studentId: number;
  studentName: string;
  studentCode: string;
  mentorId?: number | null;
  mentorName?: string | null;
  type: "LEAVE" | "OD" | "RECAT";
  startDate: string;
  endDate?: string | null;
  reason?: string | null;
  status: "PENDING" | "APPROVED" | "REJECTED" | "FACULTY_APPROVED" | "ADMIN_APPROVED" | "ADMIN_REJECTED";
  createdAt: string;
  decisionNote?: string | null;
  adminRemarks?: string | null;
};

const typeConfig: Record<LeaveResponse["type"], { label: string; color: string; darkColor: string; icon: string }> = {
  LEAVE: {
    label: "Leave",
    color: "bg-violet-100 text-violet-700 border border-violet-200",
    darkColor: "dark:bg-violet-900/40 dark:text-violet-300 dark:border-violet-700/50",
    icon: "🌿",
  },
  OD: {
    label: "OD",
    color: "bg-sky-100 text-sky-700 border border-sky-200",
    darkColor: "dark:bg-sky-900/40 dark:text-sky-300 dark:border-sky-700/50",
    icon: "📋",
  },
  RECAT: {
    label: "ReCAT",
    color: "bg-amber-100 text-amber-700 border border-amber-200",
    darkColor: "dark:bg-amber-900/40 dark:text-amber-300 dark:border-amber-700/50",
    icon: "🔄",
  },
};

export default function LeaveRequests() {
  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();

  const [studentId, setStudentId] = useState<number | null>(null);
  const [staffId, setStaffId] = useState<number | null>(null);
  const [leaveRequests, setLeaveRequests] = useState<LeaveResponse[]>([]);
  const [pendingApprovals, setPendingApprovals] = useState<LeaveResponse[]>([]);

  const [leaveType, setLeaveType] = useState<"LEAVE" | "OD" | "RECAT">("LEAVE");
  const [leaveStart, setLeaveStart] = useState("");
  const [leaveEnd, setLeaveEnd] = useState("");
  const [leaveReason, setLeaveReason] = useState("");
  const [proofFile, setProofFile] = useState<File | null>(null);
  const [letterFile, setLetterFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadProfiles = async () => {
      if (!userId) return;
      try {
        if (normalizedRole === "STUDENT") {
          const student = await getJson<StudentProfile>(`/api/students/by-user?userId=${userId}`);
          setStudentId(student.id);
        }
        if (normalizedRole === "FACULTY" || normalizedRole === "STAFF") {
          const staff = await getJson<StaffProfile>(`/api/staff/by-user?userId=${userId}`);
          setStaffId(staff.id);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load profile");
      }
    };
    loadProfiles();
  }, [normalizedRole, userId]);

  const loadLeaveRequests = async (targetStudentId: number) => {
    try {
      const data = await getJson<LeaveResponse[]>(`/api/leaves?studentId=${targetStudentId}`);
      setLeaveRequests(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load leave requests");
    }
  };

  const loadPendingApprovals = async (mentorKey?: number | null) => {
    try {
      let url = "/api/leaves?status=PENDING";
      if ((normalizedRole === "FACULTY" || normalizedRole === "STAFF") && mentorKey) {
        url = `/api/leaves?mentorId=${mentorKey}&status=PENDING`;
      }
      const data = await getJson<LeaveResponse[]>(url);
      setPendingApprovals(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load approvals");
    }
  };

  useEffect(() => {
    if (normalizedRole === "STUDENT" && studentId) loadLeaveRequests(studentId);
  }, [normalizedRole, studentId]);

  useEffect(() => {
    if ((normalizedRole === "FACULTY" || normalizedRole === "STAFF") && staffId) loadPendingApprovals(staffId);
  }, [normalizedRole, staffId]);

  useEffect(() => {
    if (leaveType === "LEAVE") setLetterFile(null);
  }, [leaveType]);

  const handleLeaveSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!userId) { setError("Please log in first."); return; }
    if (!leaveStart) { setError("Start date is required."); return; }
    if ((leaveType === "RECAT" || leaveType === "OD") && (!proofFile || !letterFile)) {
      setError("Proof and application letter are required for ReCAT/OD.");
      return;
    }
    setError(null);
    setMessage(null);
    setIsSubmitting(true);
    try {
      const formData = new FormData();
      formData.append("userId", String(userId));
      formData.append("type", leaveType);
      formData.append("startDate", leaveStart);
      if (leaveEnd) formData.append("endDate", leaveEnd);
      if (leaveReason) formData.append("reason", leaveReason);
      if (proofFile) formData.append("proofFile", proofFile);
      if (letterFile) formData.append("applicationLetter", letterFile);

      const response = await fetch(`${API_BASE_URL}/api/leaves`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: formData,
      });

      if (!response.ok) {
        const payload = await response.json().catch(() => ({}));
        throw new Error(payload.message || "Leave request failed");
      }

      setMessage("Leave request submitted successfully.");
      setLeaveStart("");
      setLeaveEnd("");
      setLeaveReason("");
      setProofFile(null);
      setLetterFile(null);
      if (studentId) await loadLeaveRequests(studentId);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Leave request failed");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDecision = async (id: number, approved: boolean) => {
    if (!userId) { setError("Approver login required."); return; }
    setError(null);
    setMessage(null);
    try {
      await patchJson(`/api/leaves/${id}/${approved ? "approve" : "reject"}`, {
        approverUserId: userId,
        note: approved ? "Approved via dashboard" : "Rejected via dashboard",
      });
      setMessage(`Request ${approved ? "approved" : "rejected"}.`);
      await loadPendingApprovals(normalizedRole === "FACULTY" || normalizedRole === "STAFF" ? staffId : null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Decision failed");
    }
  };

  const statusLabel = (leave: LeaveResponse) => {
    if (leave.type === "RECAT") {
      if (leave.status === "PENDING") return "Pending";
      if (leave.status === "FACULTY_APPROVED") return "Faculty Approved";
      if (leave.status === "ADMIN_APPROVED") return "Admin Approved";
      if (leave.status === "ADMIN_REJECTED") return "Admin Rejected";
      if (leave.status === "REJECTED") return "Faculty Rejected";
    }
    if (leave.status === "APPROVED") return "Approved";
    if (leave.status === "REJECTED") return "Rejected";
    return leave.status;
  };

  const statusBadgeClass = (leave: LeaveResponse) => {
    const base = "inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold border";
    if (leave.status === "ADMIN_APPROVED" || leave.status === "APPROVED") {
      return `${base} bg-emerald-100 text-emerald-700 border-emerald-200 dark:bg-emerald-900/40 dark:text-emerald-300 dark:border-emerald-700/50`;
    }
    if (leave.status === "FACULTY_APPROVED") {
      return `${base} bg-sky-100 text-sky-700 border-sky-200 dark:bg-sky-900/40 dark:text-sky-300 dark:border-sky-700/50`;
    }
    if (leave.status === "REJECTED" || leave.status === "ADMIN_REJECTED") {
      return `${base} bg-red-100 text-red-700 border-red-200 dark:bg-red-900/40 dark:text-red-300 dark:border-red-700/50`;
    }
    return `${base} bg-amber-100 text-amber-700 border-amber-200 dark:bg-amber-900/40 dark:text-amber-300 dark:border-amber-700/50`;
  };

  const statusDot = (leave: LeaveResponse) => {
    if (leave.status === "ADMIN_APPROVED" || leave.status === "APPROVED") return "bg-emerald-500";
    if (leave.status === "FACULTY_APPROVED") return "bg-sky-500";
    if (leave.status === "REJECTED" || leave.status === "ADMIN_REJECTED") return "bg-red-500";
    return "bg-amber-400";
  };

  const inputClass =
    "h-10 w-full rounded-xl border border-slate-200 bg-white/70 px-3 text-sm shadow-sm " +
    "transition-all duration-200 focus:border-violet-400 focus:ring-2 focus:ring-violet-400/20 focus:outline-none " +
    "dark:border-slate-700 dark:bg-slate-800/60 dark:text-slate-100 dark:placeholder:text-slate-500 " +
    "dark:focus:border-violet-500 dark:focus:ring-violet-500/20";

  return (
    <div className="space-y-6 p-1">
      {/* ── Hero header ── */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-violet-600 via-violet-500 to-indigo-600 p-6 text-white shadow-xl shadow-violet-500/20 dark:shadow-violet-900/30">
        {/* decorative blobs */}
        <div className="pointer-events-none absolute -right-8 -top-8 h-40 w-40 rounded-full bg-white/10 blur-2xl" />
        <div className="pointer-events-none absolute -bottom-6 left-12 h-28 w-28 rounded-full bg-indigo-400/20 blur-2xl" />
        <div className="relative flex items-start justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-violet-200">
              {normalizedRole === "STUDENT" ? "Student Portal" : "Faculty Portal"}
            </p>
            <h1 className="mt-1 text-2xl font-bold tracking-tight">Leave & OD Requests</h1>
            <p className="mt-1.5 text-sm text-violet-100/80">
              {normalizedRole === "STUDENT"
                ? "Apply for leave, OD, or ReCAT and track approvals."
                : "Review and action pending leave requests from students."}
            </p>
          </div>
          <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl bg-white/15 text-2xl shadow-inner">
            {normalizedRole === "STUDENT" ? "📝" : "✅"}
          </div>
        </div>
      </div>

      {/* ── Alerts ── */}
      {message && (
        <div className="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700 shadow-sm dark:border-emerald-700/40 dark:bg-emerald-900/20 dark:text-emerald-300">
          <span className="text-base">✓</span>
          {message}
        </div>
      )}
      {error && (
        <div className="flex items-center gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 shadow-sm dark:border-red-700/40 dark:bg-red-900/20 dark:text-red-300">
          <span className="text-base">✕</span>
          {error}
        </div>
      )}

      {/* ── STUDENT VIEW ── */}
      {normalizedRole === "STUDENT" && (
        <>
          {/* Submit form */}
          <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-6 shadow-sm backdrop-blur-sm dark:border-slate-700/60 dark:bg-slate-900/70">
            <div className="mb-5 flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-violet-100 text-lg dark:bg-violet-900/50">
                📤
              </div>
              <div>
                <h2 className="text-base font-semibold text-slate-800 dark:text-slate-100">
                  Submit a Request
                </h2>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  Requests go to your assigned mentor or admin.
                </p>
              </div>
            </div>

            {/* Type selector pills */}
            <div className="mb-5 flex gap-2">
              {(["LEAVE", "OD", "RECAT"] as const).map((t) => {
                const cfg = typeConfig[t];
                const active = leaveType === t;
                return (
                  <button
                    key={t}
                    type="button"
                    onClick={() => setLeaveType(t)}
                    className={[
                      "flex items-center gap-1.5 rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200",
                      active
                        ? "bg-violet-600 text-white shadow-md shadow-violet-500/30 dark:bg-violet-500"
                        : "border border-slate-200 bg-white text-slate-600 hover:border-violet-300 hover:text-violet-600 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:border-violet-600 dark:hover:text-violet-400",
                    ].join(" ")}
                  >
                    <span>{cfg.icon}</span>
                    {cfg.label}
                  </button>
                );
              })}
            </div>

            <form className="grid gap-4 md:grid-cols-2" onSubmit={handleLeaveSubmit}>
              <div className="space-y-1.5">
                <Label className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                  Start Date
                </Label>
                <input
                  type="date"
                  value={leaveStart}
                  onChange={(e) => setLeaveStart(e.target.value)}
                  className={inputClass}
                />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                  End Date
                </Label>
                <input
                  type="date"
                  value={leaveEnd}
                  onChange={(e) => setLeaveEnd(e.target.value)}
                  className={inputClass}
                />
              </div>

              <div className="space-y-1.5 md:col-span-2">
                <Label className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                  Reason
                </Label>
                <textarea
                  value={leaveReason}
                  onChange={(e) => setLeaveReason(e.target.value)}
                  placeholder="Briefly explain the reason for your request…"
                  className={[
                    inputClass,
                    "h-24 resize-none py-2.5",
                  ].join(" ")}
                />
              </div>

              <div className="space-y-1.5 md:col-span-2">
                <Label className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                  Upload Proof{" "}
                  <span className="normal-case font-normal text-slate-400 dark:text-slate-500">
                    {leaveType === "LEAVE" ? "(optional)" : "(required)"}
                  </span>
                </Label>
                <label className="flex cursor-pointer items-center gap-3 rounded-xl border border-dashed border-slate-300 bg-slate-50/80 px-4 py-3 text-sm text-slate-500 transition-colors hover:border-violet-400 hover:text-violet-600 dark:border-slate-600 dark:bg-slate-800/50 dark:text-slate-400 dark:hover:border-violet-500 dark:hover:text-violet-400">
                  <span className="text-lg">📎</span>
                  <span>{proofFile ? proofFile.name : "Click to upload proof document"}</span>
                  <input
                    type="file"
                    className="hidden"
                    onChange={(e) => setProofFile(e.target.files?.[0] || null)}
                  />
                </label>
              </div>

              {leaveType !== "LEAVE" && (
                <div className="space-y-1.5 md:col-span-2">
                  <Label className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                    Application Letter <span className="normal-case font-normal text-slate-400 dark:text-slate-500">(required)</span>
                  </Label>
                  <label className="flex cursor-pointer items-center gap-3 rounded-xl border border-dashed border-slate-300 bg-slate-50/80 px-4 py-3 text-sm text-slate-500 transition-colors hover:border-violet-400 hover:text-violet-600 dark:border-slate-600 dark:bg-slate-800/50 dark:text-slate-400 dark:hover:border-violet-500 dark:hover:text-violet-400">
                    <span className="text-lg">📄</span>
                    <span>{letterFile ? letterFile.name : "Click to upload application letter"}</span>
                    <input
                      type="file"
                      className="hidden"
                      onChange={(e) => setLetterFile(e.target.files?.[0] || null)}
                    />
                  </label>
                </div>
              )}

              <div className="md:col-span-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-violet-500/25 transition-all duration-200 hover:bg-violet-700 hover:shadow-lg hover:shadow-violet-500/30 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-violet-500 dark:hover:bg-violet-600"
                >
                  {isSubmitting ? (
                    <>
                      <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                      Submitting…
                    </>
                  ) : (
                    <>
                      <span>📨</span>
                      Submit Request
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>

          {/* Leave history table */}
          <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-6 shadow-sm backdrop-blur-sm dark:border-slate-700/60 dark:bg-slate-900/70">
            <div className="mb-5 flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-100 text-lg dark:bg-indigo-900/50">
                📊
              </div>
              <div>
                <h2 className="text-base font-semibold text-slate-800 dark:text-slate-100">
                  Request History
                </h2>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  All your submitted requests and their current status.
                </p>
              </div>
            </div>

            {leaveRequests.length === 0 ? (
              <div className="flex flex-col items-center justify-center gap-2 py-10 text-center">
                <span className="text-4xl">🌱</span>
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">No requests yet</p>
                <p className="text-xs text-slate-400 dark:text-slate-500">
                  Submit your first leave or OD request above.
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr>
                      {["Type", "Start → End", "Status", "Mentor"].map((h) => (
                        <th
                          key={h}
                          className="pb-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-400 dark:text-slate-500"
                        >
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                    {leaveRequests.map((leave) => {
                      const cfg = typeConfig[leave.type];
                      return (
                        <tr
                          key={leave.id}
                          className="group transition-colors hover:bg-slate-50/80 dark:hover:bg-slate-800/50"
                        >
                          <td className="py-3 pr-4">
                            <span
                              className={`inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1 text-xs font-semibold ${cfg.color} ${cfg.darkColor}`}
                            >
                              {cfg.icon} {cfg.label}
                            </span>
                          </td>
                          <td className="py-3 pr-4">
                            <span className="text-slate-700 dark:text-slate-300">
                              {leave.startDate}
                              {leave.endDate && (
                                <>
                                  <span className="mx-1 text-slate-400">→</span>
                                  {leave.endDate}
                                </>
                              )}
                            </span>
                          </td>
                          <td className="py-3 pr-4">
                            <span className={statusBadgeClass(leave)}>
                              <span className={`h-1.5 w-1.5 rounded-full ${statusDot(leave)}`} />
                              {statusLabel(leave)}
                            </span>
                            {leave.type === "RECAT" && leave.status === "ADMIN_REJECTED" && (
                              <p className="mt-1 text-xs text-red-500 dark:text-red-400">
                                {leave.adminRemarks || "No reason provided"}
                              </p>
                            )}
                          </td>
                          <td className="py-3 text-slate-500 dark:text-slate-400">
                            {leave.mentorName || (
                              <span className="italic text-slate-400 dark:text-slate-500">
                                Awaiting assignment
                              </span>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}

      {/* ── FACULTY / STAFF VIEW ── */}
      {(normalizedRole === "FACULTY" || normalizedRole === "STAFF") && (
        <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-6 shadow-sm backdrop-blur-sm dark:border-slate-700/60 dark:bg-slate-900/70">
          <div className="mb-5 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-100 text-lg dark:bg-amber-900/50">
                ⏳
              </div>
              <div>
                <h2 className="text-base font-semibold text-slate-800 dark:text-slate-100">
                  Approval Queue
                </h2>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  Review and action pending requests from your students.
                </p>
              </div>
            </div>
            {pendingApprovals.length > 0 && (
              <span className="flex h-6 min-w-6 items-center justify-center rounded-full bg-amber-500 px-2 text-xs font-bold text-white">
                {pendingApprovals.length}
              </span>
            )}
          </div>

          {pendingApprovals.length === 0 ? (
            <div className="flex flex-col items-center justify-center gap-2 py-10 text-center">
              <span className="text-4xl">🎉</span>
              <p className="text-sm font-medium text-slate-500 dark:text-slate-400">All clear!</p>
              <p className="text-xs text-slate-400 dark:text-slate-500">
                No pending approvals right now.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {pendingApprovals.map((leave) => {
                const cfg = typeConfig[leave.type];
                return (
                  <div
                    key={leave.id}
                    className="group relative overflow-hidden rounded-xl border border-slate-200 bg-gradient-to-r from-slate-50 to-white p-4 shadow-sm transition-all duration-200 hover:border-violet-200 hover:shadow-md dark:border-slate-700 dark:from-slate-800/60 dark:to-slate-800/40 dark:hover:border-violet-700"
                  >
                    {/* left accent bar */}
                    <div className="absolute inset-y-0 left-0 w-1 rounded-l-xl bg-gradient-to-b from-violet-500 to-indigo-500 opacity-0 transition-opacity group-hover:opacity-100" />

                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div className="min-w-0 flex-1">
                        <div className="mb-1.5 flex flex-wrap items-center gap-2">
                          <p className="font-semibold text-slate-800 dark:text-slate-100">
                            {leave.studentName}
                          </p>
                          <span className="rounded-md bg-slate-100 px-2 py-0.5 font-mono text-xs text-slate-500 dark:bg-slate-700 dark:text-slate-400">
                            {leave.studentCode}
                          </span>
                          <span
                            className={`inline-flex items-center gap-1.5 rounded-lg px-2.5 py-0.5 text-xs font-semibold ${cfg.color} ${cfg.darkColor}`}
                          >
                            {cfg.icon} {cfg.label}
                          </span>
                        </div>
                        <p className="text-sm text-slate-500 dark:text-slate-400">
                          <span className="font-medium">
                            {leave.startDate}
                            {leave.endDate && (
                              <>
                                <span className="mx-1">→</span>
                                {leave.endDate}
                              </>
                            )}
                          </span>
                        </p>
                        {leave.reason && (
                          <p className="mt-2 line-clamp-2 text-sm text-slate-600 dark:text-slate-300">
                            "{leave.reason}"
                          </p>
                        )}
                      </div>

                      <div className="flex shrink-0 gap-2">
                        <button
                          onClick={() => handleDecision(leave.id, false)}
                          className="rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm font-semibold text-red-600 transition-all duration-150 hover:bg-red-100 hover:shadow-sm active:scale-95 dark:border-red-700/50 dark:bg-red-900/20 dark:text-red-400 dark:hover:bg-red-900/40"
                        >
                          ✕ Reject
                        </button>
                        <button
                          onClick={() => handleDecision(leave.id, true)}
                          className="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-sm shadow-emerald-500/20 transition-all duration-150 hover:bg-emerald-700 hover:shadow-md active:scale-95 dark:bg-emerald-500 dark:hover:bg-emerald-600"
                        >
                          ✓ Approve
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

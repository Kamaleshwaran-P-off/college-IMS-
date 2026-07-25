import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import NotificationBell from "@/components/NotificationBell";
import { API_BASE_URL, getAuthHeaders, getJson, patchJson, postJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

type MentorAssignment = {
  id: number;
  studentId: number;
  studentName: string;
  studentCode: string;
  mentorId: number;
  mentorName: string;
  mentorCode: string;
  mentorDepartment: string | null;
  assignedAt: string;
};

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
  status:
    | "PENDING"
    | "APPROVED"
    | "REJECTED"
    | "FACULTY_APPROVED"
    | "ADMIN_APPROVED"
    | "ADMIN_REJECTED";
  createdAt: string;
  decidedByName?: string | null;
  decisionNote?: string | null;
  adminRemarks?: string | null;
};

type MentorAnalytics = {
  mentorId: number;
  mentorName: string;
  mentorCode: string;
  department?: string | null;
  menteeCount: number;
  totalDecisions: number;
  avgApprovalHours?: number | null;
};

export default function MentorLeave() {
  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();

  const [assignments, setAssignments] = useState<MentorAssignment[]>([]);
  const [studentId, setStudentId] = useState<number | null>(null);
  const [staffId, setStaffId] = useState<number | null>(null);
  const [leaveRequests, setLeaveRequests] = useState<LeaveResponse[]>([]);
  const [pendingApprovals, setPendingApprovals] = useState<LeaveResponse[]>([]);
  const [mentorAnalytics, setMentorAnalytics] = useState<MentorAnalytics[]>([]);
  const [selectedDepartment, setSelectedDepartment] = useState<string>("ALL");

  const [assignStudentId, setAssignStudentId] = useState("");
  const [assignMentorId, setAssignMentorId] = useState("");
  const [leaveType, setLeaveType] = useState<"LEAVE" | "OD" | "RECAT">("LEAVE");
  const [leaveStart, setLeaveStart] = useState("");
  const [leaveEnd, setLeaveEnd] = useState("");
  const [leaveReason, setLeaveReason] = useState("");
  const [proofFile, setProofFile] = useState<File | null>(null);
  const [letterFile, setLetterFile] = useState<File | null>(null);

  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [adminRejectId, setAdminRejectId] = useState<number | null>(null);
  const [adminRemarks, setAdminRemarks] = useState("");
  const [adminModalOpen, setAdminModalOpen] = useState(false);

  const departments = useMemo(() => {
    const unique = new Set<string>();
    mentorAnalytics.forEach((mentor) => {
      if (mentor.department) {
        unique.add(mentor.department);
      }
    });
    return Array.from(unique).sort();
  }, [mentorAnalytics]);

  const filteredAnalytics = useMemo(() => {
    if (selectedDepartment === "ALL") {
      return mentorAnalytics;
    }
    return mentorAnalytics.filter(
      (mentor) => mentor.department === selectedDepartment
    );
  }, [mentorAnalytics, selectedDepartment]);

  const analyticsSummary = useMemo(() => {
    const totalMentors = filteredAnalytics.length;
    const totalMentees = filteredAnalytics.reduce(
      (sum, mentor) => sum + mentor.menteeCount,
      0
    );
    let totalDecisionHours = 0;
    let totalDecisions = 0;
    filteredAnalytics.forEach((mentor) => {
      if (mentor.avgApprovalHours !== null && mentor.avgApprovalHours !== undefined) {
        totalDecisionHours += mentor.avgApprovalHours * mentor.totalDecisions;
        totalDecisions += mentor.totalDecisions;
      }
    });
    const avgApprovalHours =
      totalDecisions > 0 ? totalDecisionHours / totalDecisions : null;
    return { totalMentors, totalMentees, avgApprovalHours };
  }, [filteredAnalytics]);

  const chartData = useMemo(() => {
    return filteredAnalytics.map((mentor) => ({
      label: mentor.mentorCode,
      mentees: mentor.menteeCount,
      hours: mentor.avgApprovalHours ?? 0,
    }));
  }, [filteredAnalytics]);

  useEffect(() => {
    const loadProfiles = async () => {
      if (!userId) return;
      try {
        if (normalizedRole === "STUDENT") {
          const student = await getJson<StudentProfile>(`/api/students/by-user?userId=${userId}`);
          setStudentId(student.id);
        }
        if (normalizedRole === "STAFF") {
          const staff = await getJson<StaffProfile>(`/api/staff/by-user?userId=${userId}`);
          setStaffId(staff.id);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load profile");
      }
    };

    loadProfiles();
  }, [role, userId]);

  const loadAssignments = async () => {
    try {
      const data = await getJson<MentorAssignment[]>("/api/mentors");
      setAssignments(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load mentor assignments");
    }
  };

  const loadAnalytics = async () => {
    if (!userId) return;
    try {
      const data = await getJson<MentorAnalytics[]>(
        `/api/mentors/analytics?adminUserId=${userId}`
      );
      setMentorAnalytics(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load mentor analytics");
    }
  };

  const loadLeaveRequests = async (targetStudentId: number) => {
    try {
      const data = await getJson<LeaveResponse[]>(
        `/api/leaves?studentId=${targetStudentId}`
      );
      setLeaveRequests(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load leave requests");
    }
  };

  const loadPendingApprovals = async (mentorKey?: number | null) => {
    try {
      let url = "/api/leaves?status=PENDING";
      if (normalizedRole === "STAFF" && mentorKey) {
        url = `/api/leaves?mentorId=${mentorKey}&status=PENDING`;
      }
      if (normalizedRole === "ADMIN") {
        url = "/api/leaves?status=FACULTY_APPROVED&type=RECAT";
      }
      const data = await getJson<LeaveResponse[]>(url);
      setPendingApprovals(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load approvals");
    }
  };

  useEffect(() => {
    if (normalizedRole === "ADMIN") {
      loadAssignments();
      loadPendingApprovals(null);
      loadAnalytics();
    }
  }, [normalizedRole, userId]);

  useEffect(() => {
    if (normalizedRole === "STUDENT" && studentId) {
      loadLeaveRequests(studentId);
    }
  }, [normalizedRole, studentId]);

  useEffect(() => {
    if (leaveType === "LEAVE") {
      setLetterFile(null);
    }
  }, [leaveType]);

  useEffect(() => {
    if (normalizedRole === "STAFF" && staffId) {
      loadPendingApprovals(staffId);
      getJson<MentorAssignment[]>(`/api/mentors?mentorId=${staffId}`)
        .then((data) => setAssignments(data))
        .catch(() => null);
    }
  }, [normalizedRole, staffId]);

  const handleAssign = async (event: FormEvent) => {
    event.preventDefault();
    if (!assignStudentId || !assignMentorId || !userId) {
      setError("Student ID, Mentor ID, and admin login are required.");
      return;
    }
    setError(null);
    setMessage(null);
    try {
      await postJson("/api/mentors/assign", {
        studentId: Number(assignStudentId),
        mentorId: Number(assignMentorId),
        assignedByUserId: userId
      });
      setMessage("Mentor assigned successfully.");
      setAssignStudentId("");
      setAssignMentorId("");
      await loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to assign mentor");
    }
  };

  const handleAutoAllocate = async () => {
    if (!userId) {
      setError("Admin login required.");
      return;
    }
    setError(null);
    setMessage(null);
    try {
      const created = await postJson<MentorAssignment[]>("/api/mentors/auto-allocate", {
        assignedByUserId: userId
      });
      setMessage(`Auto-allocated ${created.length} mentor assignments.`);
      await loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Auto allocation failed");
    }
  };

  const handleLeaveSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!userId) {
      setError("Please log in first.");
      return;
    }
    if (!leaveStart) {
      setError("Start date is required.");
      return;
    }
    if ((leaveType === "RECAT" || leaveType === "OD") && (!proofFile || !letterFile)) {
      setError("Proof and application letter are required for ReCAT/OD.");
      return;
    }
    setError(null);
    setMessage(null);
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
        body: formData
      });

      if (!response.ok) {
        const payload = await response.json().catch(() => ({}));
        throw new Error(payload.message || "Leave request failed");
      }

      setMessage("Leave request submitted.");
      setLeaveStart("");
      setLeaveEnd("");
      setLeaveReason("");
      setProofFile(null);
      setLetterFile(null);
      if (studentId) {
        await loadLeaveRequests(studentId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Leave request failed");
    }
  };

  const handleDecision = async (id: number, approved: boolean) => {
    if (!userId) {
      setError("Approver login required.");
      return;
    }
    setError(null);
    setMessage(null);
    try {
      const path = `/api/leaves/${id}/${approved ? "approve" : "reject"}`;
      await patchJson(path, {
        approverUserId: userId,
        note: approved ? "Approved via dashboard" : "Rejected via dashboard"
      });
      setMessage(`Request ${approved ? "approved" : "rejected"}.`);
      await loadPendingApprovals(normalizedRole === "STAFF" ? staffId : null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Decision failed");
    }
  };

  const handleAdminDecision = async (id: number, status: "ADMIN_APPROVED" | "ADMIN_REJECTED", remarks?: string) => {
    setError(null);
    setMessage(null);
    try {
      const response = await fetch(`${API_BASE_URL}/api/recat/admin/review`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          ...getAuthHeaders()
        },
        body: JSON.stringify({
          requestId: id,
          status,
          adminRemarks: remarks || null
        })
      });
      if (!response.ok) {
        const payload = await response.json().catch(() => ({}));
        throw new Error(payload.message || "Admin review failed");
      }
      setMessage(`ReCAT application ${status === "ADMIN_APPROVED" ? "approved" : "rejected"}.`);
      await loadPendingApprovals(null);
      if (studentId) {
        await loadLeaveRequests(studentId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Admin decision failed");
    } finally {
      setAdminModalOpen(false);
      setAdminRemarks("");
      setAdminRejectId(null);
    }
  };

  const openAdminRejectModal = (id: number) => {
    setAdminRejectId(id);
    setAdminRemarks("");
    setAdminModalOpen(true);
  };

  const statusLabel = (leave: LeaveResponse) => {
    if (leave.type === "RECAT") {
      if (leave.status === "PENDING") return "Pending";
      if (leave.status === "FACULTY_APPROVED") return "Approved by Faculty";
      if (leave.status === "ADMIN_APPROVED") return "Approved by Admin";
      if (leave.status === "ADMIN_REJECTED") return "Rejected by Admin";
      if (leave.status === "REJECTED") return "Rejected by Faculty";
    }
    if (leave.status === "APPROVED") return "Approved";
    if (leave.status === "REJECTED") return "Rejected";
    return leave.status;
  };

  const statusBadgeClass = (leave: LeaveResponse) => {
    if (leave.status === "ADMIN_APPROVED" || leave.status === "APPROVED") {
      return "bg-emerald-100 text-emerald-700";
    }
    if (leave.status === "FACULTY_APPROVED") {
      return "bg-blue-100 text-blue-700";
    }
    if (leave.status === "REJECTED" || leave.status === "ADMIN_REJECTED") {
      return "bg-red-100 text-red-700";
    }
    return "bg-amber-100 text-amber-700";
  };

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm uppercase tracking-[0.2em] text-muted-foreground">
              Mentor & Leave Desk
            </p>
            <h1 className="text-3xl font-semibold text-foreground">
              Mentor matching and approvals
            </h1>
            <p className="text-muted-foreground">
              Allocate mentors, submit leave/OD/recat requests, and manage approvals.
            </p>
          </div>
          <NotificationBell />
        </header>

        {message && (
          <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm text-emerald-700">
            {message}
          </div>
        )}
        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
            {error}
          </div>
        )}

        {normalizedRole === "ADMIN" && (
          <Card className="bg-card/90 backdrop-blur">
            <CardHeader>
              <CardTitle>Mentor Allocation</CardTitle>
              <CardDescription>Assign mentors manually or auto-balance by load.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex flex-wrap items-center gap-3">
                <Button onClick={handleAutoAllocate}>Auto Allocate</Button>
                <span className="text-sm text-muted-foreground">
                  Matches by department when available, otherwise least load.
                </span>
              </div>
              <form className="grid gap-4 md:grid-cols-3" onSubmit={handleAssign}>
                <div className="space-y-2">
                  <Label htmlFor="studentId">Student ID</Label>
                  <Input
                    id="studentId"
                    value={assignStudentId}
                    onChange={(event) => setAssignStudentId(event.target.value)}
                    placeholder="e.g. 12"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="mentorId">Mentor (Staff) ID</Label>
                  <Input
                    id="mentorId"
                    value={assignMentorId}
                    onChange={(event) => setAssignMentorId(event.target.value)}
                    placeholder="e.g. 5"
                  />
                </div>
                <div className="flex items-end">
                  <Button type="submit" className="w-full">
                    Assign Mentor
                  </Button>
                </div>
              </form>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-xs uppercase tracking-wide text-muted-foreground">
                      <th className="pb-2">Student</th>
                      <th className="pb-2">Mentor</th>
                      <th className="pb-2">Dept</th>
                      <th className="pb-2">Assigned</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assignments.map((assignment) => (
                      <tr key={assignment.id} className="border-t border-border">
                        <td className="py-2">
                          {assignment.studentName} ({assignment.studentCode})
                        </td>
                        <td className="py-2">
                          {assignment.mentorName} ({assignment.mentorCode})
                        </td>
                        <td className="py-2">{assignment.mentorDepartment || "-"}</td>
                        <td className="py-2">
                          {new Date(assignment.assignedAt).toLocaleDateString()}
                        </td>
                      </tr>
                    ))}
                    {assignments.length === 0 && (
                      <tr>
                        <td className="py-4 text-muted-foreground" colSpan={4}>
                          No mentor assignments yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        )}

        {normalizedRole === "ADMIN" && (
          <Card className="bg-card/90 backdrop-blur">
            <CardHeader>
              <CardTitle>Mentor Analytics</CardTitle>
              <CardDescription>Mentee load and approval turnaround time.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <Label htmlFor="departmentFilter">Department</Label>
                  <select
                    id="departmentFilter"
                    className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                    value={selectedDepartment}
                    onChange={(event) => setSelectedDepartment(event.target.value)}
                  >
                    <option value="ALL">All Departments</option>
                    {departments.map((dept) => (
                      <option key={dept} value={dept}>
                        {dept}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="flex flex-wrap gap-3 text-sm text-muted-foreground">
                  <span className="rounded-full border border-border bg-background px-3 py-1">
                    Mentors: {analyticsSummary.totalMentors}
                  </span>
                  <span className="rounded-full border border-border bg-background px-3 py-1">
                    Mentees: {analyticsSummary.totalMentees}
                  </span>
                  <span className="rounded-full border border-border bg-background px-3 py-1">
                    Avg Approval:{" "}
                    {analyticsSummary.avgApprovalHours !== null
                      ? `${analyticsSummary.avgApprovalHours.toFixed(1)} hrs`
                      : "-"}
                  </span>
                </div>
              </div>

              <div className="grid gap-4 lg:grid-cols-2">
                <div className="h-64 rounded-lg border border-border bg-background/70 p-3">
                  <p className="text-xs uppercase tracking-wide text-muted-foreground">
                    Mentee Load
                  </p>
                  <div className="h-52">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={chartData}>
                        <CartesianGrid strokeDasharray="4 4" stroke="#e5e7eb" />
                        <XAxis dataKey="label" />
                        <YAxis />
                        <Tooltip />
                        <Bar dataKey="mentees" fill="#2563eb" radius={[6, 6, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
                <div className="h-64 rounded-lg border border-border bg-background/70 p-3">
                  <p className="text-xs uppercase tracking-wide text-muted-foreground">
                    Approval Speed (hrs)
                  </p>
                  <div className="h-52">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={chartData}>
                        <CartesianGrid strokeDasharray="4 4" stroke="#e5e7eb" />
                        <XAxis dataKey="label" />
                        <YAxis />
                        <Tooltip />
                        <Line
                          type="monotone"
                          dataKey="hours"
                          stroke="#f97316"
                          strokeWidth={2}
                          dot={{ r: 3 }}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-xs uppercase tracking-wide text-muted-foreground">
                      <th className="pb-2">Mentor</th>
                      <th className="pb-2">Department</th>
                      <th className="pb-2">Mentees</th>
                      <th className="pb-2">Decisions</th>
                      <th className="pb-2">Avg Approval (hrs)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredAnalytics.map((mentor) => (
                      <tr key={mentor.mentorId} className="border-t border-border">
                        <td className="py-2">
                          {mentor.mentorName} ({mentor.mentorCode})
                        </td>
                        <td className="py-2">{mentor.department || "-"}</td>
                        <td className="py-2">{mentor.menteeCount}</td>
                        <td className="py-2">{mentor.totalDecisions}</td>
                        <td className="py-2">
                          {mentor.avgApprovalHours !== null &&
                          mentor.avgApprovalHours !== undefined
                            ? mentor.avgApprovalHours.toFixed(1)
                            : "-"}
                        </td>
                      </tr>
                    ))}
                    {filteredAnalytics.length === 0 && (
                      <tr>
                        <td className="py-4 text-muted-foreground" colSpan={5}>
                          No mentor analytics yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        )}

        {normalizedRole === "STUDENT" && (
          <Card className="bg-card/90 backdrop-blur">
            <CardHeader>
              <CardTitle>Apply Leave / OD / Recat</CardTitle>
              <CardDescription>Requests go to your assigned mentor or admin.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <form className="grid gap-4 md:grid-cols-3" onSubmit={handleLeaveSubmit}>
                <div className="space-y-2">
                  <Label>Type</Label>
                  <select
                    className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                    value={leaveType}
                    onChange={(event) => setLeaveType(event.target.value as LeaveResponse["type"])}
                  >
                    <option value="LEAVE">Leave</option>
                    <option value="OD">OD</option>
                    <option value="RECAT">Recat</option>
                  </select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="startDate">Start Date</Label>
                  <Input
                    id="startDate"
                    type="date"
                    value={leaveStart}
                    onChange={(event) => setLeaveStart(event.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="endDate">End Date</Label>
                  <Input
                    id="endDate"
                    type="date"
                    value={leaveEnd}
                    onChange={(event) => setLeaveEnd(event.target.value)}
                  />
                </div>
                <div className="md:col-span-3 space-y-2">
                  <Label htmlFor="reason">Reason</Label>
                  <textarea
                    id="reason"
                    value={leaveReason}
                    onChange={(event) => setLeaveReason(event.target.value)}
                    placeholder="Explain the reason for leave/OD/recat"
                    className="h-24 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  />
                </div>
                <div className="space-y-2 md:col-span-3">
                  <Label htmlFor="proofFile">
                    Upload Proof {leaveType === "LEAVE" ? "(optional)" : "(required)"}
                  </Label>
                  <Input
                    id="proofFile"
                    type="file"
                    onChange={(event) => setProofFile(event.target.files?.[0] || null)}
                  />
                </div>
                {leaveType !== "LEAVE" && (
                  <div className="space-y-2 md:col-span-3">
                    <Label htmlFor="applicationLetter">Upload Application Letter (required)</Label>
                    <Input
                      id="applicationLetter"
                      type="file"
                      onChange={(event) => setLetterFile(event.target.files?.[0] || null)}
                    />
                  </div>
                )}
                <div className="md:col-span-3">
                  <Button type="submit">Submit Request</Button>
                </div>
              </form>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-xs uppercase tracking-wide text-muted-foreground">
                      <th className="pb-2">Type</th>
                      <th className="pb-2">Dates</th>
                      <th className="pb-2">Status</th>
                      <th className="pb-2">Mentor</th>
                    </tr>
                  </thead>
                  <tbody>
                    {leaveRequests.map((leave) => (
                      <tr key={leave.id} className="border-t border-border">
                        <td className="py-2">{leave.type}</td>
                        <td className="py-2">
                          {leave.startDate}
                          {leave.endDate ? ` -> ${leave.endDate}` : ""}
                        </td>
                        <td className="py-2">
                          <span
                            className={`rounded-full px-2 py-0.5 text-xs font-semibold ${statusBadgeClass(
                              leave
                            )}`}
                          >
                            {statusLabel(leave)}
                          </span>
                          {leave.type === "RECAT" && leave.status === "ADMIN_REJECTED" && (
                            <p className="mt-1 text-xs text-red-600">
                              Reason: {leave.adminRemarks || "No reason provided"}
                            </p>
                          )}
                          {leave.type === "RECAT" && leave.status === "REJECTED" && (
                            <p className="mt-1 text-xs text-red-600">
                              Reason: {leave.decisionNote || "No reason provided"}
                            </p>
                          )}
                          {leave.type === "RECAT" && leave.status === "ADMIN_APPROVED" && (
                            <p className="mt-1 text-xs text-emerald-600">
                              Your ReCAT application has been approved.
                            </p>
                          )}
                        </td>
                        <td className="py-2">{leave.mentorName || "Pending mentor"}</td>
                      </tr>
                    ))}
                    {leaveRequests.length === 0 && (
                      <tr>
                        <td className="py-4 text-muted-foreground" colSpan={4}>
                          No leave requests yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        )}

        {(normalizedRole === "STAFF" || normalizedRole === "ADMIN") && (
          <Card className="bg-card/90 backdrop-blur">
            <CardHeader>
              <CardTitle>
                {normalizedRole === "ADMIN" ? "ReCAT Applications" : "Approval Queue"}
              </CardTitle>
              <CardDescription>
                {normalizedRole === "ADMIN"
                  ? "Review faculty-approved ReCAT applications."
                  : "Review pending leave/OD/recat requests."}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {pendingApprovals.map((leave) => (
                <div
                  key={leave.id}
                  className="rounded-lg border border-border bg-background/70 p-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <p className="font-semibold text-foreground">
                        {leave.studentName} ({leave.studentCode})
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {leave.type} - {leave.startDate}
                        {leave.endDate ? ` -> ${leave.endDate}` : ""}
                      </p>
                      {leave.reason && (
                        <p className="mt-2 text-sm text-muted-foreground">{leave.reason}</p>
                      )}
                      {leave.type === "RECAT" && leave.decisionNote && (
                        <p className="mt-2 text-xs text-slate-500">
                          Faculty remarks: {leave.decisionNote}
                        </p>
                      )}
                    </div>
                    <div className="flex gap-2">
                      {normalizedRole === "ADMIN" ? (
                        <>
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => openAdminRejectModal(leave.id)}
                          >
                            Reject
                          </Button>
                          <Button size="sm" onClick={() => handleAdminDecision(leave.id, "ADMIN_APPROVED")}>
                            Approve
                          </Button>
                        </>
                      ) : (
                        <>
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => handleDecision(leave.id, false)}
                          >
                            Reject
                          </Button>
                          <Button size="sm" onClick={() => handleDecision(leave.id, true)}>
                            Approve
                          </Button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              ))}
              {pendingApprovals.length === 0 && (
                <p className="text-sm text-muted-foreground">
                  No pending approvals right now.
                </p>
              )}
            </CardContent>
          </Card>
        )}

        {adminModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
            <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
              <h3 className="text-lg font-semibold text-slate-900">Reject ReCAT Application</h3>
              <p className="mt-1 text-sm text-slate-500">
                Please provide the rejection reason. This will be shared with the student and faculty.
              </p>
              <textarea
                className="mt-4 h-28 w-full rounded-md border border-slate-200 px-3 py-2 text-sm"
                placeholder="Enter rejection reason"
                value={adminRemarks}
                onChange={(event) => setAdminRemarks(event.target.value)}
              />
              <div className="mt-4 flex justify-end gap-2">
                <Button
                  variant="secondary"
                  onClick={() => {
                    setAdminModalOpen(false);
                    setAdminRemarks("");
                    setAdminRejectId(null);
                  }}
                >
                  Cancel
                </Button>
                <Button
                  onClick={() => {
                    if (!adminRejectId) return;
                    if (!adminRemarks.trim()) {
                      setError("Admin rejection reason is required.");
                      return;
                    }
                    handleAdminDecision(adminRejectId, "ADMIN_REJECTED", adminRemarks.trim());
                  }}
                >
                  Reject Application
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

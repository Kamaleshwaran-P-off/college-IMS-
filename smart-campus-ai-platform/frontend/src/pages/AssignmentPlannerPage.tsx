import { useEffect, useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  CalendarClock, PlusCircle, RefreshCcw, CheckCircle2, AlertCircle,
  X, Sparkles, Clock, BookOpen, Users, User, BarChart3,
  Loader2, CalendarDays, Target, Zap, ChevronRight, ClipboardList
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import CalendarView, { PlannerTask } from "@/components/planner/CalendarView";
import TaskChecklist, { ChecklistTask } from "@/components/planner/TaskChecklist";
import { getJson, patchJson, postJson } from "@/lib/api";

/* ─── Types ───────────────────────────────────────────────────────────── */
type PlannerAssignment = {
  id: number;
  title: string;
  description?: string | null;
  deadline?: string | null;
  estimatedHours?: number | null;
  targetType: "STUDENT" | "CLASS";
  targetStudentId?: number | null;
  targetDepartment?: string | null;
  targetSection?: string | null;
  createdBy?: string | null;
};

type ScheduledTask = {
  id: number;
  assignmentId: number;
  assignmentTitle: string;
  date: string;
  taskDetail: string;
  hours: number;
  completed: boolean;
  deadline?: string | null;
};

type CreateAssignmentPayload = {
  title: string;
  description?: string;
  deadline?: string;
  estimatedHours?: number;
  targetType: "STUDENT" | "CLASS";
  targetStudentId?: number;
  targetDepartment?: string;
  targetSection?: string;
};

/* ─── Framer variants ─────────────────────────────────────────────────── */
const cardVariants = {
  hidden: { opacity: 0, y: 24, scale: 0.97 },
  visible: (i: number) => ({
    opacity: 1, y: 0, scale: 1,
    transition: { delay: i * 0.09, duration: 0.45, type: "spring", stiffness: 110 },
  }),
};

const fadeUp = {
  hidden: { opacity: 0, y: 12 },
  visible: (i = 0) => ({
    opacity: 1, y: 0,
    transition: { delay: i * 0.06, duration: 0.35 },
  }),
};

const listItem = {
  hidden: { opacity: 0, x: -12 },
  visible: (i: number) => ({
    opacity: 1, x: 0,
    transition: { delay: i * 0.07, duration: 0.35, type: "spring", stiffness: 130 },
  }),
  exit: { opacity: 0, x: 12, transition: { duration: 0.2 } },
};

const bannerVariants = {
  hidden: { opacity: 0, y: -8, height: 0 },
  visible: { opacity: 1, y: 0, height: "auto", transition: { duration: 0.3 } },
  exit: { opacity: 0, y: -8, height: 0, transition: { duration: 0.22 } },
};

/* ─── Shared input style ──────────────────────────────────────────────── */
const inputCls = `
  w-full rounded-xl border border-slate-200 dark:border-white/10
  bg-white dark:bg-white/[0.06]
  text-slate-800 dark:text-white
  placeholder:text-slate-400 dark:placeholder:text-slate-500
  px-3 py-2 text-sm
  focus:outline-none focus:ring-2 focus:ring-indigo-500/40 dark:focus:ring-indigo-400/30
  transition-all duration-200
`;

/* ─── FieldGroup helper ───────────────────────────────────────────────── */
function FieldGroup({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <Label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
        {label}
      </Label>
      {children}
    </div>
  );
}

/* ─── SectionHeading helper ───────────────────────────────────────────── */
function SectionHeading({ icon: Icon, title, color }: { icon: React.ElementType; title: string; color: string }) {
  return (
    <div className="flex items-center gap-2 mb-4">
      <div className={`w-7 h-7 rounded-lg bg-gradient-to-br ${color} flex items-center justify-center`}>
        <Icon className="w-3.5 h-3.5 text-white" />
      </div>
      <h3 className="text-sm font-semibold text-slate-700 dark:text-slate-200">{title}</h3>
    </div>
  );
}

/* ─── Deadline urgency helper ─────────────────────────────────────────── */
function deadlineUrgency(deadline?: string | null) {
  if (!deadline) return null;
  const diff = Math.ceil((new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24));
  if (diff < 0) return { label: "Overdue", cls: "bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400 border-red-200 dark:border-red-500/20" };
  if (diff <= 2) return { label: `${diff}d left`, cls: "bg-rose-50 dark:bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-200 dark:border-rose-500/20" };
  if (diff <= 7) return { label: `${diff}d left`, cls: "bg-amber-50 dark:bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-200 dark:border-amber-500/20" };
  return { label: `${diff}d left`, cls: "bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-500/20" };
}

/* ─── Main Component ──────────────────────────────────────────────────── */
export default function AssignmentPlannerPage() {
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();
  const isFaculty = normalizedRole === "FACULTY" || normalizedRole === "STAFF";
  const isStudent = normalizedRole === "STUDENT";

  const [assignments, setAssignments] = useState<PlannerAssignment[]>([]);
  const [tasks, setTasks] = useState<ScheduledTask[]>([]);
  const [loadingAssignments, setLoadingAssignments] = useState(true);
  const [loadingTasks, setLoadingTasks] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [form, setForm] = useState<CreateAssignmentPayload>({
    title: "", description: "", deadline: "",
    estimatedHours: 2, targetType: "CLASS",
    targetDepartment: "", targetSection: "",
  });
  const [maxHours, setMaxHours] = useState(3);
  const [creating, setCreating] = useState(false);

  /* ── Loaders ── */
  const loadAssignments = async () => {
    setLoadingAssignments(true);
    setError(null);
    try {
      const data = await getJson<PlannerAssignment[]>(
        isFaculty ? "/api/faculty/assignments" : "/api/student/assignments"
      );
      setAssignments(data || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load assignments.");
    } finally {
      setLoadingAssignments(false);
    }
  };

  const loadSchedule = async () => {
    if (!isStudent) return;
    const userId = localStorage.getItem("userId");
    if (!userId) return;
    setLoadingTasks(true);
    try {
      const data = await getJson<ScheduledTask[]>(`/api/schedule/${userId}`, { allow404: true });
      setTasks(data || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load schedule.");
    } finally {
      setLoadingTasks(false);
    }
  };

  useEffect(() => {
    loadAssignments();
    if (isStudent) loadSchedule();
  }, [isFaculty, isStudent]);

  /* ── Handlers ── */
  const handleCreateAssignment = async () => {
    setError(null);
    setMessage(null);
    if (!form.title.trim()) { setError("Assignment title is required."); return; }
    setCreating(true);
    try {
      await postJson("/api/faculty/assignments", {
        ...form,
        estimatedHours: form.estimatedHours ? Number(form.estimatedHours) : 2,
      });
      setMessage("Assignment created successfully.");
      setForm({ title: "", description: "", deadline: "", estimatedHours: 2, targetType: "CLASS", targetDepartment: "", targetSection: "" });
      loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create assignment.");
    } finally {
      setCreating(false);
    }
  };

  const handleGeneratePlan = async () => {
    setError(null);
    setMessage(null);
    setLoadingTasks(true);
    try {
      const data = await postJson<ScheduledTask[]>("/api/schedule/generate", { maxHoursPerDay: maxHours });
      setTasks(data || []);
      setMessage("Smart schedule generated successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to generate schedule.");
    } finally {
      setLoadingTasks(false);
    }
  };

  const handleCompleteTask = async (id: number) => {
    if (id < 0) {
      return;
    }
    try {
      const updated = await patchJson<ScheduledTask>(`/api/task/${id}/complete`);
      setTasks((prev) => prev.map((t) => (t.id === id ? updated : t)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update task.");
    }
  };

  /* ── Derived ── */
  const workload = useMemo(() => tasks.reduce<Record<string, number>>((acc, t) => {
    acc[t.date] = (acc[t.date] || 0) + (t.hours || 0);
    return acc;
  }, {}), [tasks]);

  const workloadSummary = Object.entries(workload);
  const completedCount = tasks.filter((t) => t.completed).length;
  const totalTaskCount = tasks.length;

  /* ─────────────────────────────────────────────────────────────────── */
  return (
    <div className="space-y-6 pb-10">

      {/* ── Hero ── */}
      <motion.div
        initial={{ opacity: 0, y: -16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="relative overflow-hidden rounded-2xl
          bg-gradient-to-br from-indigo-600 via-blue-600 to-cyan-600
          dark:from-indigo-700 dark:via-blue-700 dark:to-cyan-700
          p-6 shadow-lg shadow-indigo-500/20 dark:shadow-indigo-900/30"
      >
        <div className="absolute -top-10 -right-10 w-48 h-48 rounded-full bg-white/10 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-8 -left-8 w-36 h-36 rounded-full bg-cyan-300/20 blur-2xl pointer-events-none" />
        <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <CalendarClock className="w-4 h-4 text-indigo-200" />
              <p className="text-indigo-200 text-sm font-medium">Smart Assignment Planner</p>
            </div>
            <h1 className="text-2xl md:text-3xl font-bold text-white tracking-tight">
              {isFaculty ? "Manage & Create Assignments" : "Your Study Schedule"}
            </h1>
            <p className="text-indigo-200/80 text-sm mt-1">
              {isFaculty
                ? "Create targeted assignments for classes or individual students."
                : "Auto-generate balanced schedules and keep assignments on track."}
            </p>
          </div>
          {isStudent && totalTaskCount > 0 && (
            <div className="flex items-center gap-3 bg-white/15 backdrop-blur-sm border border-white/20 rounded-xl px-4 py-3 shrink-0">
              <div>
                <p className="text-white/70 text-xs">Progress</p>
                <p className="text-white font-bold text-lg leading-none">{completedCount}/{totalTaskCount}</p>
              </div>
              <div className="w-10 h-10 relative">
                <svg className="w-full h-full -rotate-90" viewBox="0 0 40 40">
                  <circle cx="20" cy="20" r="16" fill="none" stroke="rgba(255,255,255,0.2)" strokeWidth="4" />
                  <motion.circle
                    cx="20" cy="20" r="16" fill="none"
                    stroke="white" strokeWidth="4" strokeLinecap="round"
                    strokeDasharray={`${2 * Math.PI * 16}`}
                    initial={{ strokeDashoffset: 2 * Math.PI * 16 }}
                    animate={{ strokeDashoffset: 2 * Math.PI * 16 * (1 - (totalTaskCount > 0 ? completedCount / totalTaskCount : 0)) }}
                    transition={{ duration: 1, ease: "easeOut" }}
                  />
                </svg>
              </div>
            </div>
          )}
          {isFaculty && (
            <div className="flex items-center gap-2 bg-white/15 backdrop-blur-sm border border-white/20 rounded-xl px-4 py-2.5 shrink-0">
              <Sparkles className="w-4 h-4 text-amber-300" />
              <span className="text-white text-sm font-semibold">{assignments.length} Assignment{assignments.length !== 1 ? "s" : ""}</span>
            </div>
          )}
        </div>
      </motion.div>

      {/* ── Banners ── */}
      <AnimatePresence>
        {message && (
          <motion.div variants={bannerVariants} initial="hidden" animate="visible" exit="exit"
            className="flex items-center gap-3 rounded-xl border border-emerald-200/60 dark:border-emerald-400/20
              bg-emerald-50 dark:bg-emerald-500/10 px-4 py-3 text-sm
              text-emerald-700 dark:text-emerald-300 overflow-hidden"
          >
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span className="flex-1">{message}</span>
            <button onClick={() => setMessage(null)} className="shrink-0 hover:opacity-70 transition-opacity">
              <X className="w-4 h-4" />
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {error && (
          <motion.div variants={bannerVariants} initial="hidden" animate="visible" exit="exit"
            className="flex items-center gap-3 rounded-xl border border-rose-200/60 dark:border-rose-400/20
              bg-rose-50 dark:bg-rose-500/10 px-4 py-3 text-sm
              text-rose-700 dark:text-rose-300 overflow-hidden"
          >
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span className="flex-1">{error}</span>
            <button onClick={() => setError(null)} className="shrink-0 hover:opacity-70 transition-opacity">
              <X className="w-4 h-4" />
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Faculty: Create Assignment ── */}
      {isFaculty && (
        <motion.div custom={0} variants={cardVariants} initial="hidden" animate="visible">
          <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm hover:shadow-md dark:shadow-none transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center shadow-sm">
                  <PlusCircle className="w-4 h-4 text-white" />
                </div>
                <div>
                  <CardTitle className="text-sm text-slate-800 dark:text-slate-100">Create Assignment</CardTitle>
                  <CardDescription className="text-xs mt-0.5">Assign to an individual student or an entire class.</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-5">
              <div>
                <SectionHeading icon={BookOpen} title="Assignment Details" color="from-indigo-500 to-blue-500" />
                <div className="grid gap-3 md:grid-cols-2">
                  <div className="md:col-span-2">
                    <FieldGroup label="Title">
                      <Input value={form.title} onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))} placeholder="Assignment title" className={inputCls} />
                    </FieldGroup>
                  </div>
                  <div className="md:col-span-2">
                    <FieldGroup label="Description">
                      <textarea
                        value={form.description || ""}
                        onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                        placeholder="What should students focus on?"
                        className={`${inputCls} min-h-[80px] resize-none`}
                      />
                    </FieldGroup>
                  </div>
                  <FieldGroup label="Deadline">
                    <Input type="date" value={form.deadline || ""} onChange={(e) => setForm((p) => ({ ...p, deadline: e.target.value }))} className={inputCls} />
                  </FieldGroup>
                  <FieldGroup label="Estimated Hours">
                    <Input type="number" min={1} max={20} value={form.estimatedHours ?? 2} onChange={(e) => setForm((p) => ({ ...p, estimatedHours: Number(e.target.value) }))} className={inputCls} />
                  </FieldGroup>
                </div>
              </div>

              <div>
                <SectionHeading icon={Users} title="Target Audience" color="from-violet-500 to-purple-500" />
                <div className="grid gap-3 md:grid-cols-2">
                  <FieldGroup label="Assign To">
                    <select
                      value={form.targetType}
                      onChange={(e) => setForm((p) => ({ ...p, targetType: e.target.value as "STUDENT" | "CLASS" }))}
                      className={inputCls}
                    >
                      <option value="CLASS">Class</option>
                      <option value="STUDENT">Individual Student</option>
                    </select>
                  </FieldGroup>

                  {form.targetType === "STUDENT" ? (
                    <FieldGroup label="Student ID">
                      <Input type="number" value={form.targetStudentId ?? ""} onChange={(e) => setForm((p) => ({ ...p, targetStudentId: Number(e.target.value) }))} placeholder="Enter student ID" className={inputCls} />
                    </FieldGroup>
                  ) : (
                    <>
                      <FieldGroup label="Department">
                        <Input value={form.targetDepartment ?? ""} onChange={(e) => setForm((p) => ({ ...p, targetDepartment: e.target.value }))} placeholder="e.g. CSE" className={inputCls} />
                      </FieldGroup>
                      <FieldGroup label="Section">
                        <Input value={form.targetSection ?? ""} onChange={(e) => setForm((p) => ({ ...p, targetSection: e.target.value }))} placeholder="e.g. A" className={inputCls} />
                      </FieldGroup>
                    </>
                  )}
                </div>
              </div>

              <motion.button
                onClick={handleCreateAssignment}
                disabled={creating}
                whileHover={{ scale: creating ? 1 : 1.02 }}
                whileTap={{ scale: creating ? 1 : 0.97 }}
                className="w-full flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold text-white
                  bg-gradient-to-r from-indigo-500 to-blue-600
                  hover:opacity-90 hover:shadow-lg hover:shadow-indigo-500/25
                  disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
              >
                {creating
                  ? <><Loader2 className="w-4 h-4 animate-spin" /> Creating...</>
                  : <><PlusCircle className="w-4 h-4" /> Create Assignment</>
                }
              </motion.button>
            </CardContent>
          </Card>
        </motion.div>
      )}

      {/* ── Assignments list ── */}
      <motion.div custom={1} variants={cardVariants} initial="hidden" animate="visible">
        <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm">
          <CardHeader className="pb-3">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-sm">
                <ClipboardList className="w-4 h-4 text-white" />
              </div>
              <div>
                <CardTitle className="text-sm text-slate-800 dark:text-slate-100">
                  {isFaculty ? "Your Assignments" : "Assigned to You"}
                </CardTitle>
                <CardDescription className="text-xs mt-0.5">
                  {isFaculty ? "Assignments you've created." : "Tasks assigned by your faculty."}
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {loadingAssignments ? (
              <div className="flex items-center justify-center gap-2 py-10 text-slate-500 dark:text-slate-400">
                <Loader2 className="w-5 h-5 animate-spin" />
                <span className="text-sm">Loading assignments...</span>
              </div>
            ) : assignments.length === 0 ? (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
                className="flex flex-col items-center justify-center gap-3 py-10 rounded-xl border-2 border-dashed border-slate-200 dark:border-white/10"
              >
                <div className="w-12 h-12 rounded-2xl bg-slate-100 dark:bg-white/[0.06] flex items-center justify-center">
                  <ClipboardList className="w-5 h-5 text-slate-400 dark:text-slate-500" />
                </div>
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">No assignments available</p>
              </motion.div>
            ) : (
              <div className="space-y-3">
                <AnimatePresence>
                  {assignments.map((a, i) => {
                    const urgency = deadlineUrgency(a.deadline);
                    return (
                      <motion.div
                        key={a.id}
                        custom={i}
                        variants={listItem}
                        initial="hidden"
                        animate="visible"
                        exit="exit"
                        className="group flex flex-wrap items-start justify-between gap-4 rounded-2xl border
                          border-slate-100 dark:border-white/8
                          bg-slate-50/60 dark:bg-white/[0.03]
                          hover:border-indigo-200 dark:hover:border-indigo-400/25
                          hover:bg-indigo-50/30 dark:hover:bg-indigo-500/5
                          p-4 transition-all duration-200"
                      >
                        <div className="space-y-1.5 min-w-0 flex-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <p className="text-sm font-semibold text-slate-800 dark:text-white">{a.title}</p>
                            {urgency && (
                              <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${urgency.cls}`}>
                                {urgency.label}
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-slate-500 dark:text-slate-400 line-clamp-2">
                            {a.description || "No description provided"}
                          </p>
                          <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400 dark:text-slate-500 mt-1">
                            {a.deadline && (
                              <span className="flex items-center gap-1">
                                <CalendarDays className="w-3 h-3" />
                                {a.deadline}
                              </span>
                            )}
                            <span className="flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              {a.estimatedHours ?? 2}h estimated
                            </span>
                            {a.createdBy && (
                              <span className="flex items-center gap-1">
                                <User className="w-3 h-3" />
                                {a.createdBy}
                              </span>
                            )}
                          </div>
                        </div>
                        <div className="flex flex-col items-end gap-2 shrink-0">
                          <Badge className={`text-[10px] font-semibold border-0 ${
                            a.targetType === "STUDENT"
                              ? "bg-violet-50 dark:bg-violet-500/15 text-violet-700 dark:text-violet-300"
                              : "bg-indigo-50 dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300"
                          }`}>
                            {a.targetType === "STUDENT"
                              ? <><User className="w-2.5 h-2.5 mr-1 inline" />Student #{a.targetStudentId ?? ""}</>
                              : <><Users className="w-2.5 h-2.5 mr-1 inline" />{a.targetDepartment || "Dept"}-{a.targetSection || "Sec"}</>
                            }
                          </Badge>
                        </div>
                      </motion.div>
                    );
                  })}
                </AnimatePresence>
              </div>
            )}
          </CardContent>
        </Card>
      </motion.div>

      {/* ── Student: Schedule section ── */}
      {isStudent && (
        <>
          {/* Generate plan */}
          <motion.div custom={2} variants={cardVariants} initial="hidden" animate="visible">
            <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shadow-sm">
                    <Zap className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-sm text-slate-800 dark:text-slate-100">Generate Smart Schedule</CardTitle>
                    <CardDescription className="text-xs mt-0.5">Balance your workload across days automatically.</CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap items-end gap-3">
                  <div className="space-y-1.5">
                    <Label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Max hours / day</Label>
                    <Input
                      type="number"
                      min={1}
                      max={8}
                      value={maxHours}
                      onChange={(e) => setMaxHours(Number(e.target.value))}
                      className={`${inputCls} w-28`}
                    />
                  </div>

                  <motion.button
                    onClick={handleGeneratePlan}
                    disabled={loadingTasks}
                    whileHover={{ scale: loadingTasks ? 1 : 1.03 }}
                    whileTap={{ scale: loadingTasks ? 1 : 0.97 }}
                    className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold text-white
                      bg-gradient-to-r from-emerald-500 to-teal-600
                      hover:opacity-90 hover:shadow-lg hover:shadow-emerald-500/25
                      disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                  >
                    {loadingTasks
                      ? <><Loader2 className="w-4 h-4 animate-spin" /> Generating...</>
                      : <><Sparkles className="w-4 h-4" /> Generate Plan</>
                    }
                  </motion.button>

                  <motion.button
                    onClick={loadSchedule}
                    disabled={loadingTasks}
                    whileHover={{ scale: loadingTasks ? 1 : 1.03 }}
                    whileTap={{ scale: loadingTasks ? 1 : 0.97 }}
                    className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold
                      bg-slate-50 dark:bg-white/[0.06]
                      text-slate-700 dark:text-slate-300
                      border border-slate-200 dark:border-white/15
                      hover:border-slate-300 dark:hover:border-white/25
                      disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                  >
                    <RefreshCcw className={`w-4 h-4 ${loadingTasks ? "animate-spin" : ""}`} />
                    Refresh
                  </motion.button>
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* Workload distribution */}
          <AnimatePresence>
            {workloadSummary.length > 0 && (
              <motion.div
                custom={3} variants={cardVariants} initial="hidden" animate="visible"
              >
                <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm">
                  <CardHeader className="pb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-amber-500 to-orange-500 flex items-center justify-center shadow-sm">
                        <BarChart3 className="w-4 h-4 text-white" />
                      </div>
                      <div>
                        <CardTitle className="text-sm text-slate-800 dark:text-slate-100">Workload Distribution</CardTitle>
                        <CardDescription className="text-xs mt-0.5">Estimated study hours per day.</CardDescription>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="flex flex-wrap gap-3">
                      {workloadSummary.map(([date, hours], i) => {
                        const pct = Math.min((hours / maxHours) * 100, 100);
                        const barColor =
                          hours >= maxHours
                            ? "from-red-500 to-rose-500"
                            : hours >= maxHours * 0.7
                            ? "from-amber-500 to-orange-500"
                            : "from-emerald-500 to-teal-500";
                        return (
                          <motion.div
                            key={date}
                            custom={i}
                            variants={fadeUp}
                            initial="hidden"
                            animate="visible"
                            className="flex flex-col gap-2 rounded-xl border border-slate-100 dark:border-white/8
                              bg-slate-50/60 dark:bg-white/[0.04] px-4 py-3 min-w-[100px]"
                          >
                            <p className="text-[10px] font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wide">{date}</p>
                            <p className="text-lg font-bold text-slate-800 dark:text-white leading-none">{hours}h</p>
                            <div className="h-1.5 rounded-full bg-slate-100 dark:bg-white/10 overflow-hidden w-full">
                              <motion.div
                                initial={{ width: 0 }}
                                animate={{ width: `${pct}%` }}
                                transition={{ duration: 0.7, delay: i * 0.08, ease: "easeOut" }}
                                className={`h-full rounded-full bg-gradient-to-r ${barColor}`}
                              />
                            </div>
                          </motion.div>
                        );
                      })}
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Calendar + Checklist */}
          {tasks.length > 0 && (
            <motion.div
              custom={4} variants={cardVariants} initial="hidden" animate="visible"
              className="grid gap-6 lg:grid-cols-2"
            >
              <CalendarView
                tasks={tasks.map((t) => ({
                  id: t.id, assignmentTitle: t.assignmentTitle,
                  date: t.date, taskDetail: t.taskDetail,
                  hours: t.hours, completed: t.completed,
                })) as PlannerTask[]}
              />
              <TaskChecklist
                tasks={tasks.map((t) => ({
                  id: t.id, assignmentTitle: t.assignmentTitle,
                  date: t.date, taskDetail: t.taskDetail,
                  hours: t.hours, completed: t.completed,
                })) as ChecklistTask[]}
                onComplete={handleCompleteTask}
              />
            </motion.div>
          )}
        </>
      )}
    </div>
  );
}

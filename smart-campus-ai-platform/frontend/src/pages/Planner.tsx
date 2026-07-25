import { useEffect, useMemo, useState } from "react";
import { Input } from "@/components/ui/input";
import { API_BASE_URL, getAuthHeaders, getJson, patchJson, postJson } from "@/lib/api";
import NotificationBell from "@/components/NotificationBell";

interface StudyTask {
  id: number;
  dayOrder: number;
  dayLabel: string;
  title: string;
  details: string;
  completed: boolean;
  reminderAt: string | null;
}

interface StudyPlan {
  id: number;
  weekStart: string;
  riskLevel: "LOW" | "MEDIUM" | "HIGH";
  planText: string;
  tasks: StudyTask[];
  createdAt: string;
}

interface StudyMark {
  subject: string;
  score: number;
}

interface PlanHistory {
  id: number;
  weekStart: string;
  riskLevel: "LOW" | "MEDIUM" | "HIGH";
  createdAt: string;
  completedTasks: number;
  totalTasks: number;
}

interface StudyStreak {
  currentStreak: number;
  longestStreak: number;
  lastCompletedDate: string | null;
}

const riskConfig = {
  LOW: { label: "Low Risk", color: "text-emerald-600 dark:text-emerald-400", bg: "bg-emerald-100 dark:bg-emerald-900/40", border: "border-emerald-300 dark:border-emerald-700", dot: "bg-emerald-500", bar: "from-emerald-400 to-teal-500" },
  MEDIUM: { label: "Medium Risk", color: "text-amber-600 dark:text-amber-400", bg: "bg-amber-100 dark:bg-amber-900/40", border: "border-amber-300 dark:border-amber-700", dot: "bg-amber-500", bar: "from-amber-400 to-orange-500" },
  HIGH: { label: "High Risk", color: "text-rose-600 dark:text-rose-400", bg: "bg-rose-100 dark:bg-rose-900/40", border: "border-rose-300 dark:border-rose-700", dot: "bg-rose-500", bar: "from-rose-400 to-pink-500" },
};

const dayEmojis: Record<string, string> = {
  Monday: "🌅", Tuesday: "🌤", Wednesday: "⛅", Thursday: "🌥",
  Friday: "🌇", Saturday: "🌙", Sunday: "🌟",
};

export default function Planner() {
  const [plan, setPlan] = useState<StudyPlan | null>(null);
  const [history, setHistory] = useState<PlanHistory[]>([]);
  const [streak, setStreak] = useState<StudyStreak | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mounted, setMounted] = useState(false);

  const [marks, setMarks] = useState<StudyMark[]>([]);
  const [subject, setSubject] = useState("");
  const [score, setScore] = useState("");
  const [weakSubjects, setWeakSubjects] = useState("");
  const [assignments, setAssignments] = useState("");
  const [riskLevel, setRiskLevel] = useState<"LOW" | "MEDIUM" | "HIGH">("MEDIUM");
  const [weeks, setWeeks] = useState(1);
  const [activeTab, setActiveTab] = useState<"tasks" | "history">("tasks");

  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);

  useEffect(() => {
    setMounted(true);
  }, []);

  const loadLatest = async () => {
    if (!userId) return;
    try {
      const response = await getJson<StudyPlan | null>(`/api/study-plans/latest?userId=${userId}`, { allow404: true });
      setPlan(response ?? null);
    } catch { setPlan(null); }
  };

  const loadHistory = async () => {
    if (!userId) return;
    try {
      const response = await getJson<PlanHistory[]>(`/api/study-plans?userId=${userId}`);
      setHistory(response);
    } catch { /* ignore */ }
  };

  const loadStreak = async () => {
    if (!userId) return;
    try {
      const response = await getJson<StudyStreak>(`/api/study-plans/streak?userId=${userId}`);
      setStreak(response);
    } catch { /* ignore */ }
  };

  useEffect(() => {
    loadLatest();
    loadHistory();
    loadStreak();
  }, [userId]);

  const addMark = () => {
    if (!subject.trim() || !score.trim()) return;
    setMarks((prev) => [...prev, { subject: subject.trim(), score: Number(score) }]);
    setSubject("");
    setScore("");
  };

  const removeMark = (idx: number) => setMarks((prev) => prev.filter((_, i) => i !== idx));

  const createPlan = async () => {
    if (!userId) { setError("Please log in to generate a study plan."); return; }
    setLoading(true);
    setError(null);
    try {
      const safeWeeks = Math.min(8, Math.max(1, Number.isFinite(weeks) ? weeks : 1));
      const payload = {
        userId, marks,
        weakSubjects: weakSubjects.split(",").map((s) => s.trim()).filter(Boolean),
        assignments: assignments.split("\n").map((s) => s.trim()).filter(Boolean),
        riskLevel,
      };
      if (safeWeeks > 1) {
        const response = await postJson<StudyPlan[]>("/api/study-plans/multi", { ...payload, weeks: safeWeeks });
        setPlan(response[0]);
      } else {
        const response = await postJson<StudyPlan>("/api/study-plans", payload);
        setPlan(response);
      }
      await loadHistory();
      setActiveTab("tasks");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to generate plan");
    } finally {
      setLoading(false);
    }
  };

  const loadPlan = async (id: number) => {
    if (id < 0) return;
    try {
      const response = await getJson<StudyPlan>(`/api/study-plans/${id}`);
      setPlan(response);
      setActiveTab("tasks");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load plan");
    }
  };

  const toggleTask = async (task: StudyTask) => {
    if (task.id < 0) return;
    try {
      const updated = await patchJson<StudyTask>(`/api/study-plans/tasks/${task.id}/complete?completed=${!task.completed}`);
      if (!plan) return;
      setPlan({ ...plan, tasks: plan.tasks.map((t) => (t.id === updated.id ? { ...t, completed: updated.completed } : t)) });
      loadStreak();
      loadHistory();
    } catch (err) { setError(err instanceof Error ? err.message : "Failed to update task"); }
  };

  const updateReminderLocal = (taskId: number, reminderAt: string) => {
    setPlan((prev) => {
      if (!prev) return prev;
      return { ...prev, tasks: prev.tasks.map((t) => (t.id === taskId ? { ...t, reminderAt } : t)) };
    });
  };

  const updateReminder = async (task: StudyTask, reminderAt: string) => {
    try {
      const updated = await patchJson<StudyTask>(`/api/study-plans/tasks/${task.id}/reminder?reminderAt=${encodeURIComponent(reminderAt)}`);
      if (!plan) return;
      setPlan({ ...plan, tasks: plan.tasks.map((t) => (t.id === updated.id ? { ...t, reminderAt: updated.reminderAt } : t)) });
    } catch (err) { setError(err instanceof Error ? err.message : "Failed to set reminder"); }
  };

  const exportCalendar = async () => {
    if (!plan || plan.id < 0) return;
    try {
      const response = await fetch(`${API_BASE_URL}/api/study-plans/${plan.id}/export/ics`, {
        headers: getAuthHeaders(),
      });
      const text = await response.text();
      const blob = new Blob([text], { type: "text/calendar" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `study-plan-${plan.id}.ics`;
      link.click();
    } catch (err) { setError(err instanceof Error ? err.message : "Failed to export calendar"); }
  };

  const exportPdf = () => {
    if (!plan || plan.id < 0) return;
    const win = window.open("", "_blank");
    if (!win) return;
    const taskHtml = plan.tasks.map((t) => `<li><strong>${t.dayLabel}:</strong> ${t.title} — ${t.details || ""}</li>`).join("");
    win.document.write(`<html><head><title>Study Plan</title></head><body><h1>Study Plan — Week of ${plan.weekStart}</h1><p>Risk: ${plan.riskLevel}</p><p>${plan.planText || ""}</p><h2>Tasks</h2><ul>${taskHtml}</ul></body></html>`);
    win.document.close();
    win.print();
  };

  const completedCount = plan?.tasks.filter((t) => t.completed).length ?? 0;
  const totalCount = plan?.tasks.length ?? 0;
  const completion = Math.round((completedCount / Math.max(1, totalCount)) * 100);

  const inputCls =
    "h-10 w-full rounded-xl border border-slate-200 bg-white/80 px-3 text-sm text-slate-800 placeholder:text-slate-400 shadow-sm " +
    "transition-all duration-200 focus:border-indigo-400 focus:ring-2 focus:ring-indigo-400/20 focus:outline-none " +
    "dark:border-slate-700 dark:bg-slate-800/80 dark:text-slate-100 dark:placeholder:text-slate-500 dark:focus:border-indigo-500";

  const streakFlame = (streak?.currentStreak ?? 0) >= 7 ? "🔥" : (streak?.currentStreak ?? 0) >= 3 ? "⚡" : "✨";

  return (
    <>
      <style>{`
        @keyframes fadeSlideUp {
          from { opacity: 0; transform: translateY(18px); }
          to   { opacity: 1; transform: translateY(0); }
        }
        @keyframes pulseRing {
          0%, 100% { box-shadow: 0 0 0 0 rgba(99,102,241,.4); }
          50%       { box-shadow: 0 0 0 8px rgba(99,102,241,0); }
        }
        @keyframes shimmer {
          from { background-position: -200% center; }
          to   { background-position: 200% center; }
        }
        @keyframes floatY {
          0%, 100% { transform: translateY(0); }
          50%       { transform: translateY(-6px); }
        }
        @keyframes spinSlow {
          to { transform: rotate(360deg); }
        }
        @keyframes barGrow {
          from { width: 0%; }
          to   { width: var(--bar-w); }
        }
        .anim-fade { animation: fadeSlideUp .5s ease both; }
        .anim-fade-1 { animation: fadeSlideUp .5s .08s ease both; }
        .anim-fade-2 { animation: fadeSlideUp .5s .16s ease both; }
        .anim-fade-3 { animation: fadeSlideUp .5s .24s ease both; }
        .anim-fade-4 { animation: fadeSlideUp .5s .32s ease both; }
        .anim-fade-5 { animation: fadeSlideUp .5s .40s ease both; }
        .float-icon { animation: floatY 3s ease-in-out infinite; }
        .shimmer-btn {
          background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 40%, #6366f1 60%, #6366f1 100%);
          background-size: 200% auto;
          animation: shimmer 2.5s linear infinite;
        }
        .task-row { transition: all .2s ease; }
        .task-row:hover { transform: translateX(4px); }
        .check-box { transition: all .25s cubic-bezier(.34,1.56,.64,1); }
        .check-box:hover { transform: scale(1.15); }
        .history-card { transition: all .2s ease; }
        .history-card:hover { transform: translateY(-2px); }
        .tab-indicator { transition: all .25s cubic-bezier(.34,1.56,.64,1); }
        .mark-chip { animation: fadeSlideUp .3s ease both; }
        .progress-bar { animation: barGrow 1s cubic-bezier(.22,1,.36,1) both; }
      `}</style>

      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50/30 to-sky-50/40 dark:from-slate-950 dark:via-indigo-950/20 dark:to-slate-900 px-4 py-8 font-sans">

        {/* Background grid texture */}
        <div className="pointer-events-none fixed inset-0 opacity-[0.025] dark:opacity-[0.04]"
          style={{ backgroundImage: "radial-gradient(circle, #6366f1 1px, transparent 1px)", backgroundSize: "28px 28px" }} />

        <div className="relative mx-auto max-w-6xl space-y-6">

          {/* ── Top bar ── */}
          <div className="anim-fade flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="float-icon flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 text-xl shadow-lg shadow-indigo-500/30">
                🧠
              </div>
              <div>
                <h1 className="text-xl font-bold tracking-tight text-slate-800 dark:text-slate-100">AI Study Planner</h1>
                <p className="text-xs text-slate-500 dark:text-slate-400">Adaptive weekly plans powered by AI</p>
              </div>
            </div>
            <NotificationBell />
          </div>

          {/* ── Hero stat bar ── */}
          <div className="anim-fade-1 grid grid-cols-3 gap-3">
            {/* Streak card */}
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 to-violet-700 p-4 text-white shadow-xl shadow-indigo-500/20 dark:shadow-indigo-900/30">
              <div className="pointer-events-none absolute -right-4 -top-4 h-24 w-24 rounded-full bg-white/10 blur-2xl" />
              <p className="text-xs font-semibold uppercase tracking-widest text-indigo-200">Current Streak</p>
              <p className="mt-1 text-3xl font-bold">{streak?.currentStreak ?? 0} <span className="text-lg">{streakFlame}</span></p>
              <p className="mt-0.5 text-xs text-indigo-200">Best: {streak?.longestStreak ?? 0} days</p>
            </div>

            {/* Completion */}
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 shadow-sm backdrop-blur-sm dark:border-slate-700/50 dark:bg-slate-800/70">
              <p className="text-xs font-semibold uppercase tracking-widest text-slate-400 dark:text-slate-500">This Week</p>
              <p className="mt-1 text-2xl font-bold text-slate-800 dark:text-slate-100">{completion}%</p>
              <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-700">
                <div
                  className="progress-bar h-full rounded-full bg-gradient-to-r from-indigo-500 to-violet-500"
                  style={{ "--bar-w": `${completion}%`, width: `${completion}%` } as React.CSSProperties}
                />
              </div>
              <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">{completedCount}/{totalCount} tasks done</p>
            </div>

            {/* Risk level */}
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 shadow-sm backdrop-blur-sm dark:border-slate-700/50 dark:bg-slate-800/70">
              <p className="text-xs font-semibold uppercase tracking-widest text-slate-400 dark:text-slate-500">Risk Level</p>
              {plan ? (
                <>
                  <div className="mt-1 flex items-center gap-2">
                    <span className={`h-2.5 w-2.5 rounded-full ${riskConfig[plan.riskLevel].dot} shadow-sm`} />
                    <p className={`text-lg font-bold ${riskConfig[plan.riskLevel].color}`}>{riskConfig[plan.riskLevel].label}</p>
                  </div>
                  <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">Week of {plan.weekStart}</p>
                </>
              ) : (
                <p className="mt-2 text-sm text-slate-400 dark:text-slate-500">No active plan</p>
              )}
            </div>
          </div>

          {/* ── Error ── */}
          {error && (
            <div className="anim-fade flex items-center gap-3 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 shadow-sm dark:border-rose-700/40 dark:bg-rose-900/20 dark:text-rose-300">
              <span>⚠</span> {error}
              <button onClick={() => setError(null)} className="ml-auto text-rose-400 hover:text-rose-600">✕</button>
            </div>
          )}

          {/* ── Main grid ── */}
          <div className="grid gap-6 lg:grid-cols-[1.6fr_1fr]">

            {/* ── Generator form ── */}
            <div className="anim-fade-2 rounded-2xl border border-slate-200/80 bg-white/80 p-6 shadow-sm backdrop-blur-sm dark:border-slate-700/50 dark:bg-slate-900/70">
              <div className="mb-5 flex items-center gap-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-100 text-xl dark:bg-indigo-900/50">📚</div>
                <div>
                  <h2 className="font-semibold text-slate-800 dark:text-slate-100">Generate a Plan</h2>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Input your data and let AI build your week</p>
                </div>
              </div>

              <div className="space-y-4">
                {/* Marks input */}
                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                    Subject Marks
                  </label>
                  <div className="flex gap-2">
                    <input
                      className={inputCls}
                      placeholder="Subject name"
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                      onKeyDown={(e) => e.key === "Enter" && addMark()}
                    />
                    <input
                      className={`${inputCls} w-24`}
                      placeholder="Score"
                      value={score}
                      onChange={(e) => setScore(e.target.value)}
                      onKeyDown={(e) => e.key === "Enter" && addMark()}
                      type="number"
                    />
                    <button
                      type="button"
                      onClick={addMark}
                      className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-indigo-600 text-white shadow-md shadow-indigo-500/25 transition-all hover:bg-indigo-700 active:scale-90 dark:bg-indigo-500 dark:hover:bg-indigo-600"
                    >
                      +
                    </button>
                  </div>
                  {marks.length > 0 && (
                    <div className="mt-2.5 flex flex-wrap gap-2">
                      {marks.map((m, idx) => (
                        <span
                          key={`${m.subject}-${idx}`}
                          className="mark-chip group flex items-center gap-1.5 rounded-lg border border-indigo-200 bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700 dark:border-indigo-700/50 dark:bg-indigo-900/40 dark:text-indigo-300"
                        >
                          {m.subject}
                          <span className="rounded bg-indigo-100 px-1 dark:bg-indigo-800/60">{m.score}</span>
                          <button onClick={() => removeMark(idx)} className="opacity-0 transition-opacity group-hover:opacity-100 text-indigo-400 hover:text-rose-500">×</button>
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                {/* Weak subjects */}
                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                    Weak Subjects <span className="normal-case font-normal text-slate-400">(comma separated)</span>
                  </label>
                  <input className={inputCls} placeholder="e.g. Math, Physics" value={weakSubjects} onChange={(e) => setWeakSubjects(e.target.value)} />
                </div>

                {/* Assignments */}
                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                    Assignments <span className="normal-case font-normal text-slate-400">(one per line)</span>
                  </label>
                  <textarea
                    className={`${inputCls} h-20 resize-none py-2.5`}
                    placeholder="Data Structures assignment&#10;Physics lab report&#10;…"
                    value={assignments}
                    onChange={(e) => setAssignments(e.target.value)}
                  />
                </div>

                {/* Risk + Weeks */}
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">Risk Level</label>
                    <div className="flex gap-1.5">
                      {(["LOW", "MEDIUM", "HIGH"] as const).map((r) => (
                        <button
                          key={r}
                          type="button"
                          onClick={() => setRiskLevel(r)}
                          className={[
                            "flex-1 rounded-lg border py-1.5 text-xs font-semibold transition-all duration-200",
                            riskLevel === r
                              ? `${riskConfig[r].bg} ${riskConfig[r].border} ${riskConfig[r].color} shadow-sm`
                              : "border-slate-200 bg-white text-slate-500 hover:border-slate-300 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-400",
                          ].join(" ")}
                        >
                          {r === "LOW" ? "Low" : r === "MEDIUM" ? "Mid" : "High"}
                        </button>
                      ))}
                    </div>
                  </div>
                  <div>
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">Weeks (1–8)</label>
                    <input
                      type="number"
                      min={1}
                      max={8}
                      className={inputCls}
                      value={weeks}
                      onChange={(e) => setWeeks(Number(e.target.value))}
                    />
                  </div>
                </div>

                {/* Buttons */}
                <div className="flex gap-3 pt-1">
                  <button
                    type="button"
                    onClick={createPlan}
                    disabled={loading}
                    className={[
                      "flex flex-1 items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-bold text-white shadow-lg transition-all duration-200 active:scale-95 disabled:opacity-60",
                      loading ? "bg-indigo-500 cursor-wait" : "shimmer-btn hover:shadow-indigo-500/40 hover:shadow-xl",
                    ].join(" ")}
                  >
                    {loading ? (
                      <>
                        <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                        Generating…
                      </>
                    ) : (
                      <><span>✦</span> Generate Plan</>
                    )}
                  </button>
                  <button
                    type="button"
                    onClick={loadLatest}
                    className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-600 shadow-sm transition-all hover:border-indigo-300 hover:text-indigo-600 active:scale-95 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:border-indigo-600"
                  >
                    Load Latest
                  </button>
                </div>
              </div>
            </div>

            {/* ── Right column ── */}
            <div className="space-y-4">
              {/* Plan overview */}
              <div className="anim-fade-3 rounded-2xl border border-slate-200/80 bg-white/80 p-5 shadow-sm backdrop-blur-sm dark:border-slate-700/50 dark:bg-slate-900/70">
                <div className="mb-3 flex items-center gap-2.5">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-violet-100 text-base dark:bg-violet-900/50">📋</div>
                  <h2 className="font-semibold text-slate-800 dark:text-slate-100">Plan Overview</h2>
                </div>
                <p className="text-sm leading-relaxed text-slate-600 dark:text-slate-300">
                  {plan?.planText || (
                    <span className="italic text-slate-400 dark:text-slate-500">Generate a plan to see this week's overview…</span>
                  )}
                </p>
                {plan && (
                  <div className="mt-4 flex gap-2">
                    <button
                      onClick={exportCalendar}
                      className="flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-sky-200 bg-sky-50 py-2 text-xs font-semibold text-sky-700 transition-all hover:bg-sky-100 active:scale-95 dark:border-sky-700/50 dark:bg-sky-900/20 dark:text-sky-300 dark:hover:bg-sky-900/40"
                    >
                      📅 Calendar
                    </button>
                    <button
                      onClick={exportPdf}
                      className="flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 py-2 text-xs font-semibold text-slate-700 transition-all hover:bg-slate-100 active:scale-95 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
                    >
                      🖨 Export PDF
                    </button>
                  </div>
                )}
              </div>

              {/* Quick history preview */}
              <div className="anim-fade-4 rounded-2xl border border-slate-200/80 bg-white/80 p-5 shadow-sm backdrop-blur-sm dark:border-slate-700/50 dark:bg-slate-900/70">
                <div className="mb-3 flex items-center gap-2.5">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-amber-100 text-base dark:bg-amber-900/50">📈</div>
                  <h2 className="font-semibold text-slate-800 dark:text-slate-100">History</h2>
                </div>
                <div className="space-y-2">
                  {history.slice(0, 3).map((item) => {
                    const cfg = riskConfig[item.riskLevel];
                    const pct = Math.round((item.completedTasks / Math.max(1, item.totalTasks)) * 100);
                    return (
                      <button
                        key={item.id}
                        onClick={() => loadPlan(item.id)}
                        className="history-card group w-full rounded-xl border border-slate-100 bg-slate-50/80 p-3 text-left shadow-sm hover:border-indigo-200 hover:shadow-md dark:border-slate-700/50 dark:bg-slate-800/50 dark:hover:border-indigo-700"
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm font-semibold text-slate-700 dark:text-slate-200">Week of {item.weekStart}</p>
                            <span className={`mt-0.5 inline-block rounded-md px-2 py-0.5 text-xs font-semibold ${cfg.bg} ${cfg.color}`}>
                              {cfg.label}
                            </span>
                          </div>
                          <div className="text-right">
                            <p className="text-sm font-bold text-slate-600 dark:text-slate-300">{pct}%</p>
                            <p className="text-xs text-slate-400">{item.completedTasks}/{item.totalTasks}</p>
                          </div>
                        </div>
                        <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
                          <div
                            className={`h-full rounded-full bg-gradient-to-r ${cfg.bar} transition-all`}
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                      </button>
                    );
                  })}
                  {history.length === 0 && (
                    <p className="py-3 text-center text-sm text-slate-400 dark:text-slate-500">No plans yet</p>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* ── Tasks section ── */}
          <div className="anim-fade-5 rounded-2xl border border-slate-200/80 bg-white/80 shadow-sm backdrop-blur-sm dark:border-slate-700/50 dark:bg-slate-900/70">
            {/* Tab header */}
            <div className="border-b border-slate-100 px-6 pt-5 dark:border-slate-800">
              <div className="flex items-center justify-between">
                <div className="flex gap-1">
                  {(["tasks", "history"] as const).map((tab) => (
                    <button
                      key={tab}
                      onClick={() => setActiveTab(tab)}
                      className={[
                        "relative rounded-t-xl px-5 py-2.5 text-sm font-semibold transition-all duration-200",
                        activeTab === tab
                          ? "text-indigo-700 dark:text-indigo-300"
                          : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200",
                      ].join(" ")}
                    >
                      {tab === "tasks" ? "📅 Daily Tasks" : "🗂 All Plans"}
                      {activeTab === tab && (
                        <span className="tab-indicator absolute inset-x-3 bottom-0 h-0.5 rounded-full bg-gradient-to-r from-indigo-500 to-violet-500" />
                      )}
                    </button>
                  ))}
                </div>
                {plan && activeTab === "tasks" && (
                  <span className={`rounded-full px-3 py-1 text-xs font-bold ${riskConfig[plan.riskLevel].bg} ${riskConfig[plan.riskLevel].color}`}>
                    {plan.riskLevel} RISK
                  </span>
                )}
              </div>
            </div>

            <div className="p-6">
              {/* Daily tasks */}
              {activeTab === "tasks" && (
                <div className="space-y-3">
                  {plan?.tasks.map((task, i) => {
                    const emoji = dayEmojis[task.dayLabel] || "📌";
                    return (
                      <div
                        key={task.id}
                        className={[
                          "task-row group relative overflow-hidden rounded-xl border p-4 shadow-sm",
                          task.completed
                            ? "border-slate-100 bg-slate-50/60 dark:border-slate-800 dark:bg-slate-800/30"
                            : "border-slate-200/80 bg-white/90 hover:border-indigo-200 hover:shadow-md dark:border-slate-700/50 dark:bg-slate-800/60 dark:hover:border-indigo-700",
                        ].join(" ")}
                        style={{ animationDelay: `${i * 0.05}s` }}
                      >
                        {/* left accent */}
                        {!task.completed && (
                          <div className="absolute inset-y-0 left-0 w-1 rounded-l-xl bg-gradient-to-b from-indigo-500 to-violet-500 opacity-0 transition-opacity duration-200 group-hover:opacity-100" />
                        )}
                        <div className="flex items-start justify-between gap-4">
                          <div className="min-w-0 flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <span className="text-base">{emoji}</span>
                              <p className="text-xs font-semibold uppercase tracking-widest text-slate-400 dark:text-slate-500">
                                {task.dayLabel}
                              </p>
                            </div>
                            <p className={[
                              "font-semibold transition-all",
                              task.completed
                                ? "text-slate-400 line-through dark:text-slate-600"
                                : "text-slate-800 dark:text-slate-100",
                            ].join(" ")}>
                              {task.title}
                            </p>
                            {task.details && (
                              <p className={`mt-1 text-sm ${task.completed ? "text-slate-400 dark:text-slate-600" : "text-slate-500 dark:text-slate-400"}`}>
                                {task.details}
                              </p>
                            )}
                            <div className="mt-3 flex items-center gap-2">
                              <span className="text-xs text-slate-400 dark:text-slate-500 shrink-0">⏰ Remind at</span>
                              <input
                                type="datetime-local"
                                value={task.reminderAt ? task.reminderAt.slice(0, 16) : ""}
                                onChange={(e) => updateReminderLocal(task.id, e.target.value)}
                                onBlur={(e) => updateReminder(task, e.target.value)}
                                className={`${inputCls} h-8 text-xs`}
                              />
                            </div>
                          </div>
                          <button
                            onClick={() => toggleTask(task)}
                            aria-label="Toggle task"
                            className={[
                              "check-box mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 shadow-sm",
                              task.completed
                                ? "border-indigo-500 bg-indigo-500 text-white dark:border-indigo-400 dark:bg-indigo-400"
                                : "border-slate-300 bg-white hover:border-indigo-400 dark:border-slate-600 dark:bg-slate-700 dark:hover:border-indigo-500",
                            ].join(" ")}
                          >
                            {task.completed && (
                              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                              </svg>
                            )}
                          </button>
                        </div>
                      </div>
                    );
                  })}
                  {plan && (
                    <p className="pt-1 text-xs text-slate-400 dark:text-slate-500">
                      📧 Reminders are sent to your registered email via SMTP.
                    </p>
                  )}
                  {!plan && (
                    <div className="flex flex-col items-center justify-center gap-3 py-12 text-center">
                      <div className="float-icon text-5xl">🎯</div>
                      <p className="font-semibold text-slate-500 dark:text-slate-400">No plan active</p>
                      <p className="text-sm text-slate-400 dark:text-slate-500">Fill in the form above and hit Generate Plan.</p>
                    </div>
                  )}
                </div>
              )}

              {/* History tab */}
              {activeTab === "history" && (
                <div className="grid gap-3 md:grid-cols-2">
                  {history.map((item) => {
                    const cfg = riskConfig[item.riskLevel];
                    const pct = Math.round((item.completedTasks / Math.max(1, item.totalTasks)) * 100);
                    return (
                      <button
                        key={item.id}
                        onClick={() => loadPlan(item.id)}
                        className="history-card group rounded-xl border border-slate-100 bg-gradient-to-br from-slate-50 to-white p-4 text-left shadow-sm hover:border-indigo-200 hover:shadow-md dark:border-slate-700/50 dark:from-slate-800/60 dark:to-slate-800/40 dark:hover:border-indigo-700"
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <p className="font-semibold text-slate-700 dark:text-slate-200">Week of {item.weekStart}</p>
                            <span className={`mt-1 inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-semibold ${cfg.bg} ${cfg.color}`}>
                              <span className={`h-1.5 w-1.5 rounded-full ${cfg.dot}`} />
                              {cfg.label}
                            </span>
                          </div>
                          <div className="text-right">
                            <p className="text-xl font-bold text-slate-700 dark:text-slate-200">{pct}%</p>
                            <p className="text-xs text-slate-400">{item.completedTasks}/{item.totalTasks} tasks</p>
                          </div>
                        </div>
                        <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-700">
                          <div
                            className={`h-full rounded-full bg-gradient-to-r ${cfg.bar} transition-all duration-500`}
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                      </button>
                    );
                  })}
                  {history.length === 0 && (
                    <div className="col-span-2 flex flex-col items-center gap-2 py-10 text-center">
                      <span className="text-4xl">📭</span>
                      <p className="text-sm text-slate-400 dark:text-slate-500">No plan history yet.</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

        </div>
      </div>
    </>
  );
}

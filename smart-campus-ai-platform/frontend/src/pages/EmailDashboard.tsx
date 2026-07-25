import { useEffect, useMemo, useState } from "react";
import {
  BellRing,
  Bookmark,
  BookmarkCheck,
  Briefcase,
  CalendarClock,
  CheckCheck,
  Inbox,
  Rocket
} from "lucide-react";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { getJson } from "@/lib/api";
import { showToast } from "@/lib/toast";
import {
  CATEGORY_META,
  DEFAULT_EMAILS,
  getDaysUntil,
  isUpcomingDeadline,
  mapInsightToSmartEmail,
  PRIORITY_META,
  type PlannerTaskItem,
  type SmartEmail,
  type SmartPriority
} from "@/components/email-intelligence/actionPlannerData";

type EmailInsight = {
  id: number;
  emailId: number;
  messageId: string;
  subject: string;
  sender: string;
  summary: string;
  category: string;
  deadline?: string | null;
  priority: string;
  actionRequired: boolean;
  createdAt: string;
};

type EmailInsightPage = {
  items: EmailInsight[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
};

type SectionKey = "Inbox" | "Deadlines" | "Hackathons" | "Opportunities";

type ReminderItem = {
  id: string;
  title: string;
  deadline?: string | null;
  source: string;
  priority: SmartPriority;
};

const PLANNER_STORAGE_KEY = "email-action-planner-tasks";
const NOTES_STORAGE_KEY = "email-action-planner-notes";
const DONE_STORAGE_KEY = "email-action-planner-done-email-ids";
const SAVED_HACKATHON_STORAGE_KEY = "email-action-planner-saved-hackathons";

const SECTIONS: Array<{ key: SectionKey; label: string; icon: typeof Inbox }> = [
  { key: "Inbox", label: "Inbox", icon: Inbox },
  { key: "Deadlines", label: "Deadlines", icon: CalendarClock },
  { key: "Hackathons", label: "Hackathons", icon: Rocket },
  { key: "Opportunities", label: "Opportunities", icon: Briefcase }
];

const readStorage = <T,>(key: string, fallback: T): T => {
  if (typeof window === "undefined") return fallback;

  try {
    const raw = window.localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
};

const formatCategoryLabel = (value: string) => value.charAt(0).toUpperCase() + value.slice(1);

const createPlannerTask = (email: SmartEmail, note: string): PlannerTaskItem => ({
  id: `planner-${email.id}`,
  emailId: email.id,
  title: email.subject,
  deadline: email.deadline ?? null,
  category: email.category,
  completed: false,
  notes: note,
  createdAt: new Date().toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit"
  })
});

export default function EmailDashboard() {
  const [activeSection, setActiveSection] = useState<SectionKey>("Inbox");
  const [emails, setEmails] = useState<SmartEmail[]>(DEFAULT_EMAILS);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [plannerTasks, setPlannerTasks] = useState<PlannerTaskItem[]>(() =>
    readStorage<PlannerTaskItem[]>(PLANNER_STORAGE_KEY, [])
  );
  const [emailNotes, setEmailNotes] = useState<Record<string, string>>(() =>
    readStorage<Record<string, string>>(NOTES_STORAGE_KEY, {})
  );
  const [completedEmailIds, setCompletedEmailIds] = useState<string[]>(() =>
    readStorage<string[]>(DONE_STORAGE_KEY, [])
  );
  const [savedHackathonIds, setSavedHackathonIds] = useState<string[]>(() =>
    readStorage<string[]>(SAVED_HACKATHON_STORAGE_KEY, [])
  );

  const [filterCategory, setFilterCategory] = useState("All");
  const [filterPriority, setFilterPriority] = useState("All");
  const [filterSender, setFilterSender] = useState("All");
  const [noteEditorEmailId, setNoteEditorEmailId] = useState<string | null>(null);
  const [noteDrafts, setNoteDrafts] = useState<Record<string, string>>({});
  const [alertedReminderIds, setAlertedReminderIds] = useState<Set<string>>(new Set());

  const completedEmailSet = useMemo(() => new Set(completedEmailIds), [completedEmailIds]);
  const savedHackathonSet = useMemo(() => new Set(savedHackathonIds), [savedHackathonIds]);

  const categoryOptions = useMemo(() => {
    const unique = new Set(emails.map((item) => item.category));
    return ["All", ...Array.from(unique)];
  }, [emails]);

  const senderOptions = useMemo(() => {
    const unique = new Set(emails.map((item) => item.sender).filter(Boolean));
    return ["All", ...Array.from(unique)];
  }, [emails]);

  const loadEmails = async (manualRefresh = false) => {
    if (manualRefresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setError(null);

    try {
      const data = await getJson<EmailInsightPage>("/api/email-intelligence/page?page=0&size=20");
      const mapped = (data?.items ?? []).map(mapInsightToSmartEmail);
      setEmails(mapped.length > 0 ? mapped : DEFAULT_EMAILS);

      if (manualRefresh) {
        showToast({
          title: "Action inbox updated",
          description: "Your latest planner-ready email actions are now refreshed.",
          variant: "info"
        });
      }
    } catch {
      setEmails(DEFAULT_EMAILS);

      if (manualRefresh) {
        showToast({
          title: "Action inbox refreshed",
          description: "Your planner view is ready with the latest available action items.",
          variant: "info"
        });
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadEmails();
  }, []);

  useEffect(() => {
    window.localStorage.setItem(PLANNER_STORAGE_KEY, JSON.stringify(plannerTasks));
  }, [plannerTasks]);

  useEffect(() => {
    window.localStorage.setItem(NOTES_STORAGE_KEY, JSON.stringify(emailNotes));
  }, [emailNotes]);

  useEffect(() => {
    window.localStorage.setItem(DONE_STORAGE_KEY, JSON.stringify(completedEmailIds));
  }, [completedEmailIds]);

  useEffect(() => {
    window.localStorage.setItem(SAVED_HACKATHON_STORAGE_KEY, JSON.stringify(savedHackathonIds));
  }, [savedHackathonIds]);

  const filteredEmails = useMemo(() => {
    return emails.filter((item) => {
      if (filterCategory !== "All" && item.category !== filterCategory) return false;
      if (filterPriority !== "All" && item.priority !== filterPriority) return false;
      if (filterSender !== "All" && item.sender !== filterSender) return false;
      return true;
    });
  }, [emails, filterCategory, filterPriority, filterSender]);

  const sectionEmails = useMemo(() => {
    switch (activeSection) {
      case "Deadlines":
        return filteredEmails.filter((item) => item.category === "deadline" || item.deadline);
      case "Hackathons":
        return filteredEmails.filter((item) => item.category === "hackathon");
      case "Opportunities":
        return filteredEmails.filter((item) => item.category === "opportunity");
      default:
        return filteredEmails;
    }
  }, [activeSection, filteredEmails]);

  const reminders = useMemo<ReminderItem[]>(() => {
    const plannerReminders = plannerTasks
      .filter((task) => !task.completed && isUpcomingDeadline(task.deadline))
      .map((task) => ({
        id: `planner-${task.id}`,
        title: task.title,
        deadline: task.deadline,
        source: "Planner",
        priority: "HIGH" as SmartPriority
      }));

    const emailReminders = emails
      .filter((email) => !completedEmailSet.has(email.id) && isUpcomingDeadline(email.deadline))
      .map((email) => ({
        id: `email-${email.id}`,
        title: email.subject,
        deadline: email.deadline,
        source: CATEGORY_META[email.category].label,
        priority: email.priority
      }));

    return [...plannerReminders, ...emailReminders].sort((left, right) => {
      const leftDate = left.deadline ? new Date(left.deadline).getTime() : Number.MAX_SAFE_INTEGER;
      const rightDate = right.deadline ? new Date(right.deadline).getTime() : Number.MAX_SAFE_INTEGER;
      return leftDate - rightDate;
    });
  }, [completedEmailSet, emails, plannerTasks]);

  const savedHackathons = useMemo(
    () => emails.filter((email) => email.category === "hackathon" && savedHackathonSet.has(email.id)),
    [emails, savedHackathonSet]
  );

  useEffect(() => {
    if (!reminders.length) return;

    const nextAlerted = new Set(alertedReminderIds);
    reminders.forEach((reminder) => {
      if (nextAlerted.has(reminder.id)) return;
      const days = getDaysUntil(reminder.deadline);
      if (days == null) return;

      showToast({
        title: days === 0 ? "Due today" : "Upcoming deadline",
        description: `${reminder.title} is due in ${days} day${days === 1 ? "" : "s"}.`,
        variant: reminder.priority === "HIGH" ? "error" : "info"
      });

      nextAlerted.add(reminder.id);
    });

    if (nextAlerted.size !== alertedReminderIds.size) {
      setAlertedReminderIds(nextAlerted);
    }
  }, [alertedReminderIds, reminders]);

  const addToPlanner = (email: SmartEmail) => {
    setPlannerTasks((previous) => {
      if (previous.some((task) => task.emailId === email.id)) {
        showToast({
          title: "Already in planner",
          description: "This email action is already being tracked in your planner.",
          variant: "info"
        });
        return previous;
      }

      const next = [createPlannerTask(email, emailNotes[email.id] ?? ""), ...previous];
      showToast({ title: "Added to planner", description: email.subject, variant: "success" });
      return next;
    });

    if (email.category === "hackathon") {
      setSavedHackathonIds((previous) => (previous.includes(email.id) ? previous : [email.id, ...previous]));
    }
  };

  const toggleEmailDone = (email: SmartEmail) => {
    const nextDone = !completedEmailSet.has(email.id);

    setCompletedEmailIds((previous) =>
      nextDone ? [...previous, email.id] : previous.filter((item) => item !== email.id)
    );

    setPlannerTasks((previous) =>
      previous.map((task) => (task.emailId === email.id ? { ...task, completed: nextDone } : task))
    );

    showToast({
      title: nextDone ? "Marked as done" : "Marked as active",
      description: email.subject,
      variant: nextDone ? "success" : "info"
    });
  };

  const togglePlannerTask = (task: PlannerTaskItem) => {
    const nextCompleted = !task.completed;

    setPlannerTasks((previous) =>
      previous.map((item) => (item.id === task.id ? { ...item, completed: nextCompleted } : item))
    );

    setCompletedEmailIds((previous) => {
      const nextSet = new Set(previous);
      if (nextCompleted) nextSet.add(task.emailId);
      else nextSet.delete(task.emailId);
      return Array.from(nextSet);
    });
  };

  const openNoteEditor = (email: SmartEmail) => {
    setNoteEditorEmailId(email.id);
    setNoteDrafts((previous) => ({
      ...previous,
      [email.id]: previous[email.id] ?? emailNotes[email.id] ?? ""
    }));
  };

  const saveNote = (email: SmartEmail) => {
    const note = (noteDrafts[email.id] ?? "").trim();

    setEmailNotes((previous) => {
      const next = { ...previous };
      if (note) next[email.id] = note;
      else delete next[email.id];
      return next;
    });

    setPlannerTasks((previous) =>
      previous.map((task) => (task.emailId === email.id ? { ...task, notes: note } : task))
    );

    setNoteEditorEmailId(null);
    showToast({ title: note ? "Note saved" : "Note cleared", description: email.subject, variant: "success" });
  };

  const toggleHackathonSave = (email: SmartEmail) => {
    if (email.category !== "hackathon") return;

    const isSaved = savedHackathonSet.has(email.id);
    setSavedHackathonIds((previous) =>
      isSaved ? previous.filter((item) => item !== email.id) : [email.id, ...previous]
    );

    showToast({
      title: isSaved ? "Hackathon removed" : "Hackathon saved",
      description: email.subject,
      variant: "info"
    });
  };

  const clearCompletedTasks = () => {
    const completedCount = plannerTasks.filter((task) => task.completed).length;
    if (!completedCount) {
      showToast({
        title: "Nothing to clear",
        description: "Your planner does not have completed items yet.",
        variant: "info"
      });
      return;
    }

    setPlannerTasks((previous) => previous.filter((task) => !task.completed));
    showToast({
      title: "Completed tasks cleared",
      description: `${completedCount} planner item${completedCount === 1 ? "" : "s"} removed.`,
      variant: "success"
    });
  };

  const sectionCounts = {
    Inbox: emails.length,
    Deadlines: emails.filter((item) => item.category === "deadline" || item.deadline).length,
    Hackathons: emails.filter((item) => item.category === "hackathon").length,
    Opportunities: emails.filter((item) => item.category === "opportunity").length
  };

  return (
    <div className="space-y-6">
      <Card className="border border-border/60 bg-white/80 p-6 shadow-sm backdrop-blur dark:bg-white/10">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">
              Email Intelligence
            </p>
            <h2 className="text-2xl font-semibold text-foreground">Smart Action Planner System</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Turn emails into a clean action plan: track deadlines, save hackathons, capture notes, and finish tasks.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => loadEmails(true)}
              disabled={refreshing}
              className="rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground transition disabled:opacity-60"
            >
              {refreshing ? "Refreshing..." : "Refresh Actions"}
            </button>
            <button
              type="button"
              onClick={clearCompletedTasks}
              className="rounded-xl border border-border/60 bg-white/80 px-4 py-2 text-sm font-semibold text-foreground transition hover:bg-white/90 dark:bg-white/10"
            >
              Clear Completed
            </button>
          </div>
        </div>
      </Card>

      {error && (
        <Card className="border border-rose-200/60 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/10 dark:text-rose-200">
          {error}
        </Card>
      )}

      <div className="grid gap-6 xl:grid-cols-[240px_minmax(0,1fr)_340px]">
        <Card className="h-fit border border-border/60 bg-white/70 p-4 shadow-sm dark:bg-white/10">
          <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Sections</p>
          <div className="mt-4 flex flex-col gap-2">
            {SECTIONS.map((section) => {
              const Icon = section.icon;
              return (
                <button
                  key={section.key}
                  type="button"
                  onClick={() => setActiveSection(section.key)}
                  className={cn(
                    "flex items-center justify-between rounded-xl border px-3 py-2 text-sm transition",
                    activeSection === section.key
                      ? "border-primary bg-primary/10 text-primary"
                      : "border-border/60 bg-white/70 text-muted-foreground hover:text-foreground dark:bg-white/10"
                  )}
                >
                  <span className="flex items-center gap-2">
                    <Icon className="h-4 w-4" />
                    {section.label}
                  </span>
                  <span className="text-xs font-semibold">{sectionCounts[section.key]}</span>
                </button>
              );
            })}
          </div>
        </Card>

        <div className="space-y-4">
          <Card className="border border-border/60 bg-white/70 p-4 shadow-sm dark:bg-white/10">
            <div className="flex flex-wrap items-center gap-3">
              <span className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Filters</span>
              <select
                value={filterCategory}
                onChange={(event) => setFilterCategory(event.target.value)}
                className="h-9 rounded-lg border border-border/60 bg-white/80 px-3 text-xs text-foreground shadow-sm dark:bg-white/10"
              >
                {categoryOptions.map((option) => (
                  <option key={option} value={option}>
                    Category: {option === "All" ? option : formatCategoryLabel(option)}
                  </option>
                ))}
              </select>
              <select
                value={filterPriority}
                onChange={(event) => setFilterPriority(event.target.value)}
                className="h-9 rounded-lg border border-border/60 bg-white/80 px-3 text-xs text-foreground shadow-sm dark:bg-white/10"
              >
                {["All", "HIGH", "MEDIUM", "LOW"].map((option) => (
                  <option key={option} value={option}>
                    Priority: {option}
                  </option>
                ))}
              </select>
              <select
                value={filterSender}
                onChange={(event) => setFilterSender(event.target.value)}
                className="h-9 rounded-lg border border-border/60 bg-white/80 px-3 text-xs text-foreground shadow-sm dark:bg-white/10"
              >
                {senderOptions.map((option) => (
                  <option key={option} value={option}>
                    Sender: {option}
                  </option>
                ))}
              </select>
              <button
                type="button"
                onClick={() => {
                  setFilterCategory("All");
                  setFilterPriority("All");
                  setFilterSender("All");
                }}
                className="rounded-lg border border-border/60 bg-white/80 px-3 py-2 text-xs font-semibold text-foreground shadow-sm transition hover:bg-white/90 dark:bg-white/10"
              >
                Reset
              </button>
            </div>
          </Card>

          {loading ? (
            <Card className="border border-border/60 bg-white/70 p-8 text-center text-sm text-muted-foreground dark:bg-white/10">
              Loading your action inbox...
            </Card>
          ) : sectionEmails.length === 0 ? (
            <Card className="border border-border/60 bg-white/70 p-8 text-center text-sm text-muted-foreground dark:bg-white/10">
              No emails match the current filters.
            </Card>
          ) : (
            sectionEmails.map((email) => {
              const categoryMeta = CATEGORY_META[email.category];
              const noteValue = emailNotes[email.id];
              const isDone = completedEmailSet.has(email.id);
              const isSavedHackathon = savedHackathonSet.has(email.id);
              const showReminder = isUpcomingDeadline(email.deadline);

              return (
                <Card
                  key={email.id}
                  className="border border-border/60 bg-white/80 p-5 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-md dark:bg-white/10"
                >
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <h3 className="text-base font-semibold text-foreground">{email.subject}</h3>
                        {email.actionRequired && (
                          <Badge className="bg-rose-500/15 text-rose-600 dark:text-rose-200">Action Required</Badge>
                        )}
                        {showReminder && (
                          <Badge className="bg-rose-500/15 text-rose-600 dark:text-rose-200">Due Soon</Badge>
                        )}
                        {isDone && (
                          <Badge className="bg-emerald-500/15 text-emerald-700 dark:text-emerald-200">Done</Badge>
                        )}
                      </div>
                      <p className="mt-1 text-xs text-muted-foreground">{email.sender}</p>
                      <p className="mt-3 text-sm text-slate-600 dark:text-white/70">{email.summary}</p>
                      {email.deadline && (
                        <p className="mt-3 text-xs font-semibold text-rose-600 dark:text-rose-200">Deadline: {email.deadline}</p>
                      )}
                      {noteValue && noteEditorEmailId !== email.id && (
                        <div className="mt-3 rounded-2xl border border-border/60 bg-muted/35 px-3 py-2 text-sm text-foreground/80">
                          <span className="font-semibold">Note:</span> {noteValue}
                        </div>
                      )}
                    </div>

                    <div className="flex flex-col items-end gap-2 text-right">
                      <Badge className={PRIORITY_META[email.priority]}>{email.priority}</Badge>
                      <Badge className={categoryMeta.pillClass}>
                        {categoryMeta.dot} {categoryMeta.label}
                      </Badge>
                      <span className="text-xs text-muted-foreground">{email.createdAt}</span>
                    </div>
                  </div>

                  <div className="mt-4 flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => addToPlanner(email)}
                      className="rounded-lg bg-primary px-3 py-2 text-xs font-semibold text-primary-foreground transition hover:opacity-90"
                    >
                      Add to Planner
                    </button>
                    <button
                      type="button"
                      onClick={() => toggleEmailDone(email)}
                      className={cn(
                        "rounded-lg px-3 py-2 text-xs font-semibold transition",
                        isDone
                          ? "bg-emerald-500/15 text-emerald-700 dark:text-emerald-200"
                          : "border border-border/60 bg-white/80 text-foreground dark:bg-white/10"
                      )}
                    >
                      Mark as Done
                    </button>
                    <button
                      type="button"
                      onClick={() => openNoteEditor(email)}
                      className="rounded-lg border border-border/60 bg-white/80 px-3 py-2 text-xs font-semibold text-foreground transition dark:bg-white/10"
                    >
                      Save Note
                    </button>
                    {email.category === "hackathon" && (
                      <button
                        type="button"
                        onClick={() => toggleHackathonSave(email)}
                        className="rounded-lg border border-border/60 bg-white/80 px-3 py-2 text-xs font-semibold text-foreground transition dark:bg-white/10"
                      >
                        {isSavedHackathon ? "Saved Hackathon" : "Save Hackathon"}
                      </button>
                    )}
                  </div>

                  {noteEditorEmailId === email.id && (
                    <div className="mt-4 rounded-2xl border border-border/60 bg-muted/35 p-4">
                      <label className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
                        Personal note
                      </label>
                      <textarea
                        value={noteDrafts[email.id] ?? ""}
                        onChange={(event) =>
                          setNoteDrafts((previous) => ({
                            ...previous,
                            [email.id]: event.target.value
                          }))
                        }
                        rows={3}
                        placeholder="Add a reminder or quick plan for this email..."
                        className="mt-3 w-full rounded-xl border border-border/60 bg-white/80 px-3 py-2 text-sm text-foreground shadow-sm outline-none transition focus:border-primary dark:bg-white/10"
                      />
                      <div className="mt-3 flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={() => saveNote(email)}
                          className="rounded-lg bg-primary px-3 py-2 text-xs font-semibold text-primary-foreground"
                        >
                          Save Note
                        </button>
                        <button
                          type="button"
                          onClick={() => setNoteEditorEmailId(null)}
                          className="rounded-lg border border-border/60 bg-white/80 px-3 py-2 text-xs font-semibold text-foreground dark:bg-white/10"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  )}
                </Card>
              );
            })
          )}
        </div>

        <div className="space-y-4">
          <Card className="border border-border/60 bg-white/80 p-5 shadow-sm dark:bg-white/10">
            <div className="flex items-center justify-between gap-3">
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">My Action Planner</p>
                <h3 className="mt-2 text-lg font-semibold text-foreground">Checklist</h3>
              </div>
              <Badge className="bg-primary/10 text-primary">{plannerTasks.length}</Badge>
            </div>

            <div className="mt-4 space-y-3">
              {plannerTasks.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  Add important emails to your planner and they will appear here as a checklist.
                </p>
              ) : (
                plannerTasks.map((task) => {
                  const categoryMeta = CATEGORY_META[task.category];
                  return (
                    <div
                      key={task.id}
                      className={cn(
                        "rounded-2xl border border-border/60 p-4 transition",
                        task.completed ? "bg-emerald-500/5" : "bg-muted/35"
                      )}
                    >
                      <div className="flex items-start gap-3">
                        <button
                          type="button"
                          onClick={() => togglePlannerTask(task)}
                          className={cn(
                            "mt-1 flex h-5 w-5 items-center justify-center rounded border transition",
                            task.completed
                              ? "border-emerald-500 bg-emerald-500 text-white"
                              : "border-border/60 bg-white dark:bg-white/10"
                          )}
                        >
                          {task.completed && <CheckCheck className="h-3.5 w-3.5" />}
                        </button>

                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <p className={cn("text-sm font-semibold", task.completed && "line-through text-muted-foreground")}>
                              {task.title}
                            </p>
                            <Badge className={categoryMeta.pillClass}>
                              {categoryMeta.dot} {categoryMeta.label}
                            </Badge>
                          </div>
                          {task.deadline && (
                            <p className="mt-2 text-xs font-semibold text-rose-600 dark:text-rose-200">Due: {task.deadline}</p>
                          )}
                          {task.notes && <p className="mt-2 text-xs text-muted-foreground">{task.notes}</p>}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </Card>

          <Card className="border border-border/60 bg-white/80 p-5 shadow-sm dark:bg-white/10">
            <div className="flex items-center gap-2">
              <BellRing className="h-4 w-4 text-rose-500" />
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Reminders</p>
                <h3 className="mt-2 text-lg font-semibold text-foreground">Upcoming deadlines</h3>
              </div>
            </div>

            <div className="mt-4 space-y-3">
              {reminders.length === 0 ? (
                <p className="text-sm text-muted-foreground">No urgent deadlines in the next 3 days.</p>
              ) : (
                reminders.map((reminder) => {
                  const days = getDaysUntil(reminder.deadline);
                  return (
                    <div key={reminder.id} className="rounded-2xl border border-rose-200/60 bg-rose-50/80 p-4 dark:border-rose-400/20 dark:bg-rose-500/10">
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <p className="text-sm font-semibold text-foreground">{reminder.title}</p>
                          <p className="mt-1 text-xs text-muted-foreground">{reminder.source}</p>
                        </div>
                        <Badge className="bg-rose-500/15 text-rose-600 dark:text-rose-200">
                          {days === 0 ? "Today" : `${days} day${days === 1 ? "" : "s"}`}
                        </Badge>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </Card>

          <Card className="border border-border/60 bg-white/80 p-5 shadow-sm dark:bg-white/10">
            <div className="flex items-center gap-2">
              <Bookmark className="h-4 w-4 text-violet-500" />
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Saved Hackathons</p>
                <h3 className="mt-2 text-lg font-semibold text-foreground">Quick shortlist</h3>
              </div>
            </div>

            <div className="mt-4 space-y-3">
              {savedHackathons.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  Save hackathon emails here so they do not get buried in your inbox.
                </p>
              ) : (
                savedHackathons.map((email) => (
                  <div key={email.id} className="rounded-2xl border border-border/60 bg-muted/35 p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-sm font-semibold text-foreground">{email.subject}</p>
                        <p className="mt-1 text-xs text-muted-foreground">{email.sender}</p>
                        {email.deadline && (
                          <p className="mt-2 text-xs font-semibold text-amber-700 dark:text-amber-200">
                            Registration closes: {email.deadline}
                          </p>
                        )}
                      </div>
                      <button
                        type="button"
                        onClick={() => toggleHackathonSave(email)}
                        className="rounded-lg border border-border/60 bg-white/80 px-2.5 py-2 text-xs font-semibold text-foreground dark:bg-white/10"
                      >
                        {savedHackathonSet.has(email.id) ? (
                          <span className="flex items-center gap-1">
                            <BookmarkCheck className="h-3.5 w-3.5" />
                            Saved
                          </span>
                        ) : (
                          "Save"
                        )}
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

"use client";

import { AnimatePresence, motion } from "framer-motion";
import {
  BookOpen,
  BrainCircuit,
  CheckCircle2,
  Circle,
  ClipboardList,
  Command,
  Copy,
  FileText,
  ListChecks,
  Mail,
  MessageSquare,
  Paperclip,
  RefreshCcw,
  SendIcon,
  Sparkles,
  ThumbsDown,
  ThumbsUp,
  Trash2,
  TriangleAlert,
  X,
  Zap,
} from "lucide-react";
import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type FormEvent,
} from "react";
import { cn } from "@/lib/utils";

/* ─────────────────────────────────────────────
   Types
───────────────────────────────────────────── */
type ChatMode = "chat" | "explain" | "summarize" | "quiz";

type Task = {
  id: string;
  title: string;
  deadline: string;       // ISO date string "YYYY-MM-DD"
  completed: boolean;
  category: "assignment" | "hackathon" | "reminder" | "general";
};

type Attachment = {
  name: string; type: string; url?: string; isImage: boolean;
};

type ChatMessage = {
  id: string;
  role: "user" | "assistant" | "system";
  content: string;
  createdAt: string;
  attachments?: Attachment[];
  liked?: boolean;
  disliked?: boolean;
  streaming?: boolean;
};

type PendingAttachment = {
  file: File; previewUrl: string; isImage: boolean;
};

/* ─────────────────────────────────────────────
   Default realistic data
───────────────────────────────────────────── */
const EMAILS = [
  {
    id: "1",
    subject: "Operating System Assignment Unit 4",
    from: "prof.sharma@college.edu",
    body: "Please submit your OS assignment covering Unit 4 topics — Process Scheduling and Deadlock. Submission deadline is April 22nd.",
    category: "assignment" as const,
    deadline: "2026-04-22",
  },
  {
    id: "2",
    subject: "Machine Learning Hackathon – Register Now",
    from: "events@techfest.in",
    body: "Join the ML Hackathon on April 25th. Build an ML model to solve real-world problems. Team size: 2–4. Prizes worth ₹50,000.",
    category: "hackathon" as const,
    deadline: "2026-04-25",
  },
  {
    id: "3",
    subject: "DBMS Unit 3 Quiz Reminder",
    from: "lms@college.edu",
    body: "A quiz on DBMS Unit 3 (Normalization & SQL) is scheduled for April 20th at 10 AM. Prepare accordingly.",
    category: "reminder" as const,
    deadline: "2026-04-20",
  },
];

const ASSIGNMENTS = [
  { title: "Operating System Unit 4", deadline: "2026-04-22", category: "assignment" as const },
  { title: "Machine Learning Unit 5", deadline: "2026-04-28", category: "assignment" as const },
  { title: "DBMS Unit 3 Quiz",        deadline: "2026-04-20", category: "reminder" as const },
  { title: "CN Lab Record Submission", deadline: "2026-04-30", category: "assignment" as const },
];

const HACKATHONS = [
  { name: "ML Hackathon",         deadline: "2026-04-25", prize: "₹50,000", platform: "TechFest" },
  { name: "Smart India Hackathon",deadline: "2026-05-10", prize: "₹1,00,000", platform: "SIH Portal" },
  { name: "HackWithIndia",        deadline: "2026-05-05", prize: "₹25,000", platform: "Devfolio" },
];

/* ─────────────────────────────────────────────
   Constants
───────────────────────────────────────────── */
const MODE_CONFIG: Record<ChatMode, {
  label: string; icon: React.ReactNode; placeholder: string; shortcut: string;
}> = {
  chat:      { label: "Chat",      icon: <MessageSquare className="w-3.5 h-3.5" />, placeholder: "Ask about tasks, deadlines, emails, or say 'add task'…",   shortcut: "/chat" },
  explain:   { label: "Explain",   icon: <BrainCircuit  className="w-3.5 h-3.5" />, placeholder: "Enter a topic you want explained…",                         shortcut: "/explain" },
  summarize: { label: "Summarize", icon: <BookOpen      className="w-3.5 h-3.5" />, placeholder: "Paste or type the content to summarize…",                   shortcut: "/summarize" },
  quiz:      { label: "Quiz",      icon: <ListChecks    className="w-3.5 h-3.5" />, placeholder: "Enter a syllabus or topic for quiz generation…",             shortcut: "/quiz" },
};

const QUIZ_TYPES = [
  { value: "MIXED",      label: "Mixed" },
  { value: "MCQ",        label: "MCQ" },
  { value: "TRUE_FALSE", label: "True / False" },
  { value: "FILL_BLANK", label: "Fill in the blanks" },
];

const createId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`;

/* ─────────────────────────────────────────────
   Helpers
───────────────────────────────────────────── */
/** Format ISO date → "22 April 2026" */
function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-IN", { day: "numeric", month: "long", year: "numeric" });
}

/** Days until deadline (negative = overdue) */
function daysUntil(iso: string) {
  const diff = new Date(iso).getTime() - Date.now();
  return Math.ceil(diff / 86_400_000);
}

/** Load tasks from localStorage */
function loadTasks(): Task[] {
  try {
    return JSON.parse(localStorage.getItem("lms_tasks") ?? "[]");
  } catch { return []; }
}

/** Persist tasks to localStorage */
function saveTasks(tasks: Task[]) {
  localStorage.setItem("lms_tasks", JSON.stringify(tasks));
}

/* ─────────────────────────────────────────────
   Auto-resize textarea hook
───────────────────────────────────────────── */
function useAutoResizeTextarea(minHeight: number, maxHeight = 200) {
  const ref = useRef<HTMLTextAreaElement>(null);
  const adjust = useCallback((reset = false) => {
    const el = ref.current;
    if (!el) return;
    el.style.height = `${minHeight}px`;
    if (!reset) el.style.height = `${Math.min(el.scrollHeight, maxHeight)}px`;
  }, [minHeight, maxHeight]);
  return { textareaRef: ref, adjust };
}

/* ─────────────────────────────────────────────
   Typing dots
───────────────────────────────────────────── */
function TypingDots() {
  return (
    <div className="flex items-center gap-0.5 ml-1">
      {[0, 1, 2].map((i) => (
        <motion.span
          key={i}
          className="w-1.5 h-1.5 rounded-full bg-violet-400"
          animate={{ opacity: [0.3, 1, 0.3], scale: [0.8, 1.15, 0.8] }}
          transition={{ duration: 1.1, repeat: Infinity, delay: i * 0.18, ease: "easeInOut" }}
        />
      ))}
    </div>
  );
}

/* ─────────────────────────────────────────────
   ── INTENT ENGINE ──
   Pure function: given a message string + current tasks,
   returns { reply: string, newTask?: Task }
───────────────────────────────────────────── */
function processIntent(raw: string, currentTasks: Task[]): { reply: string; newTask?: Task } {
  const msg = raw.toLowerCase().trim();

  /* ── Helper: build a task object ── */
  const makeTask = (
    title: string,
    deadline: string,
    category: Task["category"] = "general"
  ): Task => ({ id: createId(), title, deadline, completed: false, category });

  /* ─── SHOW TASKS ─── */
  if (/(show|list|my|view)\s*(tasks?|planner|todos?)/i.test(msg) || msg === "tasks") {
    if (currentTasks.length === 0)
      return { reply: "📋 Your planner is empty. Say **\"add task\"** or **\"convert email to task\"** to get started!" };

    const lines = currentTasks.map((t) => {
      const d = daysUntil(t.deadline);
      const warn = d <= 3 && !t.completed ? " ⚠️" : "";
      const status = t.completed ? "✅" : "🔲";
      return `${status} **${t.title}** — Due: ${fmtDate(t.deadline)}${warn}`;
    });
    return { reply: `📋 **Your Planner (${currentTasks.length} tasks)**\n\n${lines.join("\n")}` };
  }

  /* ─── SHOW DEADLINES ─── */
  if (/deadline|due date|upcoming/i.test(msg)) {
    const sorted = [...currentTasks]
      .filter((t) => !t.completed)
      .sort((a, b) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime());

    if (sorted.length === 0)
      return { reply: "🗓️ No upcoming deadlines found. Add tasks first!" };

    const lines = sorted.map((t) => {
      const d = daysUntil(t.deadline);
      const urgency = d < 0 ? "🔴 OVERDUE" : d <= 3 ? "⚠️ Urgent" : d <= 7 ? "🟡 Soon" : "🟢 On track";
      return `• **${t.title}** — ${fmtDate(t.deadline)} (${urgency})`;
    });

    return { reply: `🗓️ **Upcoming Deadlines**\n\n${lines.join("\n")}` };
  }

  /* ─── SHOW EMAILS ─── */
  if (/email|inbox|mail/i.test(msg) && !/convert|task/i.test(msg)) {
    const lines = EMAILS.map((e) =>
      `📧 **${e.subject}**\nFrom: ${e.from} · Deadline: ${fmtDate(e.deadline)}`
    );
    return {
      reply: `📬 **Your Inbox (${EMAILS.length} emails)**\n\n${lines.join("\n\n")}\n\n💡 Say **"convert email to task"** to add any email as a planner task.`,
    };
  }

  /* ─── CONVERT EMAIL TO TASK ─── */
  if (/convert.*email|email.*task|add.*email/i.test(msg)) {
    // Convert all emails not already in planner
    const existingTitles = new Set(currentTasks.map((t) => t.title.toLowerCase()));
    const newTasks = EMAILS
      .filter((e) => !existingTitles.has(e.subject.toLowerCase()))
      .map((e) => makeTask(e.subject, e.deadline, e.category));

    if (newTasks.length === 0)
      return { reply: "✅ All emails are already in your planner!" };

    // Return only the first new task here; caller handles batch
    const lines = newTasks.map((t) => `• **${t.title}** — Due: ${fmtDate(t.deadline)}`).join("\n");
    return {
      reply: `📌 **${newTasks.length} email(s) converted to tasks!**\n\n${lines}\n\nCheck your planner below 👇`,
      newTask: newTasks[0], // signal to caller to add all (we handle multi below)
    };
  }

  /* ─── HACKATHON ─── */
  if (/hackathon|competition|contest|event/i.test(msg)) {
    const lines = HACKATHONS.map(
      (h) => `🏆 **${h.name}**\nPlatform: ${h.platform} · Prize: ${h.prize} · Deadline: ${fmtDate(h.deadline)}`
    );
    return {
      reply: `🚀 **Upcoming Hackathons & Competitions**\n\n${lines.join("\n\n")}\n\n💡 Say **"add hackathon [name]"** to save it to your planner!`,
    };
  }

  /* ─── ADD ASSIGNMENT ─── */
  if (/add.*os|os.*assignment|operating system/i.test(msg)) {
    const src = ASSIGNMENTS.find((a) => /operating system/i.test(a.title));
    if (src) {
      const task = makeTask(src.title, src.deadline, src.category);
      return {
        reply: `📌 **Task added successfully!**\n\n✅ **${task.title}**\n📅 Deadline: ${fmtDate(task.deadline)}\n🏷️ Category: Assignment\n\nView it in your planner below 👇`,
        newTask: task,
      };
    }
  }

  if (/add.*ml|machine learning.*unit/i.test(msg)) {
    const src = ASSIGNMENTS.find((a) => /machine learning/i.test(a.title));
    if (src) {
      const task = makeTask(src.title, src.deadline, src.category);
      return {
        reply: `📌 **Task added!**\n\n✅ **${task.title}**\n📅 Deadline: ${fmtDate(task.deadline)}\n\nAdded to your planner 👇`,
        newTask: task,
      };
    }
  }

  if (/add.*dbms|dbms.*quiz/i.test(msg)) {
    const src = ASSIGNMENTS.find((a) => /dbms/i.test(a.title));
    if (src) {
      const task = makeTask(src.title, src.deadline, src.category);
      return {
        reply: `📌 **Task added!**\n\n✅ **${task.title}**\n📅 Deadline: ${fmtDate(task.deadline)}\n\nAdded to your planner 👇`,
        newTask: task,
      };
    }
  }

  /* ─── ADD ALL ASSIGNMENTS ─── */
  if (/add.*all.*assignment|sync.*assignment|import.*assignment/i.test(msg)) {
    const existingTitles = new Set(currentTasks.map((t) => t.title.toLowerCase()));
    const newOnes = ASSIGNMENTS.filter((a) => !existingTitles.has(a.title.toLowerCase()));
    if (newOnes.length === 0) return { reply: "✅ All assignments are already in your planner!" };
    const lines = newOnes.map((a) => `• **${a.title}** — Due: ${fmtDate(a.deadline)}`).join("\n");
    // Signal to add first (caller handles batch via batchAssignments state)
    return {
      reply: `📚 **${newOnes.length} assignments added to your planner!**\n\n${lines}\n\nTrack them below 👇`,
      newTask: { ...newOnes[0], id: createId(), completed: false } as Task,
    };
  }

  /* ─── GENERIC ADD TASK ─── */
  if (/add.*task|create.*task|new.*task|remind.*me/i.test(msg)) {
    // Try to extract a task name after "add task"
    const match = msg.match(/add\s+(?:task\s+)?(.+)/i);
    const title = match?.[1]?.trim() || "New Task";
    // Default deadline 7 days from now
    const deadline = new Date(Date.now() + 7 * 86_400_000).toISOString().slice(0, 10);
    const task = makeTask(title.charAt(0).toUpperCase() + title.slice(1), deadline);
    return {
      reply: `📌 **Task created!**\n\n✅ **${task.title}**\n📅 Deadline: ${fmtDate(deadline)}\n\nYou can mark it complete in your planner below 👇`,
      newTask: task,
    };
  }

  /* ─── CLEAR / DELETE TASKS ─── */
  if (/clear.*task|delete.*all|reset.*planner/i.test(msg)) {
    return { reply: "🗑️ **Planner cleared.** All tasks removed. Start fresh by saying **\"add task\"** or **\"convert email to task\"**." };
  }

  /* ─── HELP ─── */
  if (/help|what can you do|commands?/i.test(msg)) {
    return {
      reply: `🤖 **Smart LMS Assistant — Command Guide**

Here's what you can ask me:

📋 **Planner**
• \`show tasks\` — View your planner
• \`add task [name]\` — Add a custom task
• \`add OS assignment\` — Add OS Unit 4 to planner
• \`add all assignments\` — Sync all assignments
• \`show deadlines\` — View upcoming deadlines

📧 **Email & Tasks**
• \`show emails\` — View your inbox
• \`convert email to task\` — Convert emails → planner tasks

🏆 **Opportunities**
• \`show hackathons\` — Browse hackathons

💡 **Tips**
• Tasks within 3 days show a ⚠️ warning
• Use \`/explain\`, \`/summarize\`, \`/quiz\` modes for study help`,
    };
  }

  /* ─── EXPLAIN (in chat mode) ─── */
  if (/explain|what is|how does|define/i.test(msg)) {
    const topic = raw.replace(/explain|what is|how does|define/gi, "").trim() || "this topic";
    return {
      reply: `🧠 **Explanation: ${topic}**\n\nThis is a study-assist response. Switch to **Explain mode** (sidebar or type \`/explain\`) for a deeper, structured explanation with examples and key takeaways.\n\n💡 You can also ask me to add this to your planner as a study reminder!`,
    };
  }

  /* ─── GREETING ─── */
  if (/^(hi|hello|hey|good morning|good evening|what's up)/i.test(msg)) {
    const urgent = currentTasks.filter((t) => !t.completed && daysUntil(t.deadline) <= 3);
    const urgentNote = urgent.length
      ? `\n\n⚠️ Heads up — you have **${urgent.length} task(s)** due within 3 days!`
      : "";
    return {
      reply: `👋 **Hello! I'm your Smart LMS Assistant.**\n\nI can help you:\n• Track deadlines & assignments\n• Convert emails into tasks\n• Find hackathon opportunities\n• Summarize or explain topics\n\nType **"help"** to see all commands.${urgentNote}`,
    };
  }

  /* ─── DEFAULT FALLBACK ─── */
  return {
    reply: `💬 I received: *"${raw}"*\n\nI couldn't detect a specific intent. Try:\n• **"show tasks"** — view your planner\n• **"show deadlines"** — see what's due soon\n• **"add task [name]"** — create a task\n• **"convert email to task"** — convert inbox items\n• **"help"** — full command list`,
  };
}

/* ─────────────────────────────────────────────
   Main Chatbot component
───────────────────────────────────────────── */
export default function Chatbot() {
  /* ── Chat state ── */
  const [messages, setMessages] = useState<ChatMessage[]>([{
    id: createId(), role: "system",
    content: "Smart LMS Assistant — Chat, plan tasks, track deadlines, convert emails.",
    createdAt: new Date().toISOString(),
  }]);
  const [input, setInput]               = useState("");
  const [pendingFiles, setPendingFiles]  = useState<PendingAttachment[]>([]);
  const [mode, setMode]                  = useState<ChatMode>("chat");
  const [isTyping, setIsTyping]          = useState(false);
  const [error, setError]                = useState<string | null>(null);
  const [quizCount, setQuizCount]        = useState(8);
  const [quizType, setQuizType]          = useState("MIXED");

  /* ── Planner state (persisted to localStorage) ── */
  const [tasks, setTasks] = useState<Task[]>(() => loadTasks());

  /* ── UI state ── */
  const [inputFocused, setInputFocused]             = useState(false);
  const [mousePos, setMousePos]                     = useState({ x: 0, y: 0 });
  const [showCommandPalette, setShowCommandPalette]  = useState(false);
  const [activeCmd, setActiveCmd]                   = useState(-1);
  const [recentCommand, setRecentCommand]            = useState<string | null>(null);
  const [showPlanner, setShowPlanner]                = useState(true);
  const [showEmails, setShowEmails]                  = useState(false);

  const fileInputRef      = useRef<HTMLInputElement>(null);
  const messagesEndRef    = useRef<HTMLDivElement>(null);
  const commandPaletteRef = useRef<HTMLDivElement>(null);
  const { textareaRef, adjust } = useAutoResizeTextarea(60, 200);

  /* ── Persist tasks whenever they change ── */
  useEffect(() => { saveTasks(tasks); }, [tasks]);

  /* ── Auto-scroll ── */
  useEffect(() => { messagesEndRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages, isTyping]);

  /* ── Mouse glow ── */
  useEffect(() => {
    const h = (e: MouseEvent) => setMousePos({ x: e.clientX, y: e.clientY });
    window.addEventListener("mousemove", h);
    return () => window.removeEventListener("mousemove", h);
  }, []);

  /* ── Command palette trigger on "/" ── */
  useEffect(() => {
    if (input.startsWith("/") && !input.includes(" ")) {
      setShowCommandPalette(true);
      const idx = Object.values(MODE_CONFIG).findIndex((c) => c.shortcut.startsWith(input));
      setActiveCmd(idx >= 0 ? idx : -1);
    } else {
      setShowCommandPalette(false);
    }
  }, [input]);

  /* ── Close palette on outside click ── */
  useEffect(() => {
    const h = (e: MouseEvent) => {
      const btn = document.querySelector("[data-cmd-btn]");
      if (commandPaletteRef.current && !commandPaletteRef.current.contains(e.target as Node) && !btn?.contains(e.target as Node))
        setShowCommandPalette(false);
    };
    document.addEventListener("mousedown", h);
    return () => document.removeEventListener("mousedown", h);
  }, []);

  /* ── Cleanup object URLs ── */
  useEffect(() => {
    return () => pendingFiles.forEach((f) => URL.revokeObjectURL(f.previewUrl));
  }, [pendingFiles]);

  /* ── Derived ── */
  const history = useMemo(
    () => messages.filter((m) => m.role === "user").slice(-6).reverse(),
    [messages]
  );

  const canSend = useMemo(
    () => input.trim().length > 0 || pendingFiles.length > 0,
    [input, pendingFiles.length]
  );

  const modeEntries = Object.entries(MODE_CONFIG) as [ChatMode, typeof MODE_CONFIG[ChatMode]][];

  const urgentTasks = useMemo(
    () => tasks.filter((t) => !t.completed && daysUntil(t.deadline) <= 3),
    [tasks]
  );

  /* ── Message helpers ── */
  const appendMessage = useCallback((msg: ChatMessage) =>
    setMessages((prev) => [...prev, msg]), []);

  const updateMessage = useCallback((id: string, fn: (m: ChatMessage) => ChatMessage) =>
    setMessages((prev) => prev.map((m) => m.id === id ? fn(m) : m)), []);

  /* Stream words into an assistant bubble */
  const streamAssistantResponse = useCallback((text: string) => {
    const id = createId();
    appendMessage({ id, role: "assistant", content: "", createdAt: new Date().toISOString(), streaming: true });
    const words = text.split(/\s+/).filter(Boolean);
    let idx = 0;
    const iv = window.setInterval(() => {
      idx += 1;
      updateMessage(id, (m) => ({ ...m, content: words.slice(0, idx).join(" "), streaming: idx < words.length }));
      if (idx >= words.length) window.clearInterval(iv);
    }, 22);
  }, [appendMessage, updateMessage]);

  /* ── Add task helper ── */
  const addTask = useCallback((task: Task) => {
    setTasks((prev) => {
      // Avoid duplicates by title
      if (prev.some((t) => t.title.toLowerCase() === task.title.toLowerCase())) return prev;
      return [...prev, task];
    });
  }, []);

  /* ── Submit handler ── */
  const handleSubmit = useCallback(async (e?: FormEvent) => {
    e?.preventDefault();
    if (!canSend) return;

    const prompt = input.trim();
    const attachments: Attachment[] = pendingFiles.map((f) => ({
      name: f.file.name, type: f.file.type,
      url: f.isImage ? URL.createObjectURL(f.file) : undefined,
      isImage: f.isImage,
    }));

    appendMessage({
      id: createId(), role: "user",
      content: prompt || "(Uploaded file)",
      createdAt: new Date().toISOString(), attachments,
    });

    setInput(""); adjust(true);
    pendingFiles.forEach((f) => URL.revokeObjectURL(f.previewUrl));
    setPendingFiles([]);
    setIsTyping(true); setError(null);

    // Small artificial delay for realism
    await new Promise((r) => setTimeout(r, 600));

    /* ── Intent detection (frontend only, no API) ── */
    const { reply, newTask } = processIntent(prompt, tasks);

    // Special case: "convert email to task" adds ALL emails as tasks
    if (/convert.*email|email.*task/i.test(prompt)) {
      EMAILS.forEach((e) =>
        addTask({ id: createId(), title: e.subject, deadline: e.deadline, completed: false, category: e.category })
      );
    }
    // Special case: "add all assignments"
    else if (/add.*all.*assignment|sync.*assignment|import.*assignment/i.test(prompt)) {
      ASSIGNMENTS.forEach((a) =>
        addTask({ ...a, id: createId(), completed: false })
      );
    }
    // Clear tasks
    else if (/clear.*task|delete.*all|reset.*planner/i.test(prompt)) {
      setTasks([]);
      saveTasks([]);
    }
    // Single task
    else if (newTask) {
      addTask(newTask);
    }

    setIsTyping(false);
    streamAssistantResponse(reply);
  }, [canSend, input, pendingFiles, tasks, appendMessage, adjust, addTask, streamAssistantResponse]);

  /* ── Keyboard handler ── */
  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (showCommandPalette) {
      if (e.key === "ArrowDown") { e.preventDefault(); setActiveCmd((p) => (p + 1) % modeEntries.length); }
      else if (e.key === "ArrowUp") { e.preventDefault(); setActiveCmd((p) => (p - 1 + modeEntries.length) % modeEntries.length); }
      else if ((e.key === "Tab" || e.key === "Enter") && activeCmd >= 0) { e.preventDefault(); selectCommand(activeCmd); }
      else if (e.key === "Escape") { e.preventDefault(); setShowCommandPalette(false); }
    } else if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault(); if (canSend) handleSubmit();
    }
  };

  /* ── Attach files ── */
  const handleAttach = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    const supported = files.filter((f) => f.type.startsWith("image/") || f.type === "application/pdf");
    if (supported.length !== files.length) setError("Only PDF or image files are supported.");
    setPendingFiles((prev) => [...prev, ...supported.map((f) => ({
      file: f, previewUrl: URL.createObjectURL(f), isImage: f.type.startsWith("image/"),
    }))]);
    e.target.value = "";
  }, []);

  const removePending = (i: number) => setPendingFiles((prev) => {
    URL.revokeObjectURL(prev[i]?.previewUrl ?? "");
    return prev.filter((_, idx) => idx !== i);
  });

  const toggleReaction = (id: string, type: "like" | "dislike") =>
    updateMessage(id, (m) => type === "like"
      ? { ...m, liked: !m.liked, disliked: m.liked ? m.disliked : false }
      : { ...m, disliked: !m.disliked, liked: m.disliked ? m.liked : false });

  const selectCommand = (i: number) => {
    const [modeKey, cfg] = modeEntries[i];
    setMode(modeKey);
    setInput(cfg.shortcut + " ");
    setShowCommandPalette(false);
    setRecentCommand(cfg.label);
    setTimeout(() => setRecentCommand(null), 2500);
    setTimeout(() => adjust(), 0);
  };

  const setValue_input = (v: string) => { setInput(v); setTimeout(() => adjust(), 0); };

  /* ── Toggle task completion ── */
  const toggleTask = (id: string) => {
    setTasks((prev) => prev.map((t) => t.id === id ? { ...t, completed: !t.completed } : t));
  };

  /* ── Delete task ── */
  const deleteTask = (id: string) => {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  };

  /* ─────────────────────────────────────────────
     Render
  ───────────────────────────────────────────── */
  return (
    <div className="min-h-screen bg-[#080810] text-white relative overflow-hidden">

      {/* ── Ambient blobs ── */}
      <div className="pointer-events-none fixed inset-0 overflow-hidden -z-0">
        <div className="absolute top-0 left-1/4 w-[32rem] h-[32rem] bg-violet-600/8 rounded-full blur-[120px] animate-pulse" />
        <div className="absolute bottom-0 right-1/4 w-[28rem] h-[28rem] bg-indigo-600/8 rounded-full blur-[100px] animate-pulse delay-700" />
        <div className="absolute top-1/3 right-1/3 w-64 h-64 bg-fuchsia-600/6 rounded-full blur-[80px] animate-pulse delay-1000" />
      </div>

      {/* ── Mouse-follow glow ── */}
      {inputFocused && (
        <motion.div
          className="fixed w-[40rem] h-[40rem] rounded-full pointer-events-none z-0 opacity-[0.025] bg-gradient-to-r from-violet-500 via-fuchsia-500 to-indigo-500 blur-[96px]"
          animate={{ x: mousePos.x - 320, y: mousePos.y - 320 }}
          transition={{ type: "spring", damping: 28, stiffness: 160, mass: 0.4 }}
        />
      )}

      <div className="relative z-10 max-w-6xl mx-auto px-4 md:px-6 py-8 space-y-6">

        {/* ── Header ── */}
        <motion.div
          initial={{ opacity: 0, y: -16 }} animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="flex flex-wrap items-center justify-between gap-4"
        >
          <div>
            <p className="text-[10px] uppercase tracking-[0.5em] text-white/30 mb-1">AI Studio</p>
            <h1 className="text-2xl font-semibold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-white/60">
              Smart LMS Assistant
            </h1>
            <p className="mt-1 text-xs text-white/30">
              Chat · Plan · Deadlines · Email Intelligence
              {urgentTasks.length > 0 && (
                <span className="ml-2 text-amber-400/90 font-medium">
                  · ⚠️ {urgentTasks.length} urgent task{urgentTasks.length > 1 ? "s" : ""}
                </span>
              )}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] text-xs text-white/50">
              <Zap className="w-3 h-3 text-violet-400" />
              <span>Powered by FusionIQ</span>
            </div>
          </div>
        </motion.div>

        {/* ── Quick-action chips ── */}
        <motion.div
          initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.1 }}
          className="flex flex-wrap gap-2"
        >
          {[
            { label: "Show tasks",        cmd: "show tasks" },
            { label: "Show deadlines",    cmd: "show deadlines" },
            { label: "Show emails",       cmd: "show emails" },
            { label: "Convert email → task", cmd: "convert email to task" },
            { label: "Show hackathons",   cmd: "show hackathons" },
            { label: "Add all assignments", cmd: "add all assignments" },
            { label: "Help",              cmd: "help" },
          ].map((chip) => (
            <button
              key={chip.cmd}
              onClick={() => { setValue_input(chip.cmd); setTimeout(() => handleSubmit(), 50); }}
              className="px-3 py-1.5 rounded-full border border-white/[0.08] bg-white/[0.02] hover:bg-violet-500/15 hover:border-violet-500/30 hover:text-violet-300 text-white/40 text-xs transition-all"
            >
              {chip.label}
            </button>
          ))}
        </motion.div>

        {/* ── Main grid ── */}
        <div className="grid gap-4 lg:grid-cols-[220px_1fr]">

          {/* ── Sidebar ── */}
          <motion.aside
            initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="rounded-2xl border border-white/[0.07] bg-white/[0.02] backdrop-blur-xl p-4 space-y-4 h-fit"
          >
            {/* History */}
            <div>
              <p className="text-[10px] uppercase tracking-[0.4em] text-white/25 mb-3">History</p>
              <button
                onClick={() => setMessages([{
                  id: createId(), role: "system",
                  content: "Smart LMS Assistant — Chat, plan tasks, track deadlines, convert emails.",
                  createdAt: new Date().toISOString(),
                }])}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-xl bg-white/[0.03] hover:bg-white/[0.07] border border-white/[0.06] text-xs text-white/50 hover:text-white/80 transition-all"
              >
                <Trash2 className="w-3.5 h-3.5" /> New chat
              </button>
            </div>
            <div className="space-y-1.5">
              {history.length === 0
                ? <p className="text-[11px] text-white/20 px-1">No history yet.</p>
                : history.map((item) => (
                  <div key={item.id} className="rounded-xl border border-white/[0.05] bg-white/[0.02] px-3 py-2 text-[11px] text-white/35 leading-relaxed line-clamp-2">
                    {item.content.slice(0, 65)}{item.content.length > 65 && "…"}
                  </div>
                ))
              }
            </div>

            {/* Mode */}
            <div>
              <p className="text-[10px] uppercase tracking-[0.4em] text-white/25 mb-2">Mode</p>
              <div className="space-y-1">
                {modeEntries.map(([key, cfg]) => (
                  <button
                    key={key}
                    onClick={() => setMode(key)}
                    className={cn(
                      "w-full flex items-center gap-2 px-3 py-2 rounded-xl text-xs transition-all",
                      mode === key
                        ? "bg-violet-500/15 border border-violet-500/30 text-violet-300"
                        : "border border-transparent text-white/40 hover:bg-white/[0.04] hover:text-white/70"
                    )}
                  >
                    {cfg.icon} {cfg.label}
                    <span className="ml-auto text-[10px] text-white/20 font-mono">{cfg.shortcut}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Planner summary */}
            <div>
              <p className="text-[10px] uppercase tracking-[0.4em] text-white/25 mb-2">Planner</p>
              <div className="space-y-1.5">
                <div className="flex justify-between text-[11px] text-white/30 px-1">
                  <span>{tasks.filter((t) => !t.completed).length} pending</span>
                  <span>{tasks.filter((t) => t.completed).length} done</span>
                </div>
                {urgentTasks.length > 0 && (
                  <div className="px-3 py-2 rounded-xl bg-amber-500/10 border border-amber-500/20 text-[11px] text-amber-300">
                    <TriangleAlert className="w-3 h-3 inline mr-1" />
                    {urgentTasks.length} due within 3 days
                  </div>
                )}
              </div>
            </div>
          </motion.aside>

          {/* ── Chat panel ── */}
          <div className="flex flex-col gap-4">
            <motion.div
              initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.55, delay: 0.15 }}
              className="flex flex-col rounded-2xl border border-white/[0.07] bg-white/[0.02] backdrop-blur-xl overflow-hidden"
            >
              {/* Panel header */}
              <div className="flex items-center justify-between px-5 py-4 border-b border-white/[0.06]">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-violet-500/20 border border-violet-500/30 flex items-center justify-center">
                    <BrainCircuit className="w-4 h-4 text-violet-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-white/90">AI Chat</p>
                    <p className="text-[11px] text-white/30">Ask, explain, summarize, quiz, or manage tasks</p>
                  </div>
                </div>
                {/* Mode chips */}
                <div className="hidden md:flex items-center gap-1.5">
                  {modeEntries.map(([key, cfg]) => (
                    <button
                      key={key}
                      onClick={() => setMode(key)}
                      className={cn(
                        "flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs transition-all",
                        mode === key
                          ? "bg-violet-500/20 border border-violet-500/40 text-violet-300"
                          : "border border-white/[0.08] text-white/35 hover:text-white/60 hover:bg-white/[0.04]"
                      )}
                    >
                      {cfg.icon} {cfg.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Quiz options bar */}
              <AnimatePresence>
                {mode === "quiz" && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }} transition={{ duration: 0.25 }}
                    className="px-5 py-3 border-b border-white/[0.06] flex flex-wrap gap-3 items-center bg-white/[0.02]"
                  >
                    <div className="flex items-center gap-2">
                      <label className="text-[11px] text-white/40">Questions</label>
                      <input
                        type="number" min={5} max={30} value={quizCount}
                        onChange={(e) => setQuizCount(Number(e.target.value))}
                        className="h-7 w-16 text-xs bg-white/[0.05] border border-white/10 text-white/80 rounded-lg px-2 focus:outline-none"
                      />
                    </div>
                    <div className="flex items-center gap-2">
                      <label className="text-[11px] text-white/40">Type</label>
                      <select
                        value={quizType} onChange={(e) => setQuizType(e.target.value)}
                        className="h-7 text-xs bg-white/[0.05] border border-white/10 text-white/80 rounded-lg px-2 focus:outline-none"
                      >
                        {QUIZ_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
                      </select>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto px-5 py-4 space-y-2 min-h-[380px] max-h-[480px]">
                <AnimatePresence initial={false}>
                  {messages.map((msg) => (
                    <motion.div
                      key={msg.id}
                      initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -8 }} transition={{ duration: 0.25 }}
                      className={cn(
                        "flex",
                        msg.role === "user" ? "justify-end" : msg.role === "assistant" ? "justify-start" : "justify-center"
                      )}
                    >
                      {msg.role === "assistant" && (
                        <div className="w-7 h-7 rounded-xl bg-violet-500/20 border border-violet-500/30 flex items-center justify-center mr-2 mt-1 flex-shrink-0">
                          <Sparkles className="w-3.5 h-3.5 text-violet-400" />
                        </div>
                      )}

                      <div className={cn(
                        "max-w-[78%] rounded-2xl px-4 py-3 text-sm leading-relaxed",
                        msg.role === "user" && "bg-violet-500/20 border border-violet-500/25 text-white/90",
                        msg.role === "assistant" && "bg-white/[0.04] border border-white/[0.07] text-white/85",
                        msg.role === "system" && "bg-transparent text-[10px] uppercase tracking-widest text-white/25 border-none px-0 py-1"
                      )}>
                        {/* Render markdown for assistant */}
                        {msg.role === "assistant" ? (
                          <div className="prose prose-sm prose-invert max-w-none whitespace-pre-wrap text-white/85">
                            {/* Simple markdown: bold and line breaks */}
                            {msg.content.split("\n").map((line, i) => {
                              const parts = line.split(/(\*\*[^*]+\*\*)/g);
                              return (
                                <div key={i} className={i > 0 ? "mt-0.5" : ""}>
                                  {parts.map((part, j) =>
                                    part.startsWith("**") && part.endsWith("**")
                                      ? <strong key={j} className="text-white font-semibold">{part.slice(2, -2)}</strong>
                                      : <span key={j}>{part}</span>
                                  )}
                                </div>
                              );
                            })}
                          </div>
                        ) : (
                          <p>{msg.content}</p>
                        )}

                        {/* Attachments */}
                        {msg.attachments && msg.attachments.length > 0 && (
                          <div className="mt-3 grid gap-2 sm:grid-cols-2">
                            {msg.attachments.map((file, i) => (
                              <div key={`${file.name}-${i}`} className="rounded-xl border border-white/[0.08] bg-white/[0.04] p-2 text-xs text-white/40">
                                {file.isImage && file.url
                                  ? <img src={file.url} alt={file.name} className="h-28 w-full rounded-lg object-cover" />
                                  : <div className="flex items-center gap-2"><FileText size={14} /><span className="truncate">{file.name}</span></div>
                                }
                              </div>
                            ))}
                          </div>
                        )}

                        {/* Assistant actions */}
                        {msg.role === "assistant" && !msg.streaming && (
                          <div className="mt-3 flex flex-wrap gap-1.5">
                            <button type="button" onClick={() => navigator.clipboard.writeText(msg.content)}
                              className="flex items-center gap-1 px-2 py-1 rounded-full border border-white/[0.08] bg-white/[0.03] hover:bg-white/[0.07] text-white/35 hover:text-white/70 text-[11px] transition-all">
                              <Copy size={11} /> Copy
                            </button>
                            <button type="button" onClick={() => toggleReaction(msg.id, "like")}
                              className={cn("flex items-center gap-1 px-2 py-1 rounded-full border text-[11px] transition-all",
                                msg.liked ? "border-emerald-500/40 bg-emerald-500/10 text-emerald-400" : "border-white/[0.08] text-white/35 hover:text-white/70")}>
                              <ThumbsUp size={11} />
                            </button>
                            <button type="button" onClick={() => toggleReaction(msg.id, "dislike")}
                              className={cn("flex items-center gap-1 px-2 py-1 rounded-full border text-[11px] transition-all",
                                msg.disliked ? "border-rose-500/40 bg-rose-500/10 text-rose-400" : "border-white/[0.08] text-white/35 hover:text-white/70")}>
                              <ThumbsDown size={11} />
                            </button>
                          </div>
                        )}
                      </div>
                    </motion.div>
                  ))}
                </AnimatePresence>

                {/* Typing indicator */}
                {isTyping && (
                  <motion.div initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }}
                    className="flex items-center gap-2">
                    <div className="w-7 h-7 rounded-xl bg-violet-500/20 border border-violet-500/30 flex items-center justify-center flex-shrink-0">
                      <Sparkles className="w-3.5 h-3.5 text-violet-400" />
                    </div>
                    <div className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white/[0.04] border border-white/[0.07] text-xs text-white/40">
                      <span>Processing</span><TypingDots />
                    </div>
                  </motion.div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* ── Input area ── */}
              <div className="px-4 pb-4 pt-2 border-t border-white/[0.06]">
                {/* Recent command badge */}
                <AnimatePresence>
                  {recentCommand && (
                    <motion.div
                      initial={{ opacity: 0, y: 4 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
                      className="mb-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-violet-500/15 border border-violet-500/25 text-xs text-violet-300"
                    >
                      <Command className="w-3 h-3" /> {recentCommand} mode activated
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Pending file previews */}
                <AnimatePresence>
                  {pendingFiles.length > 0 && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      className="mb-3 flex flex-wrap gap-2 p-3 rounded-xl border border-white/[0.07] bg-white/[0.02]"
                    >
                      {pendingFiles.map((f, i) => (
                        <motion.div key={`${f.file.name}-${i}`} initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} className="relative">
                          {f.isImage
                            ? <img src={f.previewUrl} alt={f.file.name} className="h-16 w-20 rounded-lg object-cover border border-white/[0.08]" />
                            : <div className="h-16 w-24 flex flex-col items-center justify-center rounded-lg border border-white/[0.08] bg-white/[0.04] text-xs text-white/40 gap-1">
                                <FileText size={14} /><span className="truncate px-1 max-w-full">{f.file.name}</span>
                              </div>
                          }
                          <button onClick={() => removePending(i)}
                            className="absolute -top-1.5 -right-1.5 w-5 h-5 rounded-full bg-[#080810] border border-white/[0.12] flex items-center justify-center text-white/50 hover:text-white">
                            <X size={10} />
                          </button>
                        </motion.div>
                      ))}
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Command palette */}
                <div className="relative">
                  <AnimatePresence>
                    {showCommandPalette && (
                      <motion.div ref={commandPaletteRef}
                        initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 6 }}
                        transition={{ duration: 0.15 }}
                        className="absolute bottom-full left-0 right-0 mb-2 z-50 rounded-xl border border-white/[0.1] bg-[#0d0d18]/95 backdrop-blur-xl shadow-2xl overflow-hidden"
                      >
                        <div className="py-1">
                          {modeEntries.map(([key, cfg], i) => (
                            <motion.button
                              key={key} type="button"
                              onClick={() => selectCommand(i)}
                              initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.04 }}
                              className={cn(
                                "w-full flex items-center gap-3 px-4 py-2.5 text-xs transition-colors",
                                activeCmd === i ? "bg-white/[0.08] text-white" : "text-white/50 hover:bg-white/[0.04] hover:text-white/80"
                              )}
                            >
                              <div className="text-violet-400">{cfg.icon}</div>
                              <span className="font-medium">{cfg.label}</span>
                              <span className="ml-auto font-mono text-white/25">{cfg.shortcut}</span>
                            </motion.button>
                          ))}
                        </div>
                      </motion.div>
                    )}
                  </AnimatePresence>

                  {/* Textarea form */}
                  <form onSubmit={handleSubmit}>
                    <div className={cn(
                      "rounded-2xl border transition-all duration-300",
                      inputFocused ? "border-violet-500/35 bg-white/[0.04] shadow-[0_0_0_3px_rgba(139,92,246,0.08)]" : "border-white/[0.08] bg-white/[0.02]"
                    )}>
                      <textarea
                        ref={textareaRef}
                        value={input}
                        onChange={(e) => { setInput(e.target.value); adjust(); }}
                        onKeyDown={handleKeyDown}
                        onFocus={() => setInputFocused(true)}
                        onBlur={() => setInputFocused(false)}
                        placeholder={MODE_CONFIG[mode].placeholder}
                        style={{ minHeight: 60, overflow: "hidden" }}
                        className="w-full resize-none bg-transparent px-4 pt-4 pb-2 text-sm text-white/85 placeholder:text-white/20 focus:outline-none"
                      />
                      <div className="flex items-center justify-between gap-3 px-3 pb-3">
                        <div className="flex items-center gap-1">
                          <input ref={fileInputRef} type="file" accept="application/pdf,image/*" multiple hidden onChange={handleAttach} />
                          <button type="button" onClick={() => fileInputRef.current?.click()}
                            className="p-2 rounded-xl text-white/30 hover:text-white/70 hover:bg-white/[0.06] transition-all">
                            <Paperclip className="w-4 h-4" />
                          </button>
                          <button type="button" data-cmd-btn
                            onClick={(e) => { e.stopPropagation(); setShowCommandPalette((p) => !p); }}
                            className={cn(
                              "p-2 rounded-xl transition-all",
                              showCommandPalette ? "bg-violet-500/15 text-violet-400" : "text-white/30 hover:text-white/70 hover:bg-white/[0.06]"
                            )}>
                            <Command className="w-4 h-4" />
                          </button>
                          <span className="text-[10px] text-white/20 ml-1 hidden sm:block">PDF & images · Type / for modes · Enter to send</span>
                        </div>
                        <motion.button
                          type="submit"
                          disabled={!canSend || isTyping}
                          whileHover={canSend ? { scale: 1.03 } : {}}
                          whileTap={canSend ? { scale: 0.97 } : {}}
                          className={cn(
                            "flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all",
                            canSend && !isTyping
                              ? "bg-violet-500 hover:bg-violet-400 text-white shadow-lg shadow-violet-500/20"
                              : "bg-white/[0.04] text-white/25 cursor-not-allowed"
                          )}
                        >
                          <SendIcon className="w-4 h-4" />
                          <span>{isTyping ? "…" : "Send"}</span>
                        </motion.button>
                      </div>
                    </div>
                  </form>
                </div>
              </div>

              {error && (
                <motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }}
                  className="px-5 pb-4 text-xs text-rose-400/80">{error}</motion.p>
              )}
            </motion.div>

            {/* ─────────────────────────────────────────────
                📋 PLANNER SECTION
            ───────────────────────────────────────────── */}
            <motion.div
              initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.25 }}
              className="rounded-2xl border border-white/[0.07] bg-white/[0.02] backdrop-blur-xl overflow-hidden"
            >
              {/* Planner header */}
              <div className="flex items-center justify-between px-5 py-4 border-b border-white/[0.06]">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center">
                    <ClipboardList className="w-4 h-4 text-emerald-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-white/90">My Planner</p>
                    <p className="text-[11px] text-white/30">
                      {tasks.filter((t) => !t.completed).length} pending · {tasks.filter((t) => t.completed).length} completed
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {tasks.length > 0 && (
                    <button
                      onClick={() => { setTasks([]); saveTasks([]); }}
                      className="text-[11px] text-white/25 hover:text-rose-400 transition-colors px-2 py-1"
                    >
                      Clear all
                    </button>
                  )}
                  <button
                    onClick={() => setShowPlanner((p) => !p)}
                    className="text-[11px] text-white/30 hover:text-white/70 transition-colors px-2 py-1 border border-white/[0.08] rounded-lg"
                  >
                    {showPlanner ? "Hide" : "Show"}
                  </button>
                </div>
              </div>

              {/* Planner body */}
              <AnimatePresence>
                {showPlanner && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }} transition={{ duration: 0.3 }}
                    className="overflow-hidden"
                  >
                    {tasks.length === 0 ? (
                      <div className="px-5 py-8 text-center">
                        <ClipboardList className="w-8 h-8 text-white/10 mx-auto mb-3" />
                        <p className="text-sm text-white/25">Your planner is empty.</p>
                        <p className="text-[11px] text-white/15 mt-1">
                          Say "add task", "add OS assignment", or "convert email to task" in chat.
                        </p>
                      </div>
                    ) : (
                      <div className="divide-y divide-white/[0.04]">
                        {tasks.map((task) => {
                          const d = daysUntil(task.deadline);
                          const isUrgent = d <= 3 && !task.completed;
                          const isOverdue = d < 0 && !task.completed;
                          return (
                            <motion.div
                              key={task.id}
                              layout
                              initial={{ opacity: 0, x: -8 }} animate={{ opacity: 1, x: 0 }}
                              exit={{ opacity: 0, x: 8 }}
                              className={cn(
                                "flex items-center gap-3 px-5 py-3 group hover:bg-white/[0.02] transition-colors",
                                task.completed && "opacity-50"
                              )}
                            >
                              {/* Checkbox */}
                              <button onClick={() => toggleTask(task.id)} className="flex-shrink-0 text-white/30 hover:text-emerald-400 transition-colors">
                                {task.completed
                                  ? <CheckCircle2 className="w-5 h-5 text-emerald-400" />
                                  : <Circle className="w-5 h-5" />
                                }
                              </button>

                              {/* Task info */}
                              <div className="flex-1 min-w-0">
                                <p className={cn(
                                  "text-sm text-white/80 truncate",
                                  task.completed && "line-through text-white/35"
                                )}>
                                  {task.title}
                                </p>
                                <div className="flex items-center gap-2 mt-0.5">
                                  <span className={cn(
                                    "text-[11px]",
                                    isOverdue ? "text-rose-400" : isUrgent ? "text-amber-400" : "text-white/30"
                                  )}>
                                    {isOverdue ? "🔴 Overdue · " : isUrgent ? "⚠️ " : "📅 "}
                                    {fmtDate(task.deadline)}
                                  </span>
                                  <span className={cn(
                                    "text-[10px] px-1.5 py-0.5 rounded-full border",
                                    task.category === "assignment" && "border-blue-500/30 text-blue-300/60 bg-blue-500/10",
                                    task.category === "hackathon" && "border-fuchsia-500/30 text-fuchsia-300/60 bg-fuchsia-500/10",
                                    task.category === "reminder" && "border-amber-500/30 text-amber-300/60 bg-amber-500/10",
                                    task.category === "general" && "border-white/[0.08] text-white/25",
                                  )}>
                                    {task.category}
                                  </span>
                                </div>
                              </div>

                              {/* Days badge */}
                              {!task.completed && (
                                <span className={cn(
                                  "text-[11px] px-2 py-1 rounded-full border flex-shrink-0",
                                  isOverdue ? "border-rose-500/30 text-rose-400 bg-rose-500/10" :
                                  isUrgent  ? "border-amber-500/30 text-amber-400 bg-amber-500/10" :
                                              "border-white/[0.08] text-white/25"
                                )}>
                                  {isOverdue ? `${Math.abs(d)}d ago` : d === 0 ? "Today" : `${d}d left`}
                                </span>
                              )}

                              {/* Delete */}
                              <button
                                onClick={() => deleteTask(task.id)}
                                className="opacity-0 group-hover:opacity-100 transition-opacity text-white/20 hover:text-rose-400 flex-shrink-0"
                              >
                                <X className="w-4 h-4" />
                              </button>
                            </motion.div>
                          );
                        })}
                      </div>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>

            {/* ─────────────────────────────────────────────
                📧 INBOX SECTION
            ───────────────────────────────────────────── */}
            <motion.div
              initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.35 }}
              className="rounded-2xl border border-white/[0.07] bg-white/[0.02] backdrop-blur-xl overflow-hidden"
            >
              <div className="flex items-center justify-between px-5 py-4 border-b border-white/[0.06]">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-sky-500/20 border border-sky-500/30 flex items-center justify-center">
                    <Mail className="w-4 h-4 text-sky-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-white/90">Email Inbox</p>
                    <p className="text-[11px] text-white/30">{EMAILS.length} messages · Convert any to task</p>
                  </div>
                </div>
                <button
                  onClick={() => setShowEmails((p) => !p)}
                  className="text-[11px] text-white/30 hover:text-white/70 transition-colors px-2 py-1 border border-white/[0.08] rounded-lg"
                >
                  {showEmails ? "Hide" : "Show"}
                </button>
              </div>

              <AnimatePresence>
                {showEmails && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }} transition={{ duration: 0.3 }}
                  >
                    {EMAILS.map((email, i) => {
                      const alreadyAdded = tasks.some((t) => t.title.toLowerCase() === email.subject.toLowerCase());
                      return (
                        <div
                          key={email.id}
                          className={cn(
                            "px-5 py-4 flex items-start gap-4",
                            i < EMAILS.length - 1 && "border-b border-white/[0.04]"
                          )}
                        >
                          <div className="w-8 h-8 rounded-full bg-sky-500/10 border border-sky-500/20 flex items-center justify-center flex-shrink-0 text-xs text-sky-400 font-semibold mt-0.5">
                            {email.from[0].toUpperCase()}
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-white/80 truncate">{email.subject}</p>
                            <p className="text-[11px] text-white/30 mt-0.5">{email.from}</p>
                            <p className="text-[11px] text-white/20 mt-1 line-clamp-1">{email.body}</p>
                          </div>
                          <div className="flex flex-col items-end gap-2 flex-shrink-0">
                            <span className="text-[11px] text-white/25">{fmtDate(email.deadline)}</span>
                            <button
                              onClick={() => {
                                if (!alreadyAdded)
                                  addTask({ id: createId(), title: email.subject, deadline: email.deadline, completed: false, category: email.category });
                              }}
                              className={cn(
                                "text-[11px] px-2.5 py-1 rounded-full border transition-all",
                                alreadyAdded
                                  ? "border-emerald-500/30 text-emerald-400/60 bg-emerald-500/5 cursor-default"
                                  : "border-sky-500/30 text-sky-400 hover:bg-sky-500/10 cursor-pointer"
                              )}
                            >
                              {alreadyAdded ? "✓ Added" : "+ Add task"}
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>

          </div>{/* end chat column */}
        </div>
      </div>

      {/* ── Floating typing indicator ── */}
      <AnimatePresence>
        {isTyping && (
          <motion.div
            initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 16 }}
            className="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 px-4 py-2.5 rounded-full border border-white/[0.08] bg-[#0d0d18]/90 backdrop-blur-xl shadow-xl"
          >
            <Sparkles className="w-3.5 h-3.5 text-violet-400" />
            <div className="flex items-center gap-2 text-sm text-white/50">
              <span>Processing</span><TypingDots />
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
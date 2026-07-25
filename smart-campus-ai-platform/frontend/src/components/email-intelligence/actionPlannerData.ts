export type SmartCategory = "deadline" | "task" | "hackathon" | "opportunity";
export type SmartPriority = "HIGH" | "MEDIUM" | "LOW";

export interface SmartEmail {
  id: string;
  subject: string;
  sender: string;
  summary: string;
  category: SmartCategory;
  priority: SmartPriority;
  deadline?: string | null;
  actionRequired: boolean;
  createdAt: string;
}

export interface PlannerTaskItem {
  id: string;
  emailId: string;
  title: string;
  deadline?: string | null;
  category: SmartCategory;
  completed: boolean;
  notes: string;
  createdAt: string;
}

interface EmailInsight {
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
}

const toIsoDate = (daysFromNow: number) => {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().slice(0, 10);
};

const toDisplayStamp = (daysFromNow: number, hour: number, minute: number) => {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  date.setHours(hour, minute, 0, 0);
  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit"
  });
};

export const DEFAULT_EMAILS: SmartEmail[] = [
  {
    id: "email-os-assignment",
    subject: "Operating Systems Assignment Submission",
    sender: "saranya@college.edu",
    summary: "Final report on CPU scheduling analysis is due this week. Submit the PDF and performance comparison table before the lab review.",
    category: "deadline",
    priority: "HIGH",
    deadline: toIsoDate(2),
    actionRequired: true,
    createdAt: toDisplayStamp(0, 9, 15)
  },
  {
    id: "email-ml-hackathon",
    subject: "Machine Learning Hackathon Registration",
    sender: "events@unstop.com",
    summary: "Registrations are open for the city-level ML hackathon. Team shortlist closes soon, so confirm participation and upload your abstract.",
    category: "hackathon",
    priority: "MEDIUM",
    deadline: toIsoDate(5),
    actionRequired: true,
    createdAt: toDisplayStamp(-1, 18, 40)
  },
  {
    id: "email-data-analytics-workshop",
    subject: "Data Analytics Workshop Opportunity",
    sender: "programs@internshala.com",
    summary: "A weekend workshop on data storytelling and dashboard design has opened. Seats are limited but there is no urgent action required today.",
    category: "opportunity",
    priority: "LOW",
    deadline: toIsoDate(8),
    actionRequired: false,
    createdAt: toDisplayStamp(-1, 11, 30)
  },
  {
    id: "email-daa-lab-task",
    subject: "DAA Lab Task: Graph Algorithm Demonstration",
    sender: "priya.faculty@college.edu",
    summary: "Prepare the BFS and Dijkstra demonstration workbook for tomorrow's lab session and upload screenshots of the final output.",
    category: "task",
    priority: "HIGH",
    deadline: toIsoDate(1),
    actionRequired: true,
    createdAt: toDisplayStamp(0, 7, 45)
  },
  {
    id: "email-placement-drive",
    subject: "Analytics Internship Shortlist Opportunity",
    sender: "placements@college.edu",
    summary: "Students with strong SQL and dashboard skills can apply for the upcoming analytics internship drive. Review the eligibility sheet and shortlist requirements.",
    category: "opportunity",
    priority: "MEDIUM",
    deadline: toIsoDate(6),
    actionRequired: false,
    createdAt: toDisplayStamp(-2, 16, 20)
  },
  {
    id: "email-ml-quiz-deadline",
    subject: "Machine Learning Quiz Window Closing",
    sender: "noreply@lms.edu",
    summary: "The supervised learning quiz closes in three days. Attempt it before the portal locks and make sure your internet connection is stable.",
    category: "deadline",
    priority: "HIGH",
    deadline: toIsoDate(3),
    actionRequired: true,
    createdAt: toDisplayStamp(0, 8, 5)
  }
];

export const CATEGORY_META: Record<SmartCategory, { label: string; pillClass: string; dot: string }> = {
  deadline: {
    label: "Deadline",
    pillClass: "bg-rose-500/15 text-rose-700 dark:text-rose-200",
    dot: "\uD83D\uDD25"
  },
  task: {
    label: "Task",
    pillClass: "bg-sky-500/15 text-sky-700 dark:text-sky-200",
    dot: "\uD83D\uDCCC"
  },
  hackathon: {
    label: "Hackathon",
    pillClass: "bg-violet-500/15 text-violet-700 dark:text-violet-200",
    dot: "\uD83D\uDE80"
  },
  opportunity: {
    label: "Opportunity",
    pillClass: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-200",
    dot: "\uD83C\uDF31"
  }
};

export const PRIORITY_META: Record<SmartPriority, string> = {
  HIGH: "bg-rose-500/15 text-rose-700 dark:text-rose-200",
  MEDIUM: "bg-amber-500/15 text-amber-700 dark:text-amber-200",
  LOW: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-200"
};

export const normalizeCategory = (category: string): SmartCategory => {
  const value = category.trim().toLowerCase();
  if (value.includes("hack")) return "hackathon";
  if (value.includes("dead")) return "deadline";
  if (value.includes("task") || value.includes("assignment")) return "task";
  return "opportunity";
};

export const normalizePriority = (priority: string): SmartPriority => {
  const value = priority.trim().toUpperCase();
  if (value === "HIGH" || value === "LOW") return value;
  return "MEDIUM";
};

export const mapInsightToSmartEmail = (item: EmailInsight): SmartEmail => ({
  id: `insight-${item.id}`,
  subject: item.subject,
  sender: item.sender,
  summary: item.summary,
  category: normalizeCategory(item.category),
  priority: normalizePriority(item.priority),
  deadline: item.deadline ?? null,
  actionRequired: item.actionRequired,
  createdAt: item.createdAt
});

export const isUpcomingDeadline = (deadline?: string | null) => {
  if (!deadline) return false;
  const now = new Date();
  const due = new Date(deadline);
  if (Number.isNaN(due.getTime())) return false;
  const diffDays = Math.ceil((due.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  return diffDays >= 0 && diffDays <= 3;
};

export const getDaysUntil = (deadline?: string | null) => {
  if (!deadline) return null;
  const now = new Date();
  const due = new Date(deadline);
  if (Number.isNaN(due.getTime())) return null;
  return Math.ceil((due.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
};

import { useEffect, useMemo, useRef, useState } from "react";
import { AlertCircle, Bell, CalendarClock, Library, Mail, Search, Trophy } from "lucide-react";
import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { API_BASE_URL, deleteJson, getJson, patchJson } from "@/lib/api";

type EmailCategory =
  | "All"
  | "Official"
  | "Unofficial"
  | "Deadlines"
  | "Competitions"
  | "Hackathons"
  | "Library";

type EmailItem = {
  id: string;
  subject: string;
  sender: string;
  content: string;
  date: string;
  category?: EmailCategory;
  suggestedCategory?: EmailCategory;
  important?: boolean;
};

const CATEGORY_CONFIG: Record<Exclude<EmailCategory, "All">, { label: string; color: string; icon: typeof Mail }> = {
  Official: { label: "Official", color: "bg-blue-500/15 text-blue-700 dark:text-blue-200", icon: Bell },
  Unofficial: { label: "Unofficial", color: "bg-slate-500/15 text-slate-700 dark:text-slate-200", icon: Mail },
  Deadlines: { label: "Deadlines", color: "bg-rose-500/15 text-rose-700 dark:text-rose-200", icon: CalendarClock },
  Competitions: { label: "Competitions", color: "bg-orange-500/15 text-orange-700 dark:text-orange-200", icon: Trophy },
  Hackathons: { label: "Hackathons", color: "bg-purple-500/15 text-purple-700 dark:text-purple-200", icon: AlertCircle },
  Library: { label: "Library", color: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-200", icon: Library }
};

const CATEGORY_ORDER: EmailCategory[] = [
  "All",
  "Official",
  "Deadlines",
  "Hackathons",
  "Competitions",
  "Library",
  "Unofficial"
];

const KEYWORD_RULES: Array<{ category: Exclude<EmailCategory, "All">; keywords: string[] }> = [
  { category: "Deadlines", keywords: ["deadline", "last date", "due", "submit", "submission"] },
  { category: "Hackathons", keywords: ["hackathon", "coding event", "codefest", "buildathon"] },
  { category: "Competitions", keywords: ["competition", "contest", "challenge", "tournament"] },
  { category: "Library", keywords: ["library", "book", "return", "due date", "renewal"] },
  { category: "Official", keywords: ["circular", "notice", "meeting", "official", "announcement"] }
];

const classifyEmail = (email: EmailItem): Exclude<EmailCategory, "All"> => {
  const haystack = `${email.subject} ${email.content}`.toLowerCase();
  for (const rule of KEYWORD_RULES) {
    if (rule.keywords.some((keyword) => haystack.includes(keyword))) {
      return rule.category;
    }
  }
  return "Unofficial";
};

const normalizeEmails = (emails: EmailItem[]): EmailItem[] =>
  emails.map((email) => {
    const category = email.category && email.category !== "All" ? email.category : classifyEmail(email);
    const important = email.important ?? (category === "Deadlines" || category === "Official");
    return { ...email, category, important };
  });

type ApiEmail = {
  id: string;
  subject: string;
  sender: string;
  content: string;
  date: string;
  category?: string;
  important?: boolean;
};

const toUiCategory = (value: string | undefined): EmailCategory | undefined => {
  if (!value) return undefined;
  switch (value.toUpperCase()) {
    case "OFFICIAL":
      return "Official";
    case "DEADLINES":
      return "Deadlines";
    case "HACKATHONS":
      return "Hackathons";
    case "COMPETITIONS":
      return "Competitions";
    case "LIBRARY":
      return "Library";
    case "UNOFFICIAL":
    case "GENERAL":
      return "Unofficial";
    default:
      return undefined;
  }
};

const mapApiEmail = (email: ApiEmail): EmailItem => {
  const category = toUiCategory(email.category);
  const suggested = classifyEmail({
    id: email.id,
    subject: email.subject,
    sender: email.sender,
    content: email.content,
    date: email.date
  });
  return {
    id: email.id,
    subject: email.subject,
    sender: email.sender,
    content: email.content,
    date: email.date,
    category,
    suggestedCategory: suggested,
    important: email.important
  };
};

export default function Inbox() {
  const [activeCategory, setActiveCategory] = useState<EmailCategory>("All");
  const [query, setQuery] = useState("");
  const [emails, setEmails] = useState<EmailItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [needsGmailLink, setNeedsGmailLink] = useState(false);
  const [gmailConnected, setGmailConnected] = useState(false);
  const [newCount, setNewCount] = useState(0);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [bulkCategory, setBulkCategory] = useState<Exclude<EmailCategory, "All">>("Official");
  const [bulkImportant, setBulkImportant] = useState<boolean | null>(null);
  const [bulkLoading, setBulkLoading] = useState(false);
  const latestIds = useRef<Set<string>>(new Set());

  const filteredEmails = useMemo(() => {
    const term = query.trim().toLowerCase();
    return emails.filter((email) => {
      const matchesCategory = activeCategory === "All" || email.category === activeCategory;
      const matchesQuery = !term
        || email.subject.toLowerCase().includes(term)
        || email.sender.toLowerCase().includes(term)
        || email.content.toLowerCase().includes(term);
      return matchesCategory && matchesQuery;
    });
  }, [activeCategory, emails, query]);

  const loadEmails = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getJson<ApiEmail[]>("/api/emails");
      const mapped = normalizeEmails(data.map(mapApiEmail));
      setEmails(mapped);
      latestIds.current = new Set(mapped.map((item) => item.id));
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load inbox.";
      setError(message);
      if (message.includes("GMAIL_NOT_LINKED")) {
        setNeedsGmailLink(true);
        setGmailConnected(false);
      }
    } finally {
      setLoading(false);
    }
  };

  const pollEmails = async () => {
    try {
      const data = await getJson<ApiEmail[]>("/api/emails");
      const mapped = normalizeEmails(data.map(mapApiEmail));
      const incomingIds = mapped.map((item) => item.id);
      const unseen = incomingIds.filter((id) => !latestIds.current.has(id));
      if (unseen.length > 0) {
        setNewCount(unseen.length);
      }
      setEmails(mapped);
      latestIds.current = new Set(incomingIds);
    } catch {
      // silent background errors
    }
  };

  useEffect(() => {
    let timer: number | undefined;
    const init = async () => {
      try {
        const status = await getJson<{ linked: boolean }>("/api/gmail/status");
        setNeedsGmailLink(!status.linked);
        setGmailConnected(status.linked);
        await loadEmails();
        if (status.linked) {
          timer = window.setInterval(() => pollEmails(), 15000);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to check Gmail status.");
        setLoading(false);
      }
    };
    init();
    return () => {
      if (timer) {
        window.clearInterval(timer);
      }
    };
  }, []);

  const handleCategoryChange = async (id: string, category: Exclude<EmailCategory, "All">) => {
    try {
      await patchJson<ApiEmail>(`/api/emails/${id}`, {
        category: category.toUpperCase()
      });
      setEmails((prev) =>
        prev.map((email) => (email.id === id ? { ...email, category } : email))
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update category.");
    }
  };

  const handleImportantToggle = async (id: string, important: boolean) => {
    try {
      await patchJson<ApiEmail>(`/api/emails/${id}`, {
        important
      });
      setEmails((prev) =>
        prev.map((email) => (email.id === id ? { ...email, important } : email))
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update email.");
    }
  };

  const toggleSelect = (id: string) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((value) => value !== id) : [...prev, id]));
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredEmails.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredEmails.map((email) => email.id));
    }
  };

  const applyBulkAction = async () => {
    if (selectedIds.length === 0) return;
    setBulkLoading(true);
    setError(null);
    try {
      await patchJson<ApiEmail[]>("/api/emails/bulk", {
        ids: selectedIds,
        category: bulkCategory.toUpperCase(),
        important: bulkImportant
      });
      setEmails((prev) =>
        prev.map((email) => {
          if (!selectedIds.includes(email.id)) return email;
          return {
            ...email,
            category: bulkCategory,
            important: bulkImportant === null ? email.important : bulkImportant
          };
        })
      );
      setSelectedIds([]);
      setBulkImportant(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Bulk update failed.");
    } finally {
      setBulkLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <Card className="border border-border/60 bg-white/80 p-6 shadow-sm backdrop-blur dark:bg-white/10">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Inbox</p>
            <h2 className="text-2xl font-semibold text-foreground">Smart Campus Mail</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Automatically categorized messages with manual override.
            </p>
          </div>
          <div className="relative w-full max-w-xs">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search emails..."
              className="h-10 w-full rounded-xl border border-border/60 bg-white/70 pl-10 pr-3 text-sm text-foreground shadow-sm transition focus:outline-none dark:bg-white/10"
            />
          </div>
        </div>
      </Card>

      {error && (
        <Card className="border border-rose-200/60 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/10 dark:text-rose-200">
          {error}
        </Card>
      )}

      {needsGmailLink && (
        <Card className="border border-border/60 bg-white/80 px-4 py-3 text-sm text-muted-foreground dark:bg-white/10">
          Gmail is not connected.{" "}
          <button
            type="button"
            onClick={async () => {
              const token = localStorage.getItem("authToken") || localStorage.getItem("token");
              const url = token
                ? `${API_BASE_URL}/api/gmail/connect?token=${encodeURIComponent(token)}`
                : `${API_BASE_URL}/api/gmail/connect`;
              window.location.href = url;
            }}
            className="font-semibold text-primary underline underline-offset-2"
          >
            Connect Gmail
          </button>
        </Card>
      )}

      {gmailConnected && (
        <Card className="border border-border/60 bg-white/80 px-4 py-3 text-sm text-muted-foreground dark:bg-white/10">
          Gmail is connected.{" "}
          <button
            type="button"
            onClick={async () => {
              if (!window.confirm("Disconnect Gmail for this account?")) return;
              try {
                await deleteJson("/api/gmail/disconnect");
                setGmailConnected(false);
                setNeedsGmailLink(true);
                setEmails([]);
              } catch (err) {
                setError(err instanceof Error ? err.message : "Failed to disconnect Gmail.");
              }
            }}
            className="font-semibold text-rose-600 underline underline-offset-2"
          >
            Disconnect Gmail
          </button>
        </Card>
      )}

      {newCount > 0 && (
        <Card className="border border-emerald-200/60 bg-emerald-50 px-4 py-3 text-sm text-emerald-700 dark:border-emerald-400/30 dark:bg-emerald-500/10 dark:text-emerald-200">
          {newCount} new email{newCount > 1 ? "s" : ""} arrived.{" "}
          <button
            type="button"
            onClick={() => {
              setNewCount(0);
              pollEmails();
            }}
            className="font-semibold underline underline-offset-2"
          >
            Refresh
          </button>
        </Card>
      )}

      <div className="flex flex-wrap gap-2">
        {CATEGORY_ORDER.map((category) => {
          const config = category !== "All" ? CATEGORY_CONFIG[category] : null;
          const Icon = config?.icon ?? Mail;
          return (
            <button
              key={category}
              type="button"
              onClick={() => setActiveCategory(category)}
              className={cn(
                "flex items-center gap-2 rounded-full border px-4 py-2 text-xs font-semibold transition",
                activeCategory === category
                  ? "border-primary bg-primary/10 text-primary"
                  : "border-border/60 bg-white/70 text-muted-foreground hover:text-foreground dark:bg-white/10"
              )}
            >
              <Icon className="h-4 w-4" />
              {category}
            </button>
          );
        })}
      </div>

      <Card className="border border-border/60 bg-white/70 p-4 shadow-sm dark:bg-white/10">
        <div className="flex flex-wrap items-center gap-3">
          <input
            type="checkbox"
            checked={selectedIds.length > 0 && selectedIds.length === filteredEmails.length}
            onChange={toggleSelectAll}
            className="h-4 w-4 rounded border-border"
          />
          <span className="text-sm text-muted-foreground">{selectedIds.length} selected</span>
          <select
            value={bulkCategory}
            onChange={(event) => setBulkCategory(event.target.value as Exclude<EmailCategory, "All">)}
            className="h-9 rounded-lg border border-border/60 bg-white/80 px-3 text-xs text-foreground shadow-sm dark:bg-white/10"
          >
            {Object.keys(CATEGORY_CONFIG).map((key) => (
              <option key={key} value={key}>
                Set category: {key}
              </option>
            ))}
          </select>
          <select
            value={bulkImportant === null ? "auto" : bulkImportant ? "important" : "normal"}
            onChange={(event) => {
              const value = event.target.value;
              setBulkImportant(value === "auto" ? null : value === "important");
            }}
            className="h-9 rounded-lg border border-border/60 bg-white/80 px-3 text-xs text-foreground shadow-sm dark:bg-white/10"
          >
            <option value="auto">Keep importance</option>
            <option value="important">Mark Important</option>
            <option value="normal">Remove Important</option>
          </select>
          <button
            type="button"
            onClick={applyBulkAction}
            disabled={bulkLoading || selectedIds.length === 0}
            className="rounded-lg bg-primary px-4 py-2 text-xs font-semibold text-primary-foreground transition disabled:opacity-60"
          >
            {bulkLoading ? "Applying..." : "Apply"}
          </button>
        </div>
      </Card>

      <div className="grid gap-4">
        {loading && (
          <Card className="border border-border/60 bg-white/70 p-8 text-center text-sm text-muted-foreground dark:bg-white/10">
            Loading inbox...
          </Card>
        )}
        {!loading && filteredEmails.length === 0 && (
          <Card className="border border-border/60 bg-white/70 p-8 text-center text-sm text-muted-foreground dark:bg-white/10">
            No emails found for this category.
          </Card>
        )}

        {filteredEmails.map((email) => {
          const category = email.category ?? "Unofficial";
          const config = CATEGORY_CONFIG[category];
          const Icon = config.icon;
          const urgency = category === "Deadlines";
          return (
            <Card
              key={email.id}
              className={cn(
                "border border-border/60 bg-white/80 p-5 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-md dark:bg-white/10",
                email.important && "ring-1 ring-primary/20"
              )}
            >
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="mt-1">
                  <input
                    type="checkbox"
                    checked={selectedIds.includes(email.id)}
                    onChange={() => toggleSelect(email.id)}
                    className="h-4 w-4 rounded border-border"
                  />
                </div>
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="text-base font-semibold text-foreground">{email.subject}</h3>
                    {urgency && (
                      <span className="rounded-full bg-rose-500/15 px-2 py-0.5 text-xs font-semibold text-rose-600 dark:text-rose-200">
                        Due Soon
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-xs text-muted-foreground">{email.sender}</p>
                  <p className="mt-3 text-sm text-slate-600 dark:text-white/70">{email.content}</p>
                  {email.suggestedCategory && email.suggestedCategory !== email.category && (
                    <p className="mt-2 text-xs text-muted-foreground">
                      Suggested:{" "}
                      <span className="font-semibold text-foreground" title="Smart suggestion based on keywords">
                        {email.suggestedCategory}
                      </span>
                    </p>
                  )}
                </div>

                <div className="flex flex-col items-end gap-2">
                  <span className={cn("inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold", config.color)}>
                    <Icon className="h-3.5 w-3.5" />
                    {config.label}
                  </span>
                  <p className="text-xs text-muted-foreground">{email.date}</p>
                  <select
                    value={category}
                    onChange={(event) =>
                      handleCategoryChange(email.id, event.target.value as Exclude<EmailCategory, "All">)
                    }
                    className="h-8 rounded-lg border border-border/60 bg-white/80 px-2 text-xs text-foreground shadow-sm dark:bg-white/10"
                  >
                    {Object.keys(CATEGORY_CONFIG).map((key) => (
                      <option key={key} value={key}>
                        {key}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    onClick={() => handleImportantToggle(email.id, !email.important)}
                    className={cn(
                      "text-xs font-semibold",
                      email.important ? "text-primary" : "text-muted-foreground hover:text-foreground"
                    )}
                  >
                    {email.important ? "Important" : "Mark important"}
                  </button>
                </div>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}

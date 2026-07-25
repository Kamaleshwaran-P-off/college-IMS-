import { motion } from "framer-motion";
import { Bookmark, BookmarkCheck, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import type { Opportunity } from "@/lib/happenstanceData";

type OpportunityCardProps = {
  item: Opportunity;
  saved?: boolean;
  onSave?: (item: Opportunity) => void;
  onApply?: (item: Opportunity) => void;
  variant?: "default" | "highlight";
};

const domainTone: Record<string, string> = {
  AI: "bg-blue-500/15 text-blue-600",
  "Web Dev": "bg-emerald-500/15 text-emerald-600",
  Startup: "bg-orange-500/15 text-orange-600",
  Design: "bg-pink-500/15 text-pink-600",
  "Data Science": "bg-indigo-500/15 text-indigo-600",
  Cybersecurity: "bg-rose-500/15 text-rose-600",
  Product: "bg-violet-500/15 text-violet-600",
  IoT: "bg-teal-500/15 text-teal-600",
  Mobile: "bg-sky-500/15 text-sky-600",
  "Open Source": "bg-slate-500/15 text-slate-600"
};

export default function OpportunityCard({
  item,
  saved,
  onSave,
  onApply,
  variant = "default"
}: OpportunityCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className="h-full"
    >
      <Card
        className={`group flex h-full flex-col gap-4 rounded-2xl border border-border/60 bg-white/80 p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-lg dark:bg-white/10 ${
          variant === "highlight"
            ? "ring-2 ring-primary/40 shadow-[0_18px_30px_-20px_rgba(59,130,246,0.6)]"
            : ""
        }`}
      >
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2 text-xs font-semibold">
            <span className={`rounded-full px-3 py-1 ${domainTone[item.domain] || "bg-slate-500/15 text-slate-600"}`}>
              {item.domain}
            </span>
            <span className="rounded-full bg-slate-900/10 px-3 py-1 text-slate-600 dark:bg-white/10 dark:text-white/80">
              {item.type}
            </span>
            <span className="rounded-full bg-primary/10 px-3 py-1 text-primary">
              {item.platform}
            </span>
          </div>
          <h3 className="text-lg font-semibold text-foreground">{item.title}</h3>
          {item.date && (
            <p className="text-xs text-muted-foreground">
              {item.date} {item.location ? `• ${item.location}` : ""}
            </p>
          )}
          <p className="text-sm text-muted-foreground">{item.description}</p>
          <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
            {item.tags.map((tag) => (
              <span key={`${item.id}-${tag}`} className="rounded-full border border-border/60 px-2 py-0.5">
                {tag}
              </span>
            ))}
          </div>
        </div>
        <div className="mt-auto flex items-center gap-2">
          <Button
            className="flex-1"
            onClick={() => onApply?.(item)}
          >
            Apply <ExternalLink size={14} className="ml-2" />
          </Button>
          <button
            type="button"
            onClick={() => onSave?.(item)}
            className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-border/60 bg-white/70 text-slate-600 transition hover:text-primary dark:bg-white/10 dark:text-white/70"
            aria-label="Save opportunity"
          >
            {saved ? <BookmarkCheck size={18} className="text-primary" /> : <Bookmark size={18} />}
          </button>
        </div>
      </Card>
    </motion.div>
  );
}

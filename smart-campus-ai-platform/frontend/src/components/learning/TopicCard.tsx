import { Lock, PlayCircle, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface TopicCardProps {
  title: string;
  description: string;
  status: string;
  order: number;
  bestScore?: number | null;
  onStart?: () => void;
}

export default function TopicCard({ title, description, status, order, bestScore, onStart }: TopicCardProps) {
  const isLocked = status === "LOCKED";
  const isCompleted = status === "COMPLETED";
  const statusLabel = isCompleted ? "COMPLETED ✅" : isLocked ? "LOCKED 🔒" : "UNLOCKED 🔓";
  const helperText = isCompleted
    ? "Topic complete. Revisit anytime to improve your score."
    : isLocked
      ? "Finish the previous topic to unlock this lesson."
      : "Complete this topic to unlock next.";

  return (
    <div className={cn("rounded-2xl border border-border/60 bg-white/80 p-4 shadow-sm dark:bg-white/10", isLocked && "opacity-60")}> 
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Topic {order}</p>
          <h4 className="text-lg font-semibold">{title}</h4>
          <p className="mt-2 text-sm text-muted-foreground">{description}</p>
        </div>
        {isCompleted ? (
          <CheckCircle2 className="text-emerald-500" size={22} />
        ) : isLocked ? (
          <Lock className="text-muted-foreground" size={20} />
        ) : (
          <PlayCircle className="text-primary" size={22} />
        )}
      </div>

      <div className="mt-4 flex flex-wrap items-center gap-3">
        <span className={cn("rounded-full px-3 py-1 text-xs font-semibold", isCompleted ? "bg-emerald-500/15 text-emerald-600" : isLocked ? "bg-slate-500/15 text-slate-500" : "bg-blue-500/15 text-blue-600")}>
          {statusLabel}
        </span>
        {bestScore != null && (
          <span className="text-xs text-muted-foreground">Best score: {bestScore}/3</span>
        )}
        <Button size="sm" onClick={onStart} disabled={isLocked} className="ml-auto">
          Start Focus
        </Button>
      </div>

      <p className="mt-3 text-xs text-muted-foreground">{helperText}</p>
    </div>
  );
}

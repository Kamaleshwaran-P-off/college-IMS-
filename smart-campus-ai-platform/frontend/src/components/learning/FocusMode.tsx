import { useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface FocusModeProps {
  topicTitle: string;
  durationMinutes: number;
  onComplete: () => void;
  onExit: () => void;
}

const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
};

export default function FocusMode({ topicTitle, durationMinutes, onComplete, onExit }: FocusModeProps) {
  const [remaining, setRemaining] = useState(durationMinutes * 60);
  const [warning, setWarning] = useState<string | null>(null);

  useEffect(() => {
    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const handleVisibility = () => {
      if (document.hidden) {
        setWarning("Stay focused! You switched tabs during focus mode.");
      }
    };

    document.addEventListener("visibilitychange", handleVisibility);

    return () => {
      document.body.style.overflow = originalOverflow;
      document.removeEventListener("visibilitychange", handleVisibility);
    };
  }, []);

  useEffect(() => {
    if (remaining <= 0) {
      onComplete();
      return;
    }
    const timer = window.setInterval(() => {
      setRemaining((prev) => Math.max(prev - 1, 0));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [onComplete, remaining]);

  const progress = useMemo(() => {
    const total = durationMinutes * 60;
    return total === 0 ? 0 : ((total - remaining) / total) * 100;
  }, [durationMinutes, remaining]);

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-slate-950/90 px-6 py-10">
      <motion.div
        initial={{ opacity: 0, scale: 0.96 }}
        animate={{ opacity: 1, scale: 1 }}
        className="w-full max-w-3xl rounded-3xl border border-white/10 bg-gradient-to-br from-slate-900/90 via-slate-950/90 to-black p-8 text-white shadow-[0_30px_80px_-40px_rgba(15,23,42,0.85)]"
      >
        <p className="text-xs uppercase tracking-[0.4em] text-white/50">Focus Mode</p>
        <h2 className="mt-3 text-3xl font-semibold">{topicTitle}</h2>
        <p className="mt-2 text-sm text-white/60">Stay locked in. We will prompt the quiz when time is up.</p>

        <div className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6">
          <p className="text-sm text-white/60">Remaining</p>
          <div className="mt-3 text-5xl font-semibold tracking-widest">{formatTime(remaining)}</div>
          <div className="mt-4 h-2 w-full overflow-hidden rounded-full bg-white/10">
            <div className="h-full bg-emerald-400 transition-all" style={{ width: `${progress}%` }} />
          </div>
        </div>

        {warning && (
          <div className="mt-6 rounded-2xl border border-amber-400/30 bg-amber-400/10 px-4 py-3 text-sm text-amber-100">
            {warning}
          </div>
        )}

        <div className="mt-8 flex flex-wrap items-center justify-between gap-4">
          <Button
            variant="secondary"
            onClick={() => {
              if (window.confirm("Exit focus mode? Your timer will reset.")) {
                onExit();
              }
            }}
          >
            Exit Focus
          </Button>
          <span className={cn("text-xs uppercase tracking-[0.4em]", remaining <= 0 ? "text-emerald-300" : "text-white/40")}>
            {remaining <= 0 ? "Session complete" : "Focus engaged"}
          </span>
        </div>
      </motion.div>
    </div>
  );
}

import ProgressChart from "@/components/Charts/ProgressChart";
import { Card } from "@/components/ui/card";

interface ProgressPanelProps {
  completionPercent: number;
  completedTopics: number;
  totalTopics: number;
  points: number;
  streak: number;
  badges?: string;
  recentScores: number[];
}

export default function ProgressPanel({
  completionPercent,
  completedTopics,
  totalTopics,
  points,
  streak,
  badges,
  recentScores
}: ProgressPanelProps) {
  const labels = recentScores.map((_, index) => `Attempt ${recentScores.length - index}`);

  return (
    <Card className="space-y-4 p-6">
      <div>
        <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Progress</p>
        <h3 className="text-lg font-semibold">Learning Analytics</h3>
      </div>

      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm">
          <span>Topic completion</span>
          <span>{completedTopics} / {totalTopics}</span>
        </div>
        <div className="h-2 overflow-hidden rounded-full bg-muted">
          <div className="h-full bg-emerald-500" style={{ width: `${completionPercent}%` }} />
        </div>
        <p className="text-xs text-muted-foreground">{completionPercent.toFixed(1)}% complete</p>
      </div>

      <div className="grid gap-3 md:grid-cols-3">
        <div className="rounded-2xl border border-border/60 bg-muted/40 p-3 text-center">
          <p className="text-xs text-muted-foreground">Points</p>
          <p className="text-xl font-semibold">{points}</p>
        </div>
        <div className="rounded-2xl border border-border/60 bg-muted/40 p-3 text-center">
          <p className="text-xs text-muted-foreground">Streak</p>
          <p className="text-xl font-semibold">{streak} days</p>
        </div>
        <div className="rounded-2xl border border-border/60 bg-muted/40 p-3 text-center">
          <p className="text-xs text-muted-foreground">Badges</p>
          <p className="text-sm font-semibold">{badges || "Keep going"}</p>
        </div>
      </div>

      <div className="h-40">
        <ProgressChart labels={labels} data={recentScores} label="Quiz Scores" color="rgb(16, 185, 129)" height={140} max={3} />
      </div>
    </Card>
  );
}

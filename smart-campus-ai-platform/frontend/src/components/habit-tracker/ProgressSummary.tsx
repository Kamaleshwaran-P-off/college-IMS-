import { Progress } from "@/components/ui/progress";

type WeeklyProgress = {
  label: string;
  percent: number;
  completed: number;
  total: number;
};

type ProgressSummaryProps = {
  completed: number;
  total: number;
  percent: number;
  weekly: WeeklyProgress[];
};

export default function ProgressSummary({
  completed,
  total,
  percent,
  weekly
}: ProgressSummaryProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-xl border border-slate-200/70 bg-white p-4 shadow-sm dark:border-white/10 dark:bg-slate-950">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Overview</p>
            <h3 className="text-sm font-semibold text-foreground">Overall Progress</h3>
          </div>
          <div className="text-right text-xs text-muted-foreground">
            <div>{completed} completed</div>
            <div>{Math.max(total - completed, 0)} remaining</div>
          </div>
        </div>
        <div className="mt-3">
          <Progress value={percent} />
          <p className="mt-2 text-xs text-muted-foreground">{percent.toFixed(1)}% complete</p>
        </div>
      </div>

      <div className="rounded-xl border border-slate-200/70 bg-white p-4 shadow-sm dark:border-white/10 dark:bg-slate-950">
        <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Weekly Progress</p>
        <div className="mt-3 space-y-3">
          {weekly.map((week) => (
            <div key={week.label}>
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <span>{week.label}</span>
                <span>
                  {week.completed}/{week.total}
                </span>
              </div>
              <Progress value={week.percent} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

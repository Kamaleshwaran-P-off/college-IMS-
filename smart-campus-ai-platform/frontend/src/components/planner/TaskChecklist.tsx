import { CheckCircle2, Circle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";

export type ChecklistTask = {
  id: number;
  assignmentTitle: string;
  taskDetail: string;
  date: string;
  hours: number;
  completed: boolean;
};

type Props = {
  tasks: ChecklistTask[];
  onComplete: (id: number) => void;
};

export default function TaskChecklist({ tasks, onComplete }: Props) {
  const completed = tasks.filter((task) => task.completed).length;
  const progress = tasks.length === 0 ? 0 : Math.round((completed / tasks.length) * 100);

  return (
    <Card className="bg-white/80 dark:bg-white/10">
      <CardHeader className="pb-2">
        <CardTitle className="text-sm text-slate-700 dark:text-slate-200">
          Task Checklist
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="space-y-1">
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <span>{completed} of {tasks.length} completed</span>
            <span>{progress}%</span>
          </div>
          <Progress value={progress} className="h-2" />
        </div>

        {tasks.length === 0 ? (
          <p className="text-sm text-muted-foreground">No tasks generated yet.</p>
        ) : (
          tasks.map((task) => (
            <button
              key={task.id}
              type="button"
              onClick={() => !task.completed && onComplete(task.id)}
              className="flex w-full items-start gap-3 rounded-xl border border-border/50 bg-white/70 px-3 py-2 text-left text-sm transition hover:border-primary/40 dark:bg-white/5"
            >
              {task.completed ? (
                <CheckCircle2 className="mt-0.5 h-4 w-4 text-emerald-500" />
              ) : (
                <Circle className="mt-0.5 h-4 w-4 text-muted-foreground" />
              )}
              <div>
                <p className="font-semibold text-foreground">{task.assignmentTitle}</p>
                <p className="text-xs text-muted-foreground">{task.taskDetail}</p>
                <p className="text-xs text-muted-foreground mt-1">{task.date} · {task.hours}h</p>
              </div>
            </button>
          ))
        )}
      </CardContent>
    </Card>
  );
}

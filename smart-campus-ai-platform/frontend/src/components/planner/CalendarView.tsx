import { useMemo } from "react";
import { CalendarDays, Clock3 } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

export type PlannerTask = {
  id: number;
  assignmentTitle: string;
  date: string;
  taskDetail: string;
  hours: number;
  completed: boolean;
};

type Props = {
  tasks: PlannerTask[];
};

export default function CalendarView({ tasks }: Props) {
  const grouped = useMemo(() => {
    return tasks.reduce<Record<string, PlannerTask[]>>((acc, task) => {
      acc[task.date] = acc[task.date] ? [...acc[task.date], task] : [task];
      return acc;
    }, {});
  }, [tasks]);

  const dates = useMemo(() => Object.keys(grouped).sort(), [grouped]);

  if (tasks.length === 0) {
    return (
      <Card className="bg-white/80 dark:bg-white/10">
        <CardContent className="p-6 text-sm text-muted-foreground">
          No scheduled tasks yet. Generate a plan to see your calendar.
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {dates.map((date) => {
        const dayTasks = grouped[date];
        const totalHours = dayTasks.reduce((sum, task) => sum + (task.hours || 0), 0);
        return (
          <Card key={date} className="bg-white/80 dark:bg-white/10">
            <CardHeader className="pb-2">
              <CardTitle className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-200">
                <CalendarDays className="h-4 w-4 text-indigo-500" />
                {date}
                <Badge variant="outline" className="ml-auto text-xs">
                  <Clock3 className="mr-1 h-3 w-3" /> {totalHours}h
                </Badge>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {dayTasks.map((task) => (
                <div
                  key={task.id}
                  className="rounded-xl border border-border/50 bg-white/70 px-4 py-3 text-sm dark:bg-white/5"
                >
                  <p className="font-semibold text-foreground">{task.assignmentTitle}</p>
                  <p className="text-xs text-muted-foreground">{task.taskDetail}</p>
                  <div className="mt-2 flex items-center gap-2 text-xs text-muted-foreground">
                    <Clock3 className="h-3 w-3" /> {task.hours} hours
                    {task.completed && (
                      <Badge className="bg-emerald-500/15 text-emerald-600 dark:text-emerald-300">Done</Badge>
                    )}
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}

import { useMemo, useState } from "react";
import { Card } from "@/components/ui/card";
import HabitTable from "@/components/habit-tracker/HabitTable";
import ProgressSummary from "@/components/habit-tracker/ProgressSummary";
import ChartComponent from "@/components/habit-tracker/ChartComponent";
import { Habit, WeekBlock, WeekDayCell } from "@/components/habit-tracker/types";

const DAY_LABELS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
const STORAGE_PREFIX = "habit-tracker";

const buildMonthWeeks = (year: number, monthIndex: number): { weeks: WeekBlock[]; days: WeekDayCell[] } => {
  const first = new Date(year, monthIndex, 1);
  const last = new Date(year, monthIndex + 1, 0);
  const startOffset = (first.getDay() + 6) % 7;
  const cursor = new Date(first);
  cursor.setDate(first.getDate() - startOffset);

  const weeks: WeekBlock[] = [];
  const allDays: WeekDayCell[] = [];
  let weekCount = 0;

  while (cursor <= last || weekCount < 5) {
    const weekDays: WeekDayCell[] = [];
    for (let i = 0; i < 7; i += 1) {
      const inMonth = cursor.getMonth() === monthIndex;
      const dayNumber = inMonth ? String(cursor.getDate()) : "";
      const key = cursor.toISOString().slice(0, 10);
      weekDays.push({
        date: inMonth ? new Date(cursor) : null,
        key,
        dayLabel: DAY_LABELS[i],
        dayNumber,
        inMonth
      });
      allDays.push(weekDays[weekDays.length - 1]);
      cursor.setDate(cursor.getDate() + 1);
    }
    weekCount += 1;
    weeks.push({ label: `Week ${weekCount}`, days: weekDays });
    if (cursor > last && weekCount >= 5) {
      break;
    }
  }

  return { weeks, days: allDays };
};

const buildDefaultHabits = (daysInMonth: number): Habit[] => {
  const base = [
    { name: "Deep Study", goal: daysInMonth },
    { name: "Assignment Practice", goal: Math.min(daysInMonth, 20) },
    { name: "Revision Session", goal: Math.min(daysInMonth, 15) }
  ];
  return base.map((item) => ({
    id: crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`,
    name: item.name,
    goal: item.goal,
    checks: {}
  }));
};

export default function HabitTrackerPage() {
  const today = new Date();
  const year = today.getFullYear();
  const monthIndex = today.getMonth();
  const monthKey = `${year}-${String(monthIndex + 1).padStart(2, "0")}`;
  const daysInMonth = new Date(year, monthIndex + 1, 0).getDate();

  const initialHabits = useMemo(() => {
    const saved = localStorage.getItem(`${STORAGE_PREFIX}:${monthKey}`);
    if (saved) {
      try {
        return JSON.parse(saved) as Habit[];
      } catch {
        return buildDefaultHabits(daysInMonth);
      }
    }
    return buildDefaultHabits(daysInMonth);
  }, [daysInMonth, monthKey]);

  const [habits, setHabits] = useState<Habit[]>(initialHabits);
  const [newHabit, setNewHabit] = useState("");
  const [goalInput, setGoalInput] = useState(daysInMonth);

  const { weeks, days } = useMemo(() => buildMonthWeeks(year, monthIndex), [monthIndex, year]);

  const persistHabits = (next: Habit[]) => {
    setHabits(next);
    localStorage.setItem(`${STORAGE_PREFIX}:${monthKey}`, JSON.stringify(next));
  };

  const handleToggle = (habitId: string, dateKey: string) => {
    const next = habits.map((habit) => {
      if (habit.id !== habitId) return habit;
      const current = habit.checks[dateKey] || false;
      return {
        ...habit,
        checks: { ...habit.checks, [dateKey]: !current }
      };
    });
    persistHabits(next);
  };

  const handleGoalChange = (habitId: string, goal: number) => {
    const next = habits.map((habit) =>
      habit.id === habitId ? { ...habit, goal: Math.max(goal, 0) } : habit
    );
    persistHabits(next);
  };

  const handleDelete = (habitId: string) => {
    const next = habits.filter((habit) => habit.id !== habitId);
    persistHabits(next);
  };

  const handleAddHabit = () => {
    if (!newHabit.trim()) return;
    const next: Habit[] = [
      ...habits,
      {
        id: crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`,
        name: newHabit.trim(),
        goal: Math.max(goalInput, 0),
        checks: {}
      }
    ];
    persistHabits(next);
    setNewHabit("");
    setGoalInput(daysInMonth);
  };

  const datesInMonth = useMemo(() => days.filter((day) => day.inMonth), [days]);

  const totalChecks = useMemo(() => {
    return habits.reduce((sum, habit) => {
      const count = Object.entries(habit.checks)
        .filter(([key, value]) => value && datesInMonth.some((day) => day.key === key))
        .length;
      return sum + count;
    }, 0);
  }, [habits, datesInMonth]);

  const totalPossible = habits.length * datesInMonth.length;
  const overallPercent = totalPossible === 0 ? 0 : (totalChecks / totalPossible) * 100;

  const weeklyProgress = weeks.map((week) => {
    const weekDates = week.days.filter((day) => day.inMonth);
    const total = weekDates.length * habits.length;
    const completed = habits.reduce((sum, habit) => {
      const count = weekDates.filter((day) => habit.checks[day.key]).length;
      return sum + count;
    }, 0);
    const percent = total === 0 ? 0 : (completed / total) * 100;
    return { label: week.label, completed, total, percent };
  });

  const chartData = datesInMonth.map((day) => {
    const completed = habits.reduce((sum, habit) => sum + (habit.checks[day.key] ? 1 : 0), 0);
    const percent = habits.length === 0 ? 0 : (completed / habits.length) * 100;
    return { label: day.dayNumber || "", value: Math.round(percent) };
  });

  return (
    <div className="space-y-6">
      <Card className="border border-border/60 bg-white/80 p-6 shadow-sm backdrop-blur dark:bg-white/10">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">Habit Tracker</p>
            <h2 className="text-2xl font-semibold text-foreground">
              Monthly Habit & Study Planner
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Track daily progress with an Excel-style grid and visual summaries.
            </p>
          </div>
          <div className="rounded-xl border border-slate-200/70 bg-white px-4 py-2 text-xs text-slate-600 shadow-sm dark:border-white/10 dark:bg-slate-900 dark:text-white/70">
            {today.toLocaleString("default", { month: "long" })} {year}
          </div>
        </div>
      </Card>

      <div className="grid gap-6 lg:grid-cols-[1.3fr_0.7fr]">
        <HabitTable
          weeks={weeks}
          days={days}
          habits={habits}
          onToggle={handleToggle}
          onGoalChange={handleGoalChange}
          onDelete={handleDelete}
        />

        <div className="space-y-4">
          <ProgressSummary
            completed={totalChecks}
            total={totalPossible}
            percent={overallPercent}
            weekly={weeklyProgress}
          />
          <ChartComponent data={chartData} />
          <Card className="border border-slate-200/70 bg-white p-4 shadow-sm dark:border-white/10 dark:bg-slate-950">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">
              Add Habit
            </p>
            <div className="mt-3 space-y-2">
              <input
                value={newHabit}
                onChange={(event) => setNewHabit(event.target.value)}
                placeholder="Habit name"
                className="h-9 w-full rounded-lg border border-slate-200/80 bg-white px-3 text-sm text-slate-700 shadow-sm focus:outline-none dark:border-white/10 dark:bg-slate-900 dark:text-white"
              />
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  min={0}
                  value={goalInput}
                  onChange={(event) => setGoalInput(Number(event.target.value || 0))}
                  className="h-9 w-24 rounded-lg border border-slate-200/80 bg-white px-3 text-sm text-slate-700 shadow-sm focus:outline-none dark:border-white/10 dark:bg-slate-900 dark:text-white"
                />
                <button
                  type="button"
                  onClick={handleAddHabit}
                  className="flex-1 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground transition hover:opacity-90"
                >
                  Add Habit
                </button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

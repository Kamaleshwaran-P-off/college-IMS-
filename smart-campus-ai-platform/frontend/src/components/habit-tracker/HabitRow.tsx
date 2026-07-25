import { Habit, WeekDayCell } from "./types";
import { Trash2 } from "lucide-react";

type HabitRowProps = {
  habit: Habit;
  days: WeekDayCell[];
  onToggle: (habitId: string, dateKey: string) => void;
  onGoalChange: (habitId: string, goal: number) => void;
  onDelete: (habitId: string) => void;
};

export default function HabitRow({
  habit,
  days,
  onToggle,
  onGoalChange,
  onDelete
}: HabitRowProps) {
  return (
    <tr className="border-b border-slate-200/70">
      <td className="sticky left-0 z-10 w-52 min-w-[13rem] border-r border-slate-200/70 bg-white px-3 py-2 text-xs font-semibold text-slate-700 dark:bg-slate-950 dark:text-white">
        <div className="flex items-center justify-between gap-2">
          <span className="truncate">{habit.name}</span>
          <button
            type="button"
            onClick={() => onDelete(habit.id)}
            className="rounded-md p-1 text-slate-400 transition hover:text-rose-500"
          >
            <Trash2 size={14} />
          </button>
        </div>
      </td>
      <td className="w-16 border-r border-slate-200/70 bg-white px-2 py-2 text-center text-xs dark:bg-slate-950">
        <input
          type="number"
          min={0}
          value={habit.goal}
          onChange={(event) => onGoalChange(habit.id, Number(event.target.value || 0))}
          className="h-7 w-14 rounded-md border border-slate-200/80 bg-white text-center text-xs text-slate-700 shadow-sm focus:outline-none dark:border-white/10 dark:bg-slate-900 dark:text-white"
        />
      </td>
      {days.map((day) => {
        if (!day.inMonth) {
          return (
            <td key={`${habit.id}-${day.key}`} className="h-8 w-10 border border-slate-200/70 bg-slate-50/60 dark:bg-slate-900/40" />
          );
        }
        const checked = habit.checks[day.key] || false;
        return (
          <td
            key={`${habit.id}-${day.key}`}
            className="h-8 w-10 border border-slate-200/70 text-center"
          >
            <input
              type="checkbox"
              checked={checked}
              onChange={() => onToggle(habit.id, day.key)}
              className="h-4 w-4 cursor-pointer rounded-sm border border-slate-300 text-emerald-600 focus:ring-0"
            />
          </td>
        );
      })}
    </tr>
  );
}

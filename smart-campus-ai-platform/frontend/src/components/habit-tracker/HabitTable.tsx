import HabitRow from "./HabitRow";
import { Habit, WeekBlock, WeekDayCell } from "./types";

const WEEK_COLORS = [
  "bg-emerald-700 text-white",
  "bg-blue-700 text-white",
  "bg-purple-700 text-white",
  "bg-orange-700 text-white",
  "bg-sky-700 text-white",
  "bg-rose-700 text-white"
];

type HabitTableProps = {
  weeks: WeekBlock[];
  days: WeekDayCell[];
  habits: Habit[];
  onToggle: (habitId: string, dateKey: string) => void;
  onGoalChange: (habitId: string, goal: number) => void;
  onDelete: (habitId: string) => void;
};

export default function HabitTable({
  weeks,
  days,
  habits,
  onToggle,
  onGoalChange,
  onDelete
}: HabitTableProps) {
  return (
    <div className="overflow-auto rounded-xl border border-slate-200/70 bg-white shadow-sm dark:border-white/10 dark:bg-slate-950">
      <table className="w-full border-collapse text-xs">
        <thead>
          <tr>
            <th rowSpan={2} className="sticky left-0 z-20 w-52 min-w-[13rem] border border-slate-200/70 bg-slate-900 px-3 py-2 text-left text-xs font-semibold text-white">
              Daily Habits
            </th>
            <th rowSpan={2} className="w-16 border border-slate-200/70 bg-slate-900 px-2 py-2 text-center text-xs font-semibold text-white">
              Goals
            </th>
            {weeks.map((week, index) => (
              <th
                key={week.label}
                colSpan={7}
                className={`border border-slate-200/70 px-2 py-2 text-center text-[11px] font-semibold ${WEEK_COLORS[index % WEEK_COLORS.length]}`}
              >
                {week.label}
              </th>
            ))}
          </tr>
          <tr>
            {days.map((day) => (
              <th
                key={`head-${day.key}`}
                className={`border border-slate-200/70 px-1 py-1 text-center text-[10px] font-medium ${
                  day.inMonth ? "bg-slate-100 text-slate-700" : "bg-slate-50 text-slate-300"
                }`}
              >
                <div>{day.dayLabel}</div>
                <div className="text-[10px] font-semibold">{day.dayNumber}</div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {habits.map((habit) => (
            <HabitRow
              key={habit.id}
              habit={habit}
              days={days}
              onToggle={onToggle}
              onGoalChange={onGoalChange}
              onDelete={onDelete}
            />
          ))}
          {habits.length === 0 && (
            <tr>
              <td colSpan={days.length + 2} className="p-6 text-center text-sm text-slate-500">
                Add your first habit to start tracking.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

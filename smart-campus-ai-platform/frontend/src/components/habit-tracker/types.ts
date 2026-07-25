export type HabitCheckMap = Record<string, boolean>;

export type Habit = {
  id: string;
  name: string;
  goal: number;
  checks: HabitCheckMap;
};

export type WeekDayCell = {
  date: Date | null;
  key: string;
  dayLabel: string;
  dayNumber: string;
  inMonth: boolean;
};

export type WeekBlock = {
  label: string;
  days: WeekDayCell[];
};

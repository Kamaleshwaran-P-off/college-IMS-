import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer
} from "recharts";

type ChartPoint = {
  label: string;
  value: number;
};

type ChartComponentProps = {
  data: ChartPoint[];
};

export default function ChartComponent({ data }: ChartComponentProps) {
  return (
    <div className="rounded-xl border border-slate-200/70 bg-white p-4 shadow-sm dark:border-white/10 dark:bg-slate-950">
      <p className="text-xs uppercase tracking-[0.3em] text-slate-500 dark:text-white/50">
        Monthly Progress
      </p>
      <div className="mt-3 h-40 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <XAxis dataKey="label" tick={{ fontSize: 10 }} />
            <YAxis domain={[0, 100]} tick={{ fontSize: 10 }} />
            <Tooltip />
            <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

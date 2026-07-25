import "chart.js/auto";
import { Bar } from "react-chartjs-2";

export type MarksDataset = {
  label: string;
  data: number[];
  color: string;
};

type MarksChartProps = {
  labels: string[];
  datasets: MarksDataset[];
  height?: number;
};

export default function MarksChart({ labels, datasets, height = 260 }: MarksChartProps) {
  if (!labels.length || datasets.length === 0) {
    return <p className="text-sm text-muted-foreground">No data available</p>;
  }

  return (
    <Bar
      height={height}
      data={{
        labels,
        datasets: datasets.map((set) => ({
          label: set.label,
          data: set.data,
          backgroundColor: set.color,
          borderRadius: 8
        }))
      }}
      options={{
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: "bottom" as const
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100
          }
        }
      }}
    />
  );
}

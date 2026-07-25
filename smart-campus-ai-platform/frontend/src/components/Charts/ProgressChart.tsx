import "chart.js/auto";
import { Line } from "react-chartjs-2";

type ProgressChartProps = {
  labels: string[];
  data: number[];
  label?: string;
  color?: string;
  height?: number;
  max?: number;
};

export default function ProgressChart({
  labels,
  data,
  label = "Progress",
  color = "rgb(99, 102, 241)",
  height = 260,
  max = 100
}: ProgressChartProps) {
  if (!labels.length || !data.length) {
    return <p className="text-sm text-muted-foreground">No data available</p>;
  }

  return (
    <Line
      height={height}
      data={{
        labels,
        datasets: [
          {
            label,
            data,
            borderColor: color,
            backgroundColor: color.replace("rgb", "rgba").replace(")", ", 0.2)"),
            tension: 0.35,
            fill: true,
            pointRadius: 3,
            pointBackgroundColor: color
          }
        ]
      }}
      options={{
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max
          }
        }
      }}
    />
  );
}

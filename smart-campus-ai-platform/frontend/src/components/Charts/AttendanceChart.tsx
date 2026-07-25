import "chart.js/auto";
import { Line } from "react-chartjs-2";

type AttendanceChartProps = {
  labels: string[];
  data: number[];
  height?: number;
};

export default function AttendanceChart({ labels, data, height = 260 }: AttendanceChartProps) {
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
            label: "Attendance %",
            data,
            borderColor: "rgb(59, 130, 246)",
            backgroundColor: "rgba(59, 130, 246, 0.2)",
            tension: 0.35,
            fill: true,
            pointRadius: 3,
            pointBackgroundColor: "rgb(59, 130, 246)"
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
            max: 100
          }
        }
      }}
    />
  );
}

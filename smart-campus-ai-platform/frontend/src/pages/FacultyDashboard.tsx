import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import AttendanceChart from "@/components/Charts/AttendanceChart";
import MarksChart from "@/components/Charts/MarksChart";
import Carousel from "@/components/Carousel";
import CardSkeleton from "@/components/skeletons/CardSkeleton";
import EmptyState from "@/components/feedback/EmptyState";
import { getJson } from "@/lib/api";

type FacultyDashboardResponse = {
  attendanceByDay: {
    labels: string[];
    series: { label: string; values: number[] }[];
  };
  averageInternalMarks: {
    labels: string[];
    cat1: number[];
    cat2: number[];
    cat3: number[];
  };
  averageAssignmentMarks: {
    labels: string[];
    scores: number[];
  };
  todayClasses: {
    time: string;
    className: string;
    subject: string;
    room: string;
  }[];
};

type FacultyClassSummary = {
  id: number | null;
  className: string;
  department?: string | null;
  section?: string | null;
};

const MOCK_FACULTY_DASHBOARD: FacultyDashboardResponse = {
  attendanceByDay: {
    labels: ["Mon", "Tue", "Wed", "Thu", "Fri"],
    series: [
      { label: "CSE-A", values: [92, 88, 90, 86, 89] },
      { label: "AI&DS-C", values: [86, 84, 82, 88, 85] }
    ]
  },
  averageInternalMarks: {
    labels: ["AI Systems", "DSA", "Math", "Networks"],
    cat1: [75, 72, 78, 70],
    cat2: [79, 76, 80, 74],
    cat3: [83, 81, 84, 78]
  },
  averageAssignmentMarks: {
    labels: ["AI Systems", "DSA", "Math", "Networks"],
    scores: [82, 78, 85, 80]
  },
  todayClasses: [
    { time: "09:00 - 09:50", className: "CSE-A", subject: "AI Systems", room: "CSE-201" },
    { time: "11:00 - 11:50", className: "AI&DS-C", subject: "Networks", room: "CSE-305" }
  ]
};

export default function FacultyDashboard() {
  const [data, setData] = useState<FacultyDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [assignedClasses, setAssignedClasses] = useState<FacultyClassSummary[]>([]);
  const [classError, setClassError] = useState<string | null>(null);
  const [selectedClass, setSelectedClass] = useState<FacultyClassSummary | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    getJson<FacultyDashboardResponse>("/api/faculty/dashboard")
      .then((response) => {
        if (!active) return;
        setData(response);
      })
      .catch(() => {
        if (!active) return;
        setError("Showing demo data. Connect backend for live analytics.");
        setData(MOCK_FACULTY_DASHBOARD);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    setClassError(null);
    getJson<FacultyClassSummary[]>("/api/faculty/classes/details")
      .then((response) => {
        if (!active) return;
        setAssignedClasses(response);
      })
      .catch((err) => {
        if (!active) return;
        setClassError(err instanceof Error ? err.message : "Failed to load assigned classes.");
      });
    return () => {
      active = false;
    };
  }, []);

  const handleOpenClass = async (item: FacultyClassSummary) => {
    if (!item.id) {
      setClassError("This class is not linked yet. Ask admin to reassign.");
      return;
    }
    setClassError(null);
    try {
      const response = await getJson<FacultyClassSummary>(`/api/faculty/class/${item.id}`);
      setSelectedClass(response);
    } catch (err) {
      setClassError(err instanceof Error ? err.message : "Access denied.");
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <CardSkeleton lines={2} />
        <div className="grid gap-4 lg:grid-cols-2">
          {Array.from({ length: 2 }).map((_, index) => (
            <CardSkeleton key={`faculty-chart-${index}`} lines={4} />
          ))}
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <EmptyState
        title="No faculty analytics available"
        description="Connect the backend to view attendance and marks insights."
      />
    );
  }

  return (
    <div className="space-y-6">
      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardDescription>Faculty Analytics</CardDescription>
          <CardTitle>Class Performance Overview</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Monitor attendance, internal averages, and today’s schedule.
        </CardContent>
      </Card>

      <Carousel />

      {error && (
        <div className="rounded-2xl border border-amber-200/60 bg-amber-50 px-4 py-3 text-sm text-amber-700 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-200">
          {error}
        </div>
      )}

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Assigned Classes</CardTitle>
          <CardDescription>Only classes assigned by admin are visible here.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {classError && (
            <div className="rounded-xl border border-rose-200/60 bg-rose-50 px-3 py-2 text-xs text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/10 dark:text-rose-200">
              {classError}
            </div>
          )}
          {assignedClasses.length === 0 ? (
            <p className="text-sm text-muted-foreground">No classes assigned yet.</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {assignedClasses.map((item) => (
                <button
                  key={`${item.id ?? item.className}`}
                  onClick={() => handleOpenClass(item)}
                  className="rounded-full border border-slate-200 bg-slate-50 px-4 py-2 text-xs font-semibold text-slate-700 hover:border-indigo-300 hover:bg-indigo-50 dark:border-white/10 dark:bg-white/10 dark:text-white/80"
                >
                  {item.className}
                </button>
              ))}
            </div>
          )}

          {selectedClass && (
            <div className="rounded-2xl border border-border/60 bg-slate-50 px-4 py-3 text-sm text-slate-600 dark:bg-white/10 dark:text-white/70">
              <div className="font-semibold text-slate-900 dark:text-white">
                {selectedClass.className}
              </div>
              <div className="text-xs text-slate-500 dark:text-white/60">
                Department: {selectedClass.department || "—"} · Section: {selectedClass.section || "—"}
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Class-wise Attendance</CardTitle>
            <CardDescription>Day-wise attendance trend</CardDescription>
          </CardHeader>
          <CardContent className="h-72 space-y-4">
            {data.attendanceByDay.series.length === 0 ? (
              <p className="text-sm text-muted-foreground">No data available</p>
            ) : (
              data.attendanceByDay.series.map((series) => (
                <div key={series.label} className="h-40">
                  <p className="mb-2 text-xs font-medium text-muted-foreground">{series.label}</p>
                  <AttendanceChart labels={data.attendanceByDay.labels} data={series.values} height={140} />
                </div>
              ))
            )}
          </CardContent>
        </Card>

        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Average Assignment Marks</CardTitle>
            <CardDescription>Subject-wise averages</CardDescription>
          </CardHeader>
          <CardContent className="h-72">
            <MarksChart
              labels={data.averageAssignmentMarks.labels}
              datasets={[
                {
                  label: "Assignment %",
                  data: data.averageAssignmentMarks.scores,
                  color: "rgba(16, 185, 129, 0.7)"
                }
              ]}
            />
          </CardContent>
        </Card>
      </div>

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Average Internal Marks</CardTitle>
          <CardDescription>CAT 1 / CAT 2 / CAT 3 averages</CardDescription>
        </CardHeader>
        <CardContent className="h-80">
          <MarksChart
            labels={data.averageInternalMarks.labels}
            datasets={[
              { label: "CAT 1", data: data.averageInternalMarks.cat1, color: "rgba(59, 130, 246, 0.7)" },
              { label: "CAT 2", data: data.averageInternalMarks.cat2, color: "rgba(168, 85, 247, 0.7)" },
              { label: "CAT 3", data: data.averageInternalMarks.cat3, color: "rgba(234, 88, 12, 0.7)" }
            ]}
          />
        </CardContent>
      </Card>

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Today’s Classes</CardTitle>
          <CardDescription>Based on assigned sections</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {data.todayClasses.length === 0 ? (
            <p className="text-sm text-muted-foreground">No classes scheduled today.</p>
          ) : (
            data.todayClasses.map((item, index) => (
              <div
                key={`${item.time}-${index}`}
                className="flex flex-wrap items-center justify-between gap-2 rounded-2xl border border-border/60 bg-slate-50 px-4 py-3 text-sm text-slate-600 dark:bg-white/10 dark:text-white/70"
              >
                <span className="font-medium text-slate-900 dark:text-white">{item.time}</span>
                <span>{item.className}</span>
                <span>{item.subject}</span>
                <span>{item.room}</span>
              </div>
            ))
          )}
        </CardContent>
      </Card>
    </div>
  );
}

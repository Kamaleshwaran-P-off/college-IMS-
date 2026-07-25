import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getJson, postJson } from "@/lib/api";

type StaffItem = {
  id: number;
  fullName: string;
  email: string;
  staffCode: string;
  department?: string;
  assignedClasses?: string;
};

type AssignClassPayload = {
  facultyId: number;
  classes: string[];
};

const CLASS_OPTIONS = ["CSE-A", "CSE-B", "AI&DS-C", "ECE-A", "IT-A", "MECH-A"];

export default function AdminAssignClasses() {
  const [staff, setStaff] = useState<StaffItem[]>([]);
  const [selectedFacultyId, setSelectedFacultyId] = useState<number | null>(null);
  const [selectedClasses, setSelectedClasses] = useState<string[]>([]);
  const [customClass, setCustomClass] = useState("");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    getJson<StaffItem[]>("/api/staff")
      .then((data) => {
        if (!active) return;
        setStaff(data);
      })
      .catch(() => {
        if (!active) return;
        setStaff([]);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const selectedFaculty = useMemo(
    () => staff.find((item) => item.id === selectedFacultyId) || null,
    [staff, selectedFacultyId]
  );

  useEffect(() => {
    if (!selectedFaculty) {
      setSelectedClasses([]);
      return;
    }
    const assigned = selectedFaculty.assignedClasses
      ? selectedFaculty.assignedClasses.split(",").map((cls) => cls.trim()).filter(Boolean)
      : [];
    setSelectedClasses(assigned);
  }, [selectedFaculty]);

  const toggleClass = (value: string) => {
    setSelectedClasses((prev) =>
      prev.includes(value) ? prev.filter((item) => item !== value) : [...prev, value]
    );
  };

  const addCustomClass = () => {
    const normalized = customClass.trim();
    if (!normalized) return;
    if (!selectedClasses.includes(normalized)) {
      setSelectedClasses((prev) => [...prev, normalized]);
    }
    setCustomClass("");
  };

  const handleAssign = async () => {
    if (!selectedFacultyId) return;
    setMessage(null);
    setError(null);

    try {
      const payload: AssignClassPayload = {
        facultyId: selectedFacultyId,
        classes: selectedClasses
      };
      await postJson("/api/admin/assign-class", payload);
      setMessage("Classes assigned successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Assignment failed");
    }
  };

  if (loading) {
    return (
      <div className="h-40 rounded-3xl border border-slate-200/60 bg-white/70 p-6 shadow-sm dark:border-white/10 dark:bg-white/10 animate-pulse" />
    );
  }

  return (
    <div className="space-y-6">
      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardDescription>Faculty-Class Assignment</CardDescription>
          <CardTitle>Assign Faculty to Classes</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Map faculty members to specific classes so their access is scoped correctly.
        </CardContent>
      </Card>

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Select Faculty</CardTitle>
          <CardDescription>Only active faculty are listed</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <select
            className="w-full rounded-xl border border-border bg-background px-4 py-2 text-sm"
            value={selectedFacultyId ?? ""}
            onChange={(event) => setSelectedFacultyId(Number(event.target.value) || null)}
          >
            <option value="">Choose faculty</option>
            {staff.map((item) => (
              <option key={item.id} value={item.id}>
                {item.fullName} ({item.staffCode})
              </option>
            ))}
          </select>

          {selectedFaculty && (
            <div className="rounded-2xl border border-border/60 bg-slate-50 px-4 py-3 text-sm text-slate-600 dark:bg-white/10 dark:text-white/70">
              Current classes: {selectedClasses.length ? selectedClasses.join(", ") : "None"}
            </div>
          )}
        </CardContent>
      </Card>

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Assign Classes</CardTitle>
          <CardDescription>Pick from suggested list or add custom</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            {CLASS_OPTIONS.map((option) => (
              <label
                key={option}
                className={`flex items-center gap-2 rounded-xl border px-3 py-2 text-sm ${
                  selectedClasses.includes(option)
                    ? "border-primary bg-primary/10 text-primary"
                    : "border-border bg-background text-muted-foreground"
                }`}
              >
                <input
                  type="checkbox"
                  checked={selectedClasses.includes(option)}
                  onChange={() => toggleClass(option)}
                />
                {option}
              </label>
            ))}
          </div>

          <div className="flex flex-wrap gap-2">
            <input
              type="text"
              value={customClass}
              onChange={(event) => setCustomClass(event.target.value)}
              placeholder="Add custom class"
              className="flex-1 rounded-xl border border-border bg-background px-4 py-2 text-sm"
            />
            <button
              type="button"
              onClick={addCustomClass}
              className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
            >
              Add
            </button>
          </div>

          <button
            type="button"
            disabled={!selectedFacultyId || selectedClasses.length === 0}
            onClick={handleAssign}
            className="rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground disabled:opacity-60"
          >
            Save Assignment
          </button>

          {message && <p className="text-sm text-green-600">{message}</p>}
          {error && <p className="text-sm text-red-600">{error}</p>}
        </CardContent>
      </Card>
    </div>
  );
}

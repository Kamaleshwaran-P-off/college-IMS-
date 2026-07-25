import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { getJson, putJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

type StudentProfile = {
  id: number;
  userId: number;
  studentCode: string;
  department?: string | null;
  yearOfStudy?: number | null;
  section?: string | null;
  phone?: string | null;
  parentPhone?: string | null;
};

type StaffProfile = {
  id: number;
  userId: number;
  staffCode: string;
  department?: string | null;
  designation?: string | null;
  phone?: string | null;
  assignedClasses?: string | null;
  skills?: string | null;
  interests?: string | null;
};

const DEPARTMENTS = ["CSE", "AI&DS", "CSBS", "CSE(AI&ML)", "VLSE", "ECE", "CCE", "BIOTECH", "MECH"];
const SECTIONS = ["A", "B", "C", "D", "E", "F", "G", "H", "I"];

export default function ProfileCompletionModal() {
  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);

  const { role } = useAuth();
  const normalizedRole = (role || "").toUpperCase();

  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mode, setMode] = useState<"student" | "staff" | null>(null);
  const [studentProfile, setStudentProfile] = useState<StudentProfile | null>(null);
  const [staffProfile, setStaffProfile] = useState<StaffProfile | null>(null);

  const [department, setDepartment] = useState("");
  const [section, setSection] = useState("");
  const [yearOfStudy, setYearOfStudy] = useState<number | null>(null);
  const [assignedClasses, setAssignedClasses] = useState("");
  const [studentCode, setStudentCode] = useState("");
  const [skills, setSkills] = useState("");
  const [interests, setInterests] = useState("");

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      return;
    }
    if (normalizedRole !== "STUDENT" && normalizedRole !== "STAFF" && normalizedRole !== "FACULTY") {
      setLoading(false);
      return;
    }

    const dismissed = sessionStorage.getItem("profilePromptDismissed") === "true";
    if (dismissed) {
      setLoading(false);
      return;
    }

    const load = async () => {
      try {
        if (normalizedRole === "STUDENT") {
          const profile = await getJson<StudentProfile>(`/api/students/by-user?userId=${userId}`);
          setStudentProfile(profile);
          setMode("student");
          const needsDepartment = !profile.department || profile.department.trim() === "";
          const needsSection = !profile.section || profile.section.trim() === "";
          const needsStudentCode =
            !profile.studentCode ||
            profile.studentCode.trim() === "" ||
            profile.studentCode.toUpperCase().startsWith("AUTO-");
          if (needsDepartment || needsSection || needsStudentCode) {
            setDepartment(profile.department ?? "");
            setSection(profile.section ?? "");
            setYearOfStudy(profile.yearOfStudy ?? null);
            setStudentCode(needsStudentCode ? "" : profile.studentCode ?? "");
            setOpen(true);
          }
        } else {
          const profile = await getJson<StaffProfile>(`/api/staff/by-user?userId=${userId}`);
          setStaffProfile(profile);
          setMode("staff");
          const needsDepartment = !profile.department || profile.department.trim() === "";
          const needsAssigned = !profile.assignedClasses || profile.assignedClasses.trim() === "";
          const needsSkills = !profile.skills || profile.skills.trim() === "";
          const needsInterests = !profile.interests || profile.interests.trim() === "";
          if (needsDepartment || needsAssigned || needsSkills || needsInterests) {
            setDepartment(profile.department ?? "");
            setAssignedClasses(profile.assignedClasses ?? "");
            setSkills(profile.skills ?? "");
            setInterests(profile.interests ?? "");
            setOpen(true);
          }
        }
      } catch (err) {
        // silently ignore; profile creation handled server-side
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [normalizedRole, userId]);

  if (!open || loading) {
    return null;
  }

  const handleSave = async () => {
    setError(null);
    setSaving(true);
    try {
      if (mode === "student" && studentProfile) {
        if (!studentCode.trim() || studentCode.toUpperCase().startsWith("AUTO-")) {
          setError("Register number is required.");
          setSaving(false);
          return;
        }
        if (!department.trim() || !section.trim()) {
          setError("Department and section are required.");
          setSaving(false);
          return;
        }
        await putJson(`/api/students/${studentProfile.id}`, {
          userId: studentProfile.userId,
          studentCode: studentCode.trim(),
          department: department.trim(),
          yearOfStudy,
          section: section.trim(),
          phone: studentProfile.phone ?? "",
          parentPhone: studentProfile.parentPhone ?? ""
        });
      } else if (mode === "staff" && staffProfile) {
        if (!department.trim()) {
          setError("Department is required.");
          setSaving(false);
          return;
        }
        if (!assignedClasses.trim()) {
          setError("Assigned sections are required.");
          setSaving(false);
          return;
        }
        if (!skills.trim()) {
          setError("Skills are required.");
          setSaving(false);
          return;
        }
        if (!interests.trim()) {
          setError("Interests are required.");
          setSaving(false);
          return;
        }
        await putJson(`/api/staff/${staffProfile.id}`, {
          userId: staffProfile.userId,
          staffCode: staffProfile.staffCode,
          department: department.trim(),
          designation: staffProfile.designation ?? "",
          phone: staffProfile.phone ?? "",
          assignedClasses: assignedClasses.trim(),
          skills: skills.trim(),
          interests: interests.trim()
        });
      }
      setOpen(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update profile");
    } finally {
      setSaving(false);
    }
  };

  const handleLater = () => {
    sessionStorage.setItem("profilePromptDismissed", "true");
    setOpen(false);
  };

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.97, y: 10 }}
            transition={{ duration: 0.25, ease: "easeOut" }}
            className="w-full max-w-md rounded-2xl border border-slate-200/70 bg-white/95 p-6 shadow-2xl backdrop-blur"
          >
            <h3 className="text-lg font-semibold text-slate-900">Complete your profile</h3>
            <p className="mt-1 text-sm text-slate-500">
              Add missing details so we can personalize your dashboard.
            </p>

            <div className="mt-4 space-y-3">
              <div>
                <label className="text-xs font-semibold text-slate-600">Department</label>
                <select
                  className="mt-1 h-10 w-full rounded-xl border border-border bg-background px-3 text-sm"
                  value={department}
                  onChange={(event) => setDepartment(event.target.value)}
                >
                  <option value="">Select</option>
                  {DEPARTMENTS.map((dept) => (
                    <option key={dept} value={dept}>
                      {dept}
                    </option>
                  ))}
                </select>
              </div>

              {mode === "student" && (
                <>
                  <div>
                    <label className="text-xs font-semibold text-slate-600">Register number</label>
                    <Input
                      value={studentCode}
                      onChange={(event) => setStudentCode(event.target.value)}
                      placeholder="Register number"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-slate-600">Section</label>
                    <select
                      className="mt-1 h-10 w-full rounded-xl border border-border bg-background px-3 text-sm"
                      value={section}
                      onChange={(event) => setSection(event.target.value)}
                    >
                      <option value="">Select</option>
                      {SECTIONS.map((sec) => (
                        <option key={sec} value={sec}>
                          {sec}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-slate-600">Year of study (optional)</label>
                    <Input
                      type="number"
                      min={1}
                      max={5}
                      value={yearOfStudy ?? ""}
                      onChange={(event) => setYearOfStudy(event.target.value ? Number(event.target.value) : null)}
                    />
                  </div>
                </>
              )}

              {mode === "staff" && (
                <>
                  <div>
                    <label className="text-xs font-semibold text-slate-600">Assigned sections (comma separated)</label>
                    <Input
                      value={assignedClasses}
                      onChange={(event) => setAssignedClasses(event.target.value)}
                      placeholder="CSE-A, AI&DS-C"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-slate-600">Skills</label>
                    <Input
                      value={skills}
                      onChange={(event) => setSkills(event.target.value)}
                      placeholder="AI, Data Science, Web"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-slate-600">Interests</label>
                    <Input
                      value={interests}
                      onChange={(event) => setInterests(event.target.value)}
                      placeholder="Hackathons, Research, Projects"
                    />
                  </div>
                </>
              )}

              {error && <p className="text-xs text-rose-600">{error}</p>}
            </div>

            <div className="mt-6 flex gap-3">
              <Button variant="secondary" onClick={handleLater} type="button">
                Later
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                {saving ? "Saving..." : "Save"}
              </Button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

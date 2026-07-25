import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getJson, postJson, putJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

// ─── Constants ────────────────────────────────────────────────────────────────

const DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"] as const;
const TIME_SLOTS = [
  { key: "AM", label: "Morning" },
  { key: "PM", label: "Afternoon" },
  { key: "EV", label: "Evening" },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

const parseAvailability = (value?: string | null) => {
  if (!value) return new Set<string>();
  return new Set(
    value
      .split(",")
      .map((item) => item.trim().toUpperCase())
      .filter(Boolean)
  );
};

const serializeAvailability = (value: Set<string>) =>
  Array.from(value).join(",");

// ─── AvailabilityGrid ─────────────────────────────────────────────────────────

type AvailabilityGridProps = {
  value?: string | null;
  onChange: (value: string) => void;
};

function AvailabilityGrid({ value, onChange }: AvailabilityGridProps) {
  const selected = useMemo(() => parseAvailability(value), [value]);

  const toggleSlot = (day: string, slotKey: string) => {
    const next = new Set(selected);
    const key = `${day.toUpperCase()}-${slotKey}`;
    if (next.has(key)) next.delete(key);
    else next.add(key);
    onChange(serializeAvailability(next));
  };

  return (
    <div className="rounded-xl border border-zinc-200 bg-zinc-50/60 p-3 dark:border-zinc-700/50 dark:bg-zinc-800/40">
      {/* Header row */}
      <div className="mb-2 grid grid-cols-[56px_repeat(3,minmax(0,1fr))] gap-1.5">
        <span />
        {TIME_SLOTS.map((slot) => (
          <span
            key={slot.key}
            className="text-center text-[10px] font-semibold uppercase tracking-widest text-zinc-400 dark:text-zinc-500"
          >
            {slot.label}
          </span>
        ))}
      </div>

      {/* Day rows */}
      {DAYS.map((day) => (
        <div
          key={day}
          className="mb-1.5 grid grid-cols-[56px_repeat(3,minmax(0,1fr))] items-center gap-1.5"
        >
          <span className="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">
            {day}
          </span>
          {TIME_SLOTS.map((slot) => {
            const key = `${day.toUpperCase()}-${slot.key}`;
            const active = selected.has(key);
            return (
              <button
                key={slot.key}
                type="button"
                onClick={() => toggleSlot(day, slot.key)}
                className={[
                  "rounded-lg border py-2 text-[10px] font-semibold tracking-wide transition-all duration-150",
                  active
                    ? "border-violet-400 bg-violet-500/15 text-violet-700 shadow-sm dark:border-violet-500/60 dark:bg-violet-500/20 dark:text-violet-300"
                    : "border-zinc-200 bg-white text-zinc-400 hover:border-violet-300 hover:bg-violet-50/60 hover:text-violet-600 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-500 dark:hover:border-violet-600/50 dark:hover:bg-violet-900/20 dark:hover:text-violet-400",
                ].join(" ")}
              >
                {active ? "✓" : "—"}
              </button>
            );
          })}
        </div>
      ))}
    </div>
  );
}

// ─── Types ────────────────────────────────────────────────────────────────────

type FacultyProfile = {
  id?: number;
  staffId?: number;
  staffName?: string;
  department?: string | null;
  skills?: string | null;
  proficiencyLevel?: "BEGINNER" | "INTERMEDIATE" | "EXPERT" | null;
  availability?: string | null;
  bio?: string | null;
};

type StudentPreferences = {
  id?: number;
  studentId?: number;
  studentName?: string;
  requiredSkills?: string | null;
  learningGoals?: string | null;
  mentorType?: string | null;
  availability?: string | null;
};

type MentorMatch = {
  mentorId: number;
  mentorName: string;
  mentorDepartment?: string | null;
  skills?: string | null;
  proficiencyLevel?: "BEGINNER" | "INTERMEDIATE" | "EXPERT" | null;
  availability?: string | null;
  bio?: string | null;
  score?: number | null;
};

type MentorRequest = {
  id: number;
  studentId: number;
  studentName: string;
  mentorId: number;
  mentorName: string;
  status: "PENDING" | "ACCEPTED" | "REJECTED";
  message?: string | null;
  requestedAt?: string | null;
  respondedAt?: string | null;
};

type MentorAnalytics = {
  mentorId: number;
  mentorName: string;
  department?: string | null;
  totalMatches: number;
  averageScore?: number | null;
  pendingRequests: number;
  acceptedRequests: number;
  rejectedRequests: number;
};

// ─── Status Badge ─────────────────────────────────────────────────────────────

function StatusBadge({ status }: { status: MentorRequest["status"] }) {
  const styles = {
    PENDING:
      "bg-amber-100 text-amber-700 border-amber-200 dark:bg-amber-400/10 dark:text-amber-300 dark:border-amber-400/20",
    ACCEPTED:
      "bg-emerald-100 text-emerald-700 border-emerald-200 dark:bg-emerald-400/10 dark:text-emerald-300 dark:border-emerald-400/20",
    REJECTED:
      "bg-red-100 text-red-600 border-red-200 dark:bg-red-400/10 dark:text-red-300 dark:border-red-400/20",
  };
  return (
    <span
      className={`rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider ${styles[status]}`}
    >
      {status}
    </span>
  );
}

// ─── Section Card ─────────────────────────────────────────────────────────────

function SectionCard({
  title,
  description,
  children,
  accent,
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
  accent?: string;
}) {
  return (
    <div
      className={[
        "rounded-2xl border bg-white shadow-sm dark:bg-zinc-900",
        accent
          ? `border-l-4 border-zinc-200 dark:border-zinc-700/70 ${accent}`
          : "border-zinc-200 dark:border-zinc-700/70",
      ].join(" ")}
    >
      <div className="border-b border-zinc-100 px-6 py-4 dark:border-zinc-800">
        <p className="text-[11px] font-semibold uppercase tracking-widest text-zinc-400 dark:text-zinc-500">
          {description}
        </p>
        <h3 className="mt-0.5 text-base font-bold text-zinc-900 dark:text-zinc-50">
          {title}
        </h3>
      </div>
      <div className="px-6 py-5">{children}</div>
    </div>
  );
}

// ─── Field ────────────────────────────────────────────────────────────────────

function Field({
  label,
  children,
  span2,
}: {
  label: string;
  children: React.ReactNode;
  span2?: boolean;
}) {
  return (
    <div className={`space-y-1.5 ${span2 ? "md:col-span-2" : ""}`}>
      <label className="block text-[11px] font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400">
        {label}
      </label>
      {children}
    </div>
  );
}

// ─── Shared input/textarea styles ─────────────────────────────────────────────

const inputCls =
  "h-10 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm text-zinc-800 placeholder:text-zinc-400 focus:border-violet-400 focus:outline-none focus:ring-2 focus:ring-violet-400/20 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500 dark:focus:border-violet-500 dark:focus:ring-violet-500/20 transition";

const textareaCls =
  "h-24 w-full rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 placeholder:text-zinc-400 focus:border-violet-400 focus:outline-none focus:ring-2 focus:ring-violet-400/20 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500 dark:focus:border-violet-500 dark:focus:ring-violet-500/20 transition resize-none";

const selectCls =
  "h-10 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm text-zinc-800 focus:border-violet-400 focus:outline-none focus:ring-2 focus:ring-violet-400/20 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:focus:border-violet-500 dark:focus:ring-violet-500/20 transition";

// ─── Main Component ───────────────────────────────────────────────────────────

export default function MentorMatching() {
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();

  const [facultyProfile, setFacultyProfile] = useState<FacultyProfile | null>(null);
  const [preferences, setPreferences] = useState<StudentPreferences | null>(null);
  const [matches, setMatches] = useState<MentorMatch[]>([]);
  const [requests, setRequests] = useState<MentorRequest[]>([]);
  const [analytics, setAnalytics] = useState<MentorAnalytics[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [requestNote, setRequestNote] = useState("");

  const [skillForm, setSkillForm] = useState({
    skills: "",
    proficiencyLevel: "INTERMEDIATE",
    availability: "",
    bio: "",
  });

  const [prefForm, setPrefForm] = useState({
    requiredSkills: "",
    learningGoals: "",
    mentorType: "",
    availability: "",
  });

  // ── Loaders ────────────────────────────────────────────────────────────────

  const loadFacultyProfile = async () => {
    try {
      const data = await getJson<FacultyProfile>("/api/faculty/profile");
      setFacultyProfile(data);
      setSkillForm({
        skills: data.skills || "",
        proficiencyLevel: data.proficiencyLevel || "INTERMEDIATE",
        availability: data.availability || "",
        bio: data.bio || "",
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load faculty profile");
    }
  };

  const loadStudentPreferences = async () => {
    try {
      const data = await getJson<StudentPreferences>("/api/student/preferences");
      setPreferences(data);
      setPrefForm({
        requiredSkills: data.requiredSkills || "",
        learningGoals: data.learningGoals || "",
        mentorType: data.mentorType || "",
        availability: data.availability || "",
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load preferences");
    }
  };

  const loadMatches = async () => {
    try {
      const data = await getJson<MentorMatch[]>("/api/student/matches");
      setMatches(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load matches");
    }
  };

  const loadRequests = async () => {
    try {
      const data = await getJson<MentorRequest[]>("/api/request");
      setRequests(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load requests");
    }
  };

  const loadAnalytics = async () => {
    try {
      const data = await getJson<MentorAnalytics[]>("/api/mentor/analytics");
      setAnalytics(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load mentor analytics");
    }
  };

  useEffect(() => {
    if (normalizedRole === "FACULTY" || normalizedRole === "STAFF") {
      loadFacultyProfile();
      loadRequests();
    }
    if (normalizedRole === "STUDENT") {
      loadStudentPreferences();
      loadMatches();
      loadRequests();
    }
    if (normalizedRole === "ADMIN") {
      loadAnalytics();
    }
  }, [normalizedRole]);

  // ── Handlers ───────────────────────────────────────────────────────────────

  const handleSaveSkills = async () => {
    setError(null);
    setMessage(null);
    try {
      const response = await postJson<FacultyProfile>("/api/faculty/skills", skillForm);
      setFacultyProfile(response);
      setMessage("Faculty skills updated.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save skills");
    }
  };

  const handleSavePreferences = async () => {
    setError(null);
    setMessage(null);
    try {
      const response = await postJson<StudentPreferences>("/api/student/preferences", prefForm);
      setPreferences(response);
      setMessage("Preferences saved. Updated mentor matches.");
      await loadMatches();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save preferences");
    }
  };

  const handleRequestMentor = async (mentorId: number) => {
    setError(null);
    setMessage(null);
    try {
      await postJson("/api/request", { mentorId, message: requestNote || null });
      setMessage("Mentor request sent.");
      setRequestNote("");
      await loadRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to send request");
    }
  };

  const handleUpdateRequest = async (
    requestId: number,
    status: MentorRequest["status"]
  ) => {
    setError(null);
    setMessage(null);
    try {
      await putJson("/api/request/status", { requestId, status });
      setMessage(`Request ${status.toLowerCase()}.`);
      await loadRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update request");
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="space-y-5">

      {/* ── Hero Banner ── */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-violet-600 via-violet-700 to-indigo-700 px-7 py-6 shadow-lg dark:from-violet-700 dark:via-violet-800 dark:to-indigo-900">
        {/* decorative circles */}
        <div className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full bg-white/5" />
        <div className="pointer-events-none absolute -bottom-8 right-24 h-32 w-32 rounded-full bg-white/5" />
        <p className="text-[11px] font-semibold uppercase tracking-widest text-violet-200">
          Peer Mentor Matching
        </p>
        <h1 className="mt-1 text-2xl font-bold text-white">
          Find the best mentor fit
        </h1>
        <p className="mt-1 text-sm text-violet-200/80">
          Match mentors and mentees using skills, availability, and experience.
        </p>
      </div>

      {/* ── Alerts ── */}
      {message && (
        <div className="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700 dark:border-emerald-500/25 dark:bg-emerald-500/10 dark:text-emerald-300">
          <span className="shrink-0 text-base">✓</span>
          {message}
        </div>
      )}
      {error && (
        <div className="flex items-center gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-500/25 dark:bg-red-500/10 dark:text-red-300">
          <span className="shrink-0 text-base">✕</span>
          {error}
        </div>
      )}

      {/* ══════════════════════════════════════════════
          FACULTY / STAFF — Mentor Profile
      ══════════════════════════════════════════════ */}
      {(normalizedRole === "FACULTY" || normalizedRole === "STAFF") && (
        <SectionCard
          title="Your Mentor Profile"
          description="Mentor profile"
          accent="border-l-violet-400 dark:border-l-violet-600"
        >
          <div className="grid gap-4 md:grid-cols-2">
            <Field label="Skills (comma separated)" span2>
              <input
                className={inputCls}
                value={skillForm.skills}
                onChange={(e) =>
                  setSkillForm((p) => ({ ...p, skills: e.target.value }))
                }
                placeholder="AI, DSA, Java, Interview prep"
              />
            </Field>

            <Field label="Proficiency Level">
              <select
                className={selectCls}
                value={skillForm.proficiencyLevel}
                onChange={(e) =>
                  setSkillForm((p) => ({ ...p, proficiencyLevel: e.target.value }))
                }
              >
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="EXPERT">Expert</option>
              </select>
            </Field>

            <Field label="Availability">
              <AvailabilityGrid
                value={skillForm.availability}
                onChange={(v) =>
                  setSkillForm((p) => ({ ...p, availability: v }))
                }
              />
            </Field>

            <Field label="Short Bio" span2>
              <textarea
                className={textareaCls}
                value={skillForm.bio}
                onChange={(e) =>
                  setSkillForm((p) => ({ ...p, bio: e.target.value }))
                }
                placeholder="Share what you can help with."
              />
            </Field>

            <div className="md:col-span-2">
              <button
                onClick={handleSaveSkills}
                className="rounded-lg bg-violet-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-violet-700 active:scale-[0.98] dark:bg-violet-600 dark:hover:bg-violet-500 transition"
              >
                Save Profile
              </button>
            </div>
          </div>
        </SectionCard>
      )}

      {/* ══════════════════════════════════════════════
          STUDENT — Preferences
      ══════════════════════════════════════════════ */}
      {normalizedRole === "STUDENT" && (
        <SectionCard
          title="Your Preferences"
          description="Student preferences"
          accent="border-l-indigo-400 dark:border-l-indigo-500"
        >
          <div className="grid gap-4 md:grid-cols-2">
            <Field label="Required Skills (comma separated)" span2>
              <input
                className={inputCls}
                value={prefForm.requiredSkills}
                onChange={(e) =>
                  setPrefForm((p) => ({ ...p, requiredSkills: e.target.value }))
                }
                placeholder="React, Data Structures, Resume tips"
              />
            </Field>

            <Field label="Learning Goals">
              <input
                className={inputCls}
                value={prefForm.learningGoals}
                onChange={(e) =>
                  setPrefForm((p) => ({ ...p, learningGoals: e.target.value }))
                }
                placeholder="Crack interviews by June"
              />
            </Field>

            <Field label="Preferred Mentor Type">
              <input
                className={inputCls}
                value={prefForm.mentorType}
                onChange={(e) =>
                  setPrefForm((p) => ({ ...p, mentorType: e.target.value }))
                }
                placeholder="Expert / Industry / Friendly"
              />
            </Field>

            <Field label="Availability" span2>
              <AvailabilityGrid
                value={prefForm.availability}
                onChange={(v) =>
                  setPrefForm((p) => ({ ...p, availability: v }))
                }
              />
            </Field>

            <div className="md:col-span-2">
              <button
                onClick={handleSavePreferences}
                className="rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 active:scale-[0.98] dark:bg-indigo-600 dark:hover:bg-indigo-500 transition"
              >
                Save Preferences
              </button>
            </div>
          </div>
        </SectionCard>
      )}

      {/* ══════════════════════════════════════════════
          STUDENT — Recommended Mentors
      ══════════════════════════════════════════════ */}
      {normalizedRole === "STUDENT" && (
        <SectionCard title="Recommended Mentors" description="Top matches">
          {matches.length === 0 ? (
            <p className="text-sm text-zinc-400 dark:text-zinc-500">
              No mentor matches yet. Save your preferences to get started.
            </p>
          ) : (
            <div className="space-y-3">
              {matches.map((mentor) => (
                <div
                  key={mentor.mentorId}
                  className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-700/60 dark:bg-zinc-800/50"
                >
                  {/* top row */}
                  <div className="flex flex-wrap items-start justify-between gap-2">
                    <div>
                      <p className="font-bold text-zinc-900 dark:text-zinc-50">
                        {mentor.mentorName}
                      </p>
                      <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-400">
                        {mentor.mentorDepartment || "Department"} ·{" "}
                        <span className="capitalize lowercase">
                          {mentor.proficiencyLevel || "Intermediate"}
                        </span>
                      </p>
                    </div>
                    {/* score pill */}
                    <div className="flex items-center gap-1.5 rounded-full bg-violet-100 px-3 py-1 dark:bg-violet-500/15">
                      <span className="text-[10px] font-semibold uppercase tracking-wider text-violet-600 dark:text-violet-300">
                        Match
                      </span>
                      <span className="text-sm font-extrabold text-violet-700 dark:text-violet-300">
                        {mentor.score ?? 0}
                      </span>
                    </div>
                  </div>

                  {/* skills */}
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {(mentor.skills || "Not provided")
                      .split(",")
                      .map((s) => s.trim())
                      .filter(Boolean)
                      .map((skill) => (
                        <span
                          key={skill}
                          className="rounded-md bg-zinc-200/70 px-2 py-0.5 text-[11px] font-medium text-zinc-600 dark:bg-zinc-700 dark:text-zinc-300"
                        >
                          {skill}
                        </span>
                      ))}
                  </div>

                  {/* bio */}
                  {mentor.bio && (
                    <p className="mt-2 text-xs leading-relaxed text-zinc-500 dark:text-zinc-400">
                      {mentor.bio}
                    </p>
                  )}

                  {/* request row */}
                  <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-zinc-200 pt-3 dark:border-zinc-700/50">
                    <input
                      className={`${inputCls} max-w-xs`}
                      value={requestNote}
                      onChange={(e) => setRequestNote(e.target.value)}
                      placeholder="Add a short note (optional)"
                    />
                    <button
                      onClick={() => handleRequestMentor(mentor.mentorId)}
                      className="rounded-lg bg-violet-600 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-violet-700 active:scale-[0.98] dark:bg-violet-600 dark:hover:bg-violet-500 transition whitespace-nowrap"
                    >
                      Request Mentor
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </SectionCard>
      )}

      {/* ══════════════════════════════════════════════
          ADMIN — Analytics
      ══════════════════════════════════════════════ */}
      {normalizedRole === "ADMIN" && (
        <SectionCard title="Mentor Analytics" description="Admin overview">
          {analytics.length === 0 ? (
            <p className="text-sm text-zinc-400 dark:text-zinc-500">
              No analytics available yet.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr>
                    {["Mentor", "Department", "Matches", "Avg Score", "Pending", "Accepted", "Rejected"].map(
                      (h) => (
                        <th
                          key={h}
                          className="pb-3 pr-4 text-left text-[10px] font-bold uppercase tracking-widest text-zinc-400 dark:text-zinc-500"
                        >
                          {h}
                        </th>
                      )
                    )}
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
                  {analytics.map((mentor) => (
                    <tr
                      key={mentor.mentorId}
                      className="text-zinc-700 hover:bg-zinc-50 dark:text-zinc-300 dark:hover:bg-zinc-800/50 transition"
                    >
                      <td className="py-2.5 pr-4 font-medium text-zinc-900 dark:text-zinc-100">
                        {mentor.mentorName}
                      </td>
                      <td className="py-2.5 pr-4 text-zinc-500 dark:text-zinc-400">
                        {mentor.department || "—"}
                      </td>
                      <td className="py-2.5 pr-4">{mentor.totalMatches}</td>
                      <td className="py-2.5 pr-4">{mentor.averageScore ?? "—"}</td>
                      <td className="py-2.5 pr-4">
                        <span className="rounded-md bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-700 dark:bg-amber-400/10 dark:text-amber-300">
                          {mentor.pendingRequests}
                        </span>
                      </td>
                      <td className="py-2.5 pr-4">
                        <span className="rounded-md bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700 dark:bg-emerald-400/10 dark:text-emerald-300">
                          {mentor.acceptedRequests}
                        </span>
                      </td>
                      <td className="py-2.5">
                        <span className="rounded-md bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-600 dark:bg-red-400/10 dark:text-red-300">
                          {mentor.rejectedRequests}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </SectionCard>
      )}

      {/* ══════════════════════════════════════════════
          FACULTY / STAFF — Incoming Requests
      ══════════════════════════════════════════════ */}
      {(normalizedRole === "FACULTY" || normalizedRole === "STAFF") && (
        <SectionCard title="Mentor Requests" description="Incoming requests">
          {requests.length === 0 ? (
            <p className="text-sm text-zinc-400 dark:text-zinc-500">
              No requests yet.
            </p>
          ) : (
            <div className="space-y-2.5">
              {requests.map((request) => (
                <div
                  key={request.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-3 dark:border-zinc-700/60 dark:bg-zinc-800/50"
                >
                  <div>
                    <p className="font-semibold text-zinc-900 dark:text-zinc-50">
                      {request.studentName}
                    </p>
                    <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-400">
                      {request.message || "No message provided"}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <StatusBadge status={request.status} />
                    {request.status === "PENDING" && (
                      <>
                        <button
                          onClick={() => handleUpdateRequest(request.id, "REJECTED")}
                          className="rounded-lg border border-zinc-300 bg-white px-3 py-1.5 text-xs font-semibold text-zinc-600 hover:bg-zinc-100 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700 transition"
                        >
                          Reject
                        </button>
                        <button
                          onClick={() => handleUpdateRequest(request.id, "ACCEPTED")}
                          className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-500 transition"
                        >
                          Accept
                        </button>
                      </>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </SectionCard>
      )}

      {/* ══════════════════════════════════════════════
          STUDENT — My Requests
      ══════════════════════════════════════════════ */}
      {normalizedRole === "STUDENT" && (
        <SectionCard title="Your Requests" description="Request tracker">
          {requests.length === 0 ? (
            <p className="text-sm text-zinc-400 dark:text-zinc-500">
              No mentor requests yet.
            </p>
          ) : (
            <div className="space-y-2.5">
              {requests.map((request) => (
                <div
                  key={request.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-3 dark:border-zinc-700/60 dark:bg-zinc-800/50"
                >
                  <div>
                    <p className="font-semibold text-zinc-900 dark:text-zinc-50">
                      {request.mentorName}
                    </p>
                    {request.requestedAt && (
                      <p className="mt-0.5 text-xs text-zinc-400 dark:text-zinc-500">
                        {new Date(request.requestedAt).toLocaleDateString(
                          undefined,
                          { year: "numeric", month: "short", day: "numeric" }
                        )}
                      </p>
                    )}
                  </div>
                  <StatusBadge status={request.status} />
                </div>
              ))}
            </div>
          )}
        </SectionCard>
      )}
    </div>
  );
}

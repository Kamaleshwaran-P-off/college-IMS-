import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Upload, BarChart3, TrendingUp, Users, MousePointerClick,
  Bookmark, Star, Calendar, Clock, CheckCircle2, AlertCircle,
  Loader2, ChevronRight, Megaphone, FileImage, FileText,
  Building2, Sparkles
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import Carousel from "@/components/Carousel";
import { API_BASE_URL, getAuthHeaders, getJson, readErrorMessage } from "@/lib/api";
import MarksChart from "@/components/Charts/MarksChart";

/* ─── Types ───────────────────────────────────────────────────────────── */
type DomainStat = { domain: string; count: number };
type OpportunityStat = { id: number; title: string; saves: number };

type AdminHappenstanceAnalytics = {
  topClickedDomains: DomainStat[];
  mostSavedDomains: DomainStat[];
  mostSavedOpportunities: OpportunityStat[];
  totalClicks: number;
  totalSaves: number;
  activeUsers: number;
  averageSerendipityScore: number;
};

type UploadStatus = "idle" | "uploading" | "success" | "error";

/* ─── Framer variants ─────────────────────────────────────────────────── */
const cardVariants = {
  hidden: { opacity: 0, y: 24, scale: 0.96 },
  visible: (i: number) => ({
    opacity: 1, y: 0, scale: 1,
    transition: { delay: i * 0.08, duration: 0.45, type: "spring", stiffness: 110 },
  }),
};

const fadeUp = {
  hidden: { opacity: 0, y: 14 },
  visible: (i = 0) => ({ opacity: 1, y: 0, transition: { delay: i * 0.06, duration: 0.38 } }),
};

/* ─── Stat Card ───────────────────────────────────────────────────────── */
interface StatCardProps {
  label: string;
  value: string | number;
  sub?: string;
  icon: React.ElementType;
  gradient: string;
  index: number;
}

function StatCard({ label, value, sub, icon: Icon, gradient, index }: StatCardProps) {
  return (
    <motion.div custom={index} variants={cardVariants} initial="hidden" animate="visible">
      <Card className="
        relative overflow-hidden group cursor-default
        bg-white dark:bg-white/[0.06]
        border border-slate-200/80 dark:border-white/10
        hover:border-slate-300 dark:hover:border-white/20
        shadow-sm hover:shadow-md dark:shadow-none
        transition-all duration-300
      ">
        <div className={`absolute top-0 left-0 right-0 h-[2px] ${gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-300`} />
        <CardContent className="p-4">
          <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center mb-3 shadow-sm transition-transform duration-300 group-hover:scale-110`}>
            <Icon className="w-5 h-5 text-white" />
          </div>
          <p className="text-2xl font-bold text-slate-900 dark:text-white leading-none">{value}</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1.5 font-medium">{label}</p>
          {sub && <p className="text-[10px] text-slate-400 dark:text-slate-500 mt-0.5">{sub}</p>}
        </CardContent>
      </Card>
    </motion.div>
  );
}

/* ─── Upload Card ─────────────────────────────────────────────────────── */
interface UploadCardProps {
  title: string;
  description: string;
  icon: React.ElementType;
  accentColor: string;
  status: UploadStatus;
  message: string | null;
  onUpload: () => void;
  children: React.ReactNode;
  disabled: boolean;
}

function UploadCard({ title, description, icon: Icon, accentColor, status, message, onUpload, children, disabled }: UploadCardProps) {
  return (
    <Card className="
      bg-white dark:bg-white/[0.06]
      border border-slate-200/80 dark:border-white/10
      shadow-sm hover:shadow-md dark:shadow-none
      transition-all duration-300
    ">
      <CardHeader className="pb-3">
        <div className="flex items-center gap-3">
          <div className={`w-9 h-9 rounded-xl bg-gradient-to-br ${accentColor} flex items-center justify-center shadow-sm`}>
            <Icon className="w-4 h-4 text-white" />
          </div>
          <div>
            <CardTitle className="text-sm text-slate-800 dark:text-slate-100">{title}</CardTitle>
            <CardDescription className="text-xs mt-0.5">{description}</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-3">
        {children}

        <button
          type="button"
          onClick={onUpload}
          disabled={disabled || status === "uploading"}
          className={`
            w-full flex items-center justify-center gap-2
            rounded-xl px-4 py-2.5 text-sm font-semibold
            transition-all duration-200
            bg-gradient-to-r ${accentColor} text-white
            hover:opacity-90 hover:shadow-md
            disabled:opacity-40 disabled:cursor-not-allowed disabled:shadow-none
          `}
        >
          {status === "uploading" ? (
            <><Loader2 className="w-4 h-4 animate-spin" /> Uploading...</>
          ) : (
            <><Upload className="w-4 h-4" /> {title.includes("Calendar") ? "Upload Calendar" : "Upload Timetable"}</>
          )}
        </button>

        <AnimatePresence>
          {message && (
            <motion.div
              initial={{ opacity: 0, y: -6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              className={`flex items-center gap-2 rounded-xl px-3 py-2.5 text-xs font-medium ${
                status === "error"
                  ? "bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400 border border-red-200/60 dark:border-red-500/20"
                  : "bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-200/60 dark:border-emerald-500/20"
              }`}
            >
              {status === "error"
                ? <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                : <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
              }
              {message}
            </motion.div>
          )}
        </AnimatePresence>
      </CardContent>
    </Card>
  );
}

/* ─── File Input ──────────────────────────────────────────────────────── */
function FileInput({ onChange, accept, file }: { onChange: (f: File | null) => void; accept: string; file: File | null }) {
  return (
    <label className="
      relative flex flex-col items-center justify-center gap-2
      rounded-xl border-2 border-dashed
      border-slate-200 dark:border-white/15
      hover:border-slate-300 dark:hover:border-white/25
      bg-slate-50/50 dark:bg-white/[0.03]
      hover:bg-slate-50 dark:hover:bg-white/[0.05]
      p-4 cursor-pointer transition-all duration-200
    ">
      <input
        type="file"
        accept={accept}
        className="sr-only"
        onChange={(e) => onChange(e.target.files?.[0] || null)}
      />
      {file ? (
        <>
          <CheckCircle2 className="w-6 h-6 text-emerald-500" />
          <p className="text-xs font-medium text-slate-700 dark:text-slate-300 text-center truncate max-w-full">{file.name}</p>
          <p className="text-[10px] text-slate-400 dark:text-slate-500">Click to change</p>
        </>
      ) : (
        <>
          <Upload className="w-6 h-6 text-slate-400 dark:text-slate-500" />
          <p className="text-xs font-medium text-slate-600 dark:text-slate-400">Click to choose file</p>
          <p className="text-[10px] text-slate-400 dark:text-slate-500">PDF or image</p>
        </>
      )}
    </label>
  );
}

/* ─── Main Component ──────────────────────────────────────────────────── */
export default function AdminDashboard() {
  const [calendarFile, setCalendarFile] = useState<File | null>(null);
  const [timetableFile, setTimetableFile] = useState<File | null>(null);
  const [department, setDepartment] = useState("");
  const [section, setSection] = useState("")
  const [calendarStatus, setCalendarStatus] = useState<UploadStatus>("idle");
  const [timetableStatus, setTimetableStatus] = useState<UploadStatus>("idle");
  const [calendarMessage, setCalendarMessage] = useState<string | null>(null);
  const [timetableMessage, setTimetableMessage] = useState<string | null>(null);
  const [happenstanceAnalytics, setHappenstanceAnalytics] = useState<AdminHappenstanceAnalytics | null>(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(true);
  const [analyticsError, setAnalyticsError] = useState<string | null>(null);

  const authHeaders = getAuthHeaders();

  useEffect(() => {
    let active = true;
    setAnalyticsLoading(true);
    (async () => {
      try {
        const data = await getJson<AdminHappenstanceAnalytics>("/api/happenstance/admin/analytics");
        if (active) setHappenstanceAnalytics(data);
      } catch (err) {
        if (active) {
          setHappenstanceAnalytics(null);
          setAnalyticsError(err instanceof Error ? err.message : "Failed to load analytics.");
        }
      } finally {
        if (active) setAnalyticsLoading(false);
      }
    })();
    return () => { active = false; };
  }, []);

  const uploadCalendar = async () => {
    if (!calendarFile) return;
    setCalendarStatus("uploading");
    setCalendarMessage(null);
    const formData = new FormData();
    formData.append("file", calendarFile);
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/upload-calendar`, {
        method: "POST",
        headers: authHeaders,
        body: formData,
      });
      if (!response.ok) throw new Error(await readErrorMessage(response));
      setCalendarStatus("success");
      setCalendarMessage("Academic calendar uploaded successfully.");
      setCalendarFile(null);
    } catch (error) {
      setCalendarStatus("error");
      setCalendarMessage(error instanceof Error ? error.message : "Upload failed");
    }
  };

  const uploadTimetable = async () => {
    if (!timetableFile || !department || !section) return;
    setTimetableStatus("uploading");
    setTimetableMessage(null);
    const formData = new FormData();
    formData.append("file", timetableFile);
    formData.append("department", department);
    formData.append("section", section);
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/upload-timetable`, {
        method: "POST",
        headers: authHeaders,
        body: formData,
      });
      if (!response.ok) throw new Error(await readErrorMessage(response));
      setTimetableStatus("success");
      setTimetableMessage("Timetable uploaded successfully.");
      setTimetableFile(null);
    } catch (error) {
      setTimetableStatus("error");
      setTimetableMessage(error instanceof Error ? error.message : "Upload failed");
    }
  };

  /* ── Stat cards data ── */
  const stats: StatCardProps[] = happenstanceAnalytics ? [
    { label: "Total Clicks", value: happenstanceAnalytics.totalClicks.toLocaleString(), icon: MousePointerClick, gradient: "from-indigo-500 to-blue-500", index: 0 },
    { label: "Total Saves", value: happenstanceAnalytics.totalSaves.toLocaleString(), icon: Bookmark, gradient: "from-violet-500 to-purple-500", index: 1 },
    { label: "Active Users", value: happenstanceAnalytics.activeUsers.toLocaleString(), sub: `Avg serendipity: ${happenstanceAnalytics.averageSerendipityScore.toFixed(1)}`, icon: Users, gradient: "from-emerald-500 to-teal-500", index: 2 },
  ] : [];

  const inputClass = `
    w-full rounded-xl border border-slate-200 dark:border-white/10
    bg-white dark:bg-white/[0.06]
    text-slate-800 dark:text-white
    placeholder:text-slate-400 dark:placeholder:text-slate-500
    px-3 py-2 text-sm
    focus:outline-none focus:ring-2 focus:ring-indigo-500/40 dark:focus:ring-indigo-400/30
    transition-all duration-200
  `;

  return (
    <div className="space-y-6 pb-10">

      {/* ── Hero header ── */}
      <motion.div
        initial={{ opacity: 0, y: -16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="relative overflow-hidden rounded-2xl
          bg-gradient-to-br from-rose-600 via-red-600 to-orange-600
          dark:from-rose-700 dark:via-red-700 dark:to-orange-700
          p-6 shadow-lg shadow-red-500/20 dark:shadow-red-900/30"
      >
        <div className="absolute -top-10 -right-10 w-48 h-48 rounded-full bg-white/10 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-8 -left-8 w-36 h-36 rounded-full bg-orange-300/20 blur-2xl pointer-events-none" />
        <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Building2 className="w-4 h-4 text-red-200" />
              <p className="text-red-200 text-sm font-medium">Admin Control Panel</p>
            </div>
            <h1 className="text-2xl md:text-3xl font-bold text-white tracking-tight">
              Academic Resources &amp; Analytics
            </h1>
            <p className="text-red-200/80 text-sm mt-1">Upload calendars, timetables and monitor the Happenstance engine.</p>
          </div>
          <div className="flex items-center gap-2 bg-white/15 backdrop-blur-sm border border-white/20 rounded-xl px-4 py-2.5">
            <Sparkles className="w-4 h-4 text-amber-300" />
            <span className="text-white text-sm font-medium">System Active</span>
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse ml-1" />
          </div>
        </div>
      </motion.div>

      {/* ── Carousel ── */}
      <Carousel />

      {/* ── Analytics section ── */}
      <motion.div custom={0} variants={fadeUp} initial="hidden" animate="visible">
        <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm">
          <CardHeader className="pb-3">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center">
                <Sparkles className="w-4 h-4 text-white" />
              </div>
              <div>
                <CardTitle className="text-sm text-slate-800 dark:text-slate-100">Happenstance Engine</CardTitle>
                <CardDescription className="text-xs mt-0.5">Opportunity Discovery Analytics</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-5">

            {/* Error */}
            <AnimatePresence>
              {analyticsError && (
                <motion.div
                  initial={{ opacity: 0, y: -6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
                  className="flex items-center gap-2 rounded-xl px-4 py-3 text-sm
                    bg-rose-50 dark:bg-rose-500/10
                    text-rose-600 dark:text-rose-400
                    border border-rose-200/60 dark:border-rose-500/20"
                >
                  <AlertCircle className="w-4 h-4 shrink-0" /> {analyticsError}
                </motion.div>
              )}
            </AnimatePresence>

            {/* Loading skeleton */}
            {analyticsLoading && !analyticsError && (
              <div className="space-y-3 animate-pulse">
                <div className="grid gap-3 md:grid-cols-3">
                  {[0,1,2].map(i => <div key={i} className="h-24 rounded-xl bg-slate-100 dark:bg-white/[0.06]" />)}
                </div>
                <div className="h-56 rounded-xl bg-slate-100 dark:bg-white/[0.06]" />
              </div>
            )}

            {/* Stat cards */}
            {!analyticsLoading && happenstanceAnalytics && (
              <>
                <div className="grid gap-3 md:grid-cols-3">
                  {stats.map((s) => <StatCard key={s.label} {...s} />)}
                </div>

                {/* Charts */}
                <div className="grid gap-4 lg:grid-cols-2">
                  <motion.div custom={1} variants={fadeUp} initial="hidden" animate="visible">
                    <Card className="bg-slate-50/80 dark:bg-white/[0.04] border border-slate-100 dark:border-white/8 shadow-none">
                      <CardHeader className="pb-2">
                        <CardTitle className="text-xs flex items-center gap-2 text-slate-600 dark:text-slate-300">
                          <MousePointerClick className="w-3.5 h-3.5 text-indigo-500" /> Top Clicked Domains
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="h-56">
                        <MarksChart
                          labels={happenstanceAnalytics.topClickedDomains.map((s) => s.domain)}
                          datasets={[{ label: "Clicks", data: happenstanceAnalytics.topClickedDomains.map((s) => s.count), color: "rgba(99,102,241,0.75)" }]}
                          height={220}
                        />
                      </CardContent>
                    </Card>
                  </motion.div>

                  <motion.div custom={2} variants={fadeUp} initial="hidden" animate="visible">
                    <Card className="bg-slate-50/80 dark:bg-white/[0.04] border border-slate-100 dark:border-white/8 shadow-none">
                      <CardHeader className="pb-2">
                        <CardTitle className="text-xs flex items-center gap-2 text-slate-600 dark:text-slate-300">
                          <Bookmark className="w-3.5 h-3.5 text-emerald-500" /> Most Saved Domains
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="h-56">
                        <MarksChart
                          labels={happenstanceAnalytics.mostSavedDomains.map((s) => s.domain)}
                          datasets={[{ label: "Saves", data: happenstanceAnalytics.mostSavedDomains.map((s) => s.count), color: "rgba(16,185,129,0.75)" }]}
                          height={220}
                        />
                      </CardContent>
                    </Card>
                  </motion.div>
                </div>

                {/* Most saved opportunities */}
                <motion.div custom={3} variants={fadeUp} initial="hidden" animate="visible">
                  <Card className="bg-slate-50/80 dark:bg-white/[0.04] border border-slate-100 dark:border-white/8 shadow-none">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-xs flex items-center gap-2 text-slate-600 dark:text-slate-300">
                        <Star className="w-3.5 h-3.5 text-amber-500" /> Most Saved Opportunities
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-2">
                      {happenstanceAnalytics.mostSavedOpportunities.map((item, i) => (
                        <motion.div
                          key={`opp-${item.id}`}
                          custom={i}
                          variants={fadeUp}
                          initial="hidden"
                          animate="visible"
                          className="flex items-center justify-between
                            rounded-xl px-3 py-2.5
                            bg-white dark:bg-white/[0.06]
                            border border-slate-100 dark:border-white/8
                            hover:border-slate-200 dark:hover:border-white/15
                            transition-colors duration-150"
                        >
                          <div className="flex items-center gap-2.5">
                            <div className="w-6 h-6 rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white text-[10px] font-bold shrink-0">
                              {i + 1}
                            </div>
                            <p className="text-sm font-medium text-slate-800 dark:text-white">{item.title}</p>
                          </div>
                          <Badge className="bg-amber-50 dark:bg-amber-500/15 text-amber-700 dark:text-amber-300 border-0 text-[10px] font-semibold">
                            {item.saves} saves
                          </Badge>
                        </motion.div>
                      ))}
                    </CardContent>
                  </Card>
                </motion.div>
              </>
            )}
          </CardContent>
        </Card>
      </motion.div>

      {/* ── Upload section ── */}
      <div>
        <motion.div custom={4} variants={fadeUp} initial="hidden" animate="visible" className="flex items-center gap-2 mb-4">
          <Upload className="w-4 h-4 text-slate-500 dark:text-slate-400" />
          <h2 className="text-sm font-semibold text-slate-700 dark:text-slate-200 uppercase tracking-wide">File Uploads</h2>
        </motion.div>

        <div className="grid gap-4 lg:grid-cols-2">
          {/* Academic Calendar */}
          <motion.div custom={5} variants={fadeUp} initial="hidden" animate="visible">
            <UploadCard
              title="Academic Calendar"
              description="Upload the official academic calendar (PDF or image)"
              icon={Calendar}
              accentColor="from-indigo-500 to-blue-600"
              status={calendarStatus}
              message={calendarMessage}
              onUpload={uploadCalendar}
              disabled={!calendarFile}
            >
              <FileInput
                file={calendarFile}
                accept=".pdf,image/*"
                onChange={setCalendarFile}
              />
            </UploadCard>
          </motion.div>

          {/* Timetable */}
          <motion.div custom={6} variants={fadeUp} initial="hidden" animate="visible">
            <UploadCard
              title="Timetable Upload"
              description="Department and section-wise timetable (PDF or image)"
              icon={Clock}
              accentColor="from-violet-500 to-purple-600"
              status={timetableStatus}
              message={timetableMessage}
              onUpload={uploadTimetable}
              disabled={!timetableFile || !department || !section}
            >
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="text"
                  placeholder="Department (e.g. CSE)"
                  value={department}
                  onChange={(e) => setDepartment(e.target.value)}
                  className={inputClass}
                />
                <input
                  type="text"
                  placeholder="Section (e.g. A)"
                  value={section}
                  onChange={(e) => setSection(e.target.value)}
                  className={inputClass}
                />
              </div>
              <FileInput
                file={timetableFile}
                accept=".pdf,image/*"
                onChange={setTimetableFile}
              />
            </UploadCard>
          </motion.div>
        </div>
      </div>

    </div>
  );
}

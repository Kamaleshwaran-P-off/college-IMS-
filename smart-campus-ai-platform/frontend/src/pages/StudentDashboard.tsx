import React, { useState, useMemo, useEffect, useRef } from 'react';
import { 
  LineChart, 
  Line, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  Cell,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Legend
} from 'recharts';
import { 
  BookOpen, 
  CheckCircle2, 
  Clock, 
  AlertTriangle, 
  TrendingUp, 
  Target, 
  Zap,
  Calendar,
  Award,
  Brain,
  GraduationCap,
  BarChart3,
  ChevronRight,
  Send,
  X,
  Bot,
  Sparkles,
  Megaphone,
  Clock as ClockIcon
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

/* ─── Types ───────────────────────────────────────────────────────────── */
interface PerformanceData {
  week: string;
  score: number;
}

interface SubjectData {
  subject: string;
  score: number;
}

interface AssignmentStats {
  total: number;
  completed: number;
  pending: number;
}

interface ProductivityData {
  studyHours: number;
  tasksCompleted: number;
  streak: number;
}

interface ChatMessage {
  role: "user" | "ai";
  text: string;
}

interface MentorMatch {
  mentorId: number;
  mentorName: string;
  mentorDepartment?: string | null;
  skills?: string | null;
  proficiencyLevel?: string | null;
  score?: number | null;
}

/* ─── Mock Data ───────────────────────────────────────────────────────── */
const performanceData: PerformanceData[] = [
  { week: 'Week 1', score: 60 },
  { week: 'Week 2', score: 70 },
  { week: 'Week 3', score: 80 },
  { week: 'Week 4', score: 75 },
  { week: 'Week 5', score: 85 },
  { week: 'Week 6', score: 88 },
];

const subjectData: SubjectData[] = [
  { subject: 'Operating System', score: 72 },
  { subject: 'Machine Learning', score: 85 },
  { subject: 'Data Analytics', score: 68 },
  { subject: 'DAA', score: 78 },
];

const assignmentStats: AssignmentStats = {
  total: 12,
  completed: 8,
  pending: 4,
};

const productivityData: ProductivityData = {
  studyHours: 42,
  tasksCompleted: 24,
  streak: 7,
};

const internalMarksData = {
  labels: ["AI Systems", "DSA", "Math", "Networks"],
  cat1: [78, 72, 84, 75], 
  cat2: [82, 76, 88, 79], 
  cat3: [85, 80, 90, 82],
};

const attendanceData = [
  { label: "Mon", percent: 92 }, 
  { label: "Tue", percent: 88 },
  { label: "Wed", percent: 86 }, 
  { label: "Thu", percent: 90 },
  { label: "Fri", percent: 84 }, 
  { label: "Sat", percent: 94 },
];

const suggestions = [
  { id: 1, text: 'Revise Operating Systems - Score below 75%', icon: BookOpen, priority: 'high' },
  { id: 2, text: 'Complete 4 pending assignments', icon: CheckCircle2, priority: 'medium' },
  { id: 3, text: 'Practice Data Analytics quizzes daily', icon: Target, priority: 'high' },
  { id: 4, text: 'Maintain your 7-day study streak!', icon: Zap, priority: 'low' },
];

const mentorMatches: MentorMatch[] = [
  { mentorId: 1, mentorName: "Dr. Sarah Chen", mentorDepartment: "Computer Science", skills: "AI, ML, Data Science", proficiencyLevel: "Expert", score: 95 },
  { mentorId: 2, mentorName: "Prof. James Wilson", mentorDepartment: "Mathematics", skills: "Algorithms, DSA", proficiencyLevel: "Advanced", score: 88 },
];

/* ─── Helper Functions ─────────────────────────────────────────────────── */
const calculateRiskLevel = (scores: number[]): { level: string; color: string; bg: string; border: string; message: string } => {
  const avg = scores.reduce((a, b) => a + b, 0) / scores.length;
  
  if (avg < 50) {
    return {
      level: 'HIGH RISK',
      color: 'text-red-500',
      bg: 'bg-red-500',
      border: 'border-red-200',
      message: '⚠️ Critical attention needed. Immediate intervention recommended.'
    };
  } else if (avg < 70) {
    return {
      level: 'MEDIUM RISK',
      color: 'text-orange-500',
      bg: 'bg-orange-500',
      border: 'border-orange-200',
      message: '⚠️ You are at Medium Risk. Focus on weak subjects.'
    };
  } else {
    return {
      level: 'LOW RISK',
      color: 'text-emerald-500',
      bg: 'bg-emerald-500',
      border: 'border-emerald-200',
      message: '✅ Great performance! Keep maintaining your consistency.'
    };
  }
};

const getPriorityColor = (priority: string): string => {
  switch (priority) {
    case 'high': return 'border-l-red-500 bg-red-50';
    case 'medium': return 'border-l-orange-500 bg-orange-50';
    case 'low': return 'border-l-emerald-500 bg-emerald-50';
    default: return 'border-l-gray-500 bg-gray-50';
  }
};

const getAIResponse = (msg: string): string => {
  const lower = msg.toLowerCase();
  const avg = Math.round([...performanceData.map(d => d.score), ...subjectData.map(d => d.score)].reduce((a, b) => a + b, 0) / (performanceData.length + subjectData.length));
  
  if (lower.includes("attendance"))
    return `Your overall attendance is 87.5%. You're above 75% — keep it up!`;
  if (lower.includes("marks") || lower.includes("score") || lower.includes("weak"))
    return `Your average score is ${avg}%. Weakest subject: Data Analytics (68%). Consider extra practice there!`;
  if (lower.includes("study plan") || lower.includes("plan"))
    return `📅 Suggested study plan:\n• Mon/Wed: Operating Systems (theory + problems)\n• Tue/Thu: Machine Learning (practice sets)\n• Fri: Revision & quiz prep\n• Sat: Mock tests & weak-area review`;
  if (lower.includes("cgpa") || lower.includes("grade"))
    return `Your current CGPA is 8.42. To improve, focus on your CAT scores and assignment submissions.`;
  return "I can help with attendance, marks, study plans, CGPA, or risk assessment. What would you like to know? 🎓";
};

/* ─── Animation Variants ───────────────────────────────────────────────── */
const cardVariants = {
  hidden: { opacity: 0, y: 24, scale: 0.95 },
  visible: (i: number) => ({
    opacity: 1, y: 0, scale: 1,
    transition: { delay: i * 0.08, duration: 0.45, type: "spring", stiffness: 120 },
  }),
};

const fadeUp = {
  hidden: { opacity: 0, y: 16 },
  visible: (i = 0) => ({ opacity: 1, y: 0, transition: { delay: i * 0.06, duration: 0.4 } }),
};

/* ─── Sub-Components ───────────────────────────────────────────────────── */
const Card: React.FC<{ children: React.ReactNode; className?: string; index?: number }> = ({ 
  children, 
  className = '',
  index = 0
}) => (
  <motion.div
    custom={index}
    variants={cardVariants}
    initial="hidden"
    animate="visible"
    className={`bg-white dark:bg-slate-900/60 rounded-2xl shadow-lg border border-slate-200/80 dark:border-white/10 overflow-hidden ${className}`}
  >
    {children}
  </motion.div>
);

const ProgressBar: React.FC<{ progress: number; color?: string; className?: string }> = ({ 
  progress, 
  color = 'bg-indigo-600',
  className = ''
}) => (
  <div className={`w-full bg-gray-200 dark:bg-white/10 rounded-full h-3 ${className}`}>
    <div 
      className={`${color} h-3 rounded-full transition-all duration-500`} 
      style={{ width: `${Math.min(progress, 100)}%` }}
    />
  </div>
);

const StatCard: React.FC<{
  label: string;
  value: string | number;
  icon: React.ElementType;
  iconColor: string;
  iconBg: string;
  accent: string;
  index: number;
}> = ({ label, value, icon: Icon, iconColor, iconBg, accent, index }) => (
  <Card index={index} className="relative group cursor-default hover:shadow-xl transition-all duration-300">
    <div className={`absolute top-0 left-0 right-0 h-[3px] ${accent} opacity-0 group-hover:opacity-100 transition-opacity duration-300`} />
    <div className="p-5">
      <div className={`w-12 h-12 rounded-xl ${iconBg} flex items-center justify-center mb-4 transition-transform duration-300 group-hover:scale-110`}>
        <Icon className={`w-6 h-6 ${iconColor}`} />
      </div>
      <p className="text-2xl font-bold text-slate-900 dark:text-white leading-none">{value}</p>
      <p className="text-sm text-slate-500 dark:text-slate-400 mt-2 font-medium">{label}</p>
    </div>
  </Card>
);

/* ─── Main Component ────────────────────────────────────────────────────── */
export default function StudentDashboard() {
  const [chatOpen, setChatOpen] = useState(false);
  const [chatInput, setChatInput] = useState("");
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    { role: "ai", text: "Hi! I'm your AI Academic Assistant 🤖. Ask about your performance, study plan, or risk level!" },
  ]);
  const [showStudyPlan, setShowStudyPlan] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  const allScores = [...performanceData.map(d => d.score), ...subjectData.map(d => d.score)];
  const riskStatus = calculateRiskLevel(allScores);
  const assignmentProgress = (assignmentStats.completed / assignmentStats.total) * 100;
  const avgScore = Math.round(allScores.reduce((a, b) => a + b, 0) / allScores.length);

  const weakAreas = useMemo(() => {
    return subjectData
      .map((item) => ({ label: item.subject, score: item.score }))
      .sort((a, b) => a.score - b.score)
      .slice(0, 3);
  }, []);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

  const handleChatSend = () => {
    if (!chatInput.trim()) return;
    const userMsg = chatInput.trim();
    setChatMessages((prev) => [...prev, { role: "user", text: userMsg }]);
    setChatInput("");
    setTimeout(() => {
      setChatMessages((prev) => [...prev, { role: "ai", text: getAIResponse(userMsg) }]);
    }, 400);
  };

  const stats = [
    { label: "CGPA", value: "8.42", icon: GraduationCap, iconColor: "text-indigo-600", iconBg: "bg-indigo-50", accent: "bg-gradient-to-r from-indigo-500 to-blue-500" },
    { label: "Overall Attendance", value: "87.5%", icon: Calendar, iconColor: "text-emerald-600", iconBg: "bg-emerald-50", accent: "bg-gradient-to-r from-emerald-500 to-green-500" },
    { label: "Assignment Avg", value: `${avgScore}%`, icon: BarChart3, iconColor: "text-violet-600", iconBg: "bg-violet-50", accent: "bg-gradient-to-r from-violet-500 to-purple-500" },
    { label: "Study Hours", value: `${productivityData.studyHours}h`, icon: Clock, iconColor: "text-sky-600", iconBg: "bg-sky-50", accent: "bg-gradient-to-r from-sky-500 to-cyan-500" },
    { label: "AI Risk Level", value: riskStatus.level, icon: AlertTriangle, iconColor: riskStatus.color.replace('text-', ''), iconBg: riskStatus.level === 'LOW RISK' ? 'bg-emerald-50' : riskStatus.level === 'MEDIUM RISK' ? 'bg-orange-50' : 'bg-red-50', accent: riskStatus.level === 'LOW RISK' ? 'bg-gradient-to-r from-emerald-500 to-green-500' : riskStatus.level === 'MEDIUM RISK' ? 'bg-gradient-to-r from-orange-500 to-yellow-500' : 'bg-gradient-to-r from-red-500 to-rose-500' },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-slate-950 dark:via-indigo-950 dark:to-slate-900 p-4 md:p-8">
      <div className="max-w-7xl mx-auto space-y-6 pb-24">
        
        {/* ── Hero Header ─────────────────────────────────────────────── */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="relative overflow-hidden rounded-3xl
            bg-gradient-to-br from-indigo-600 via-violet-600 to-purple-700
            dark:from-indigo-700 dark:via-violet-700 dark:to-purple-800
            p-8 shadow-2xl shadow-indigo-500/20"
        >
          <div className="absolute -top-10 -right-10 w-48 h-48 rounded-full bg-white/10 blur-3xl pointer-events-none" />
          <div className="absolute -bottom-8 -left-8 w-36 h-36 rounded-full bg-violet-300/20 blur-2xl pointer-events-none" />
          
          <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <p className="text-indigo-200 text-sm font-medium mb-1">Performance Dashboard</p>
              <h1 className="text-3xl md:text-4xl font-bold text-white tracking-tight">
                Student Performance Dashboard
              </h1>
              <p className="text-indigo-200/80 text-sm mt-2">Track your progress and improve learning</p>
            </div>
            <div className="flex gap-3 flex-wrap">
              <button
                onClick={() => setShowStudyPlan(!showStudyPlan)}
                className="flex items-center gap-2 px-4 py-2.5 bg-white/15 hover:bg-white/25 text-white border border-white/20 backdrop-blur-sm rounded-xl transition-all font-medium"
              >
                <Brain className="w-4 h-4" />
                AI Study Plan
              </button>
              <button className="flex items-center gap-2 px-4 py-2.5 bg-white text-indigo-700 hover:bg-indigo-50 rounded-xl transition-all font-medium shadow-lg">
                My Subjects <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </motion.div>

        {/* ── Top Stats Grid ─────────────────────────────────────────── */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
          {stats.map((s, i) => <StatCard key={s.label} {...s} index={i} />)}
        </div>

        {/* ── AI Study Plan Panel ─────────────────────────────────────── */}
        <AnimatePresence>
          {showStudyPlan && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.35 }}
              className="overflow-hidden"
            >
              <Card className="bg-gradient-to-br from-violet-50 to-purple-50 dark:from-violet-950/40 dark:to-purple-950/40 border-violet-200/60">
                <div className="p-6">
                  <h3 className="text-sm font-bold text-violet-700 dark:text-violet-300 flex items-center gap-2 mb-4">
                    <Brain className="w-4 h-4" /> AI-Generated Weekly Study Plan
                  </h3>
                  <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
                    {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day, i) => {
                      const subj = internalMarksData.labels[i % internalMarksData.labels.length];
                      const tasks = ["Theory review", "Practice sets", "Problem solving", "Mock quiz", "Revision", "Past papers"];
                      return (
                        <motion.div
                          key={day}
                          custom={i}
                          variants={fadeUp}
                          initial="hidden"
                          animate="visible"
                          className="p-4 rounded-xl bg-white dark:bg-white/[0.06] border border-violet-100 dark:border-violet-500/15 hover:border-violet-300 transition-colors"
                        >
                          <p className="text-[10px] font-bold text-violet-500 uppercase tracking-widest">{day}</p>
                          <p className="text-sm font-semibold text-slate-800 dark:text-white mt-2">{subj}</p>
                          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{tasks[i]}</p>
                        </motion.div>
                      );
                    })}
                  </div>
                </div>
              </Card>
            </motion.div>
          )}
        </AnimatePresence>

        {/* ── Charts Section ─────────────────────────────────────────── */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Performance Over Time */}
          <Card index={5}>
            <div className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <TrendingUp className="w-5 h-5 text-indigo-600" />
                <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Performance Over Time</h2>
              </div>
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={performanceData}>
                    <defs>
                      <linearGradient id="colorScore" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3}/>
                        <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="week" stroke="#64748b" fontSize={12} />
                    <YAxis stroke="#64748b" fontSize={12} domain={[0, 100]} />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#fff', 
                        border: 'none', 
                        borderRadius: '12px', 
                        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' 
                      }}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="score" 
                      stroke="#6366f1" 
                      strokeWidth={3}
                      fillOpacity={1} 
                      fill="url(#colorScore)" 
                    />
                    <Line 
                      type="monotone" 
                      dataKey="score" 
                      stroke="#6366f1" 
                      strokeWidth={3}
                      dot={{ fill: '#6366f1', r: 5 }}
                      activeDot={{ r: 7 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>
          </Card>

          {/* Subject-wise Performance */}
          <Card index={6}>
            <div className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <BookOpen className="w-5 h-5 text-indigo-600" />
                <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Subject-wise Performance</h2>
              </div>
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={subjectData} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" horizontal={false} />
                    <XAxis type="number" domain={[0, 100]} stroke="#64748b" fontSize={12} />
                    <YAxis type="category" dataKey="subject" stroke="#64748b" fontSize={12} width={120} />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#fff', 
                        border: 'none', 
                        borderRadius: '12px', 
                        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' 
                      }}
                    />
                    <Bar dataKey="score" radius={[0, 8, 8, 0]}>
                      {subjectData.map((entry, index) => (
                        <Cell 
                          key={`cell-${index}`} 
                          fill={entry.score >= 75 ? '#10b981' : entry.score >= 60 ? '#6366f1' : '#f59e0b'}
                        />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </Card>
        </div>

        {/* ── Internal Marks & Attendance ────────────────────────────── */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card index={7}>
            <div className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <BookOpen className="w-5 h-5 text-emerald-600" />
                <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Internal Marks — CAT 1 / 2 / 3</h2>
              </div>
              <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={internalMarksData.labels.map((label, i) => ({
                    subject: label,
                    cat1: internalMarksData.cat1[i],
                    cat2: internalMarksData.cat2[i],
                    cat3: internalMarksData.cat3[i],
                  }))}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="subject" stroke="#64748b" fontSize={12} />
                    <YAxis stroke="#64748b" fontSize={12} domain={[0, 100]} />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#fff', 
                        border: 'none', 
                        borderRadius: '12px', 
                        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' 
                      }}
                    />
                    <Legend />
                    <Bar dataKey="cat1" fill="rgba(99,102,241,0.8)" radius={[4, 4, 0, 0]} name="CAT 1" />
                    <Bar dataKey="cat2" fill="rgba(139,92,246,0.8)" radius={[4, 4, 0, 0]} name="CAT 2" />
                    <Bar dataKey="cat3" fill="rgba(167,139,250,0.8)" radius={[4, 4, 0, 0]} name="CAT 3" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </Card>

          <Card index={8}>
            <div className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <Calendar className="w-5 h-5 text-sky-600" />
                <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Weekly Attendance</h2>
              </div>
              <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={attendanceData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="label" stroke="#64748b" fontSize={12} />
                    <YAxis stroke="#64748b" fontSize={12} domain={[0, 100]} />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#fff', 
                        border: 'none', 
                        borderRadius: '12px', 
                        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' 
                      }}
                    />
                    <Bar dataKey="percent" fill="rgba(14,165,233,0.8)" radius={[8, 8, 0, 0]} name="Attendance %" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </Card>
        </div>

        {/* ── Bottom Section: Risk, Assignments, Weak Areas, Suggestions ─ */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          
          {/* AI Risk Prediction */}
          <Card index={9}>
            <div className="p-6 h-full">
              <div className="flex items-center gap-2 mb-4">
                <Brain className="w-5 h-5 text-purple-600" />
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">AI Risk Prediction</h2>
              </div>
              
              <div className={`${riskStatus.bg} rounded-2xl p-5 text-white mb-4 shadow-lg`}>
                <div className="flex items-center gap-3 mb-2">
                  <AlertTriangle className="w-6 h-6" />
                  <span className="text-xl font-bold">{riskStatus.level}</span>
                </div>
                <p className="text-sm opacity-90 leading-relaxed">{riskStatus.message}</p>
              </div>

              <div className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-600 dark:text-slate-400">Risk Score</span>
                  <span className="font-semibold text-slate-900 dark:text-white">
                    {riskStatus.level === 'HIGH RISK' ? '85%' : riskStatus.level === 'MEDIUM RISK' ? '45%' : '15%'}
                  </span>
                </div>
                <ProgressBar 
                  progress={riskStatus.level === 'HIGH RISK' ? 85 : riskStatus.level === 'MEDIUM RISK' ? 45 : 15}
                  color={riskStatus.level === 'HIGH RISK' ? 'bg-red-400' : riskStatus.level === 'MEDIUM RISK' ? 'bg-orange-400' : 'bg-emerald-400'}
                />
              </div>
            </div>
          </Card>

          {/* Assignment Progress */}
          <Card index={10}>
            <div className="p-6 h-full">
              <div className="flex items-center gap-2 mb-4">
                <Calendar className="w-5 h-5 text-indigo-600" />
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Assignment Progress</h2>
              </div>

              <div className="grid grid-cols-3 gap-3 mb-6">
                <div className="text-center p-3 bg-slate-50 dark:bg-white/5 rounded-xl">
                  <p className="text-xl font-bold text-slate-900 dark:text-white">{assignmentStats.total}</p>
                  <p className="text-xs text-slate-500">Total</p>
                </div>
                <div className="text-center p-3 bg-emerald-50 dark:bg-emerald-500/10 rounded-xl">
                  <p className="text-xl font-bold text-emerald-600">{assignmentStats.completed}</p>
                  <p className="text-xs text-slate-500">Done</p>
                </div>
                <div className="text-center p-3 bg-orange-50 dark:bg-orange-500/10 rounded-xl">
                  <p className="text-xl font-bold text-orange-600">{assignmentStats.pending}</p>
                  <p className="text-xs text-slate-500">Pending</p>
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-600 dark:text-slate-400">Completion Rate</span>
                  <span className="font-semibold text-slate-900 dark:text-white">{Math.round(assignmentProgress)}%</span>
                </div>
                <ProgressBar progress={assignmentProgress} color="bg-indigo-600" />
              </div>
            </div>
          </Card>

          {/* Weak Areas */}
          <Card index={11}>
            <div className="p-6 h-full">
              <div className="flex items-center gap-2 mb-4">
                <Target className="w-5 h-5 text-rose-600" />
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Weak Areas</h2>
              </div>
              <div className="space-y-4">
                {weakAreas.map((area, i) => (
                  <motion.div
                    key={area.label}
                    custom={i}
                    variants={fadeUp}
                    initial="hidden"
                    animate="visible"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-slate-800 dark:text-white">{area.label}</span>
                      <span className="text-sm font-semibold text-slate-600 dark:text-slate-300">{area.score}%</span>
                    </div>
                    <div className="h-2 rounded-full bg-slate-100 dark:bg-white/10 overflow-hidden">
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${area.score}%` }}
                        transition={{ duration: 0.8, delay: i * 0.1 }}
                        className={`h-full rounded-full ${area.score < 75 ? "bg-gradient-to-r from-rose-500 to-red-400" : "bg-gradient-to-r from-amber-500 to-yellow-400"}`}
                      />
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>
          </Card>

          {/* Smart Suggestions */}
          <Card index={12}>
            <div className="p-6 h-full">
              <div className="flex items-center gap-2 mb-4">
                <Award className="w-5 h-5 text-indigo-600" />
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Smart Suggestions</h2>
              </div>
              <div className="space-y-3">
                {suggestions.map((suggestion) => {
                  const Icon = suggestion.icon;
                  return (
                    <div 
                      key={suggestion.id}
                      className={`flex items-start gap-3 p-3 rounded-xl border-l-4 ${getPriorityColor(suggestion.priority)}`}
                    >
                      <Icon className="w-5 h-5 text-slate-600 mt-0.5 flex-shrink-0" />
                      <p className="text-sm text-slate-700 dark:text-slate-300">{suggestion.text}</p>
                    </div>
                  );
                })}
              </div>
            </div>
          </Card>
        </div>

        {/* ── Mentor Matches ─────────────────────────────────────────── */}
        <Card index={13}>
          <div className="p-6">
            <div className="flex items-center gap-2 mb-6">
              <Sparkles className="w-5 h-5 text-amber-500" />
              <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Recommended Mentors</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {mentorMatches.map((mentor, i) => (
                <motion.div
                  key={mentor.mentorId}
                  custom={i}
                  variants={fadeUp}
                  initial="hidden"
                  animate="visible"
                  className="flex items-center justify-between gap-4 p-4 rounded-xl border border-slate-100 dark:border-white/10 bg-slate-50/80 dark:bg-white/5 hover:border-indigo-200 dark:hover:border-indigo-400/30 hover:bg-indigo-50/40 transition-all"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center text-white text-sm font-bold shrink-0">
                      {mentor.mentorName.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2)}
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900 dark:text-white">{mentor.mentorName}</p>
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        {mentor.mentorDepartment} · {mentor.proficiencyLevel}
                      </p>
                      <p className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">Skills: {mentor.skills}</p>
                    </div>
                  </div>
                  <div className="px-3 py-1.5 bg-indigo-50 dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 rounded-full text-xs font-semibold">
                    Match {mentor.score}%
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </Card>

        {/* ── AI Chat FAB ──────────────────────────────────────────────── */}
        <motion.button
          whileHover={{ scale: 1.08 }}
          whileTap={{ scale: 0.92 }}
          onClick={() => setChatOpen(!chatOpen)}
          className="fixed bottom-6 right-6 w-14 h-14 rounded-full
            bg-gradient-to-br from-indigo-500 to-violet-600
            text-white shadow-2xl shadow-indigo-500/40
            flex items-center justify-center z-50
            transition-shadow hover:shadow-indigo-500/60"
        >
          <AnimatePresence mode="wait">
            {chatOpen
              ? <motion.span key="x" initial={{ rotate: -90, opacity: 0 }} animate={{ rotate: 0, opacity: 1 }} exit={{ rotate: 90, opacity: 0 }}><X className="w-6 h-6" /></motion.span>
              : <motion.span key="bot" initial={{ rotate: 90, opacity: 0 }} animate={{ rotate: 0, opacity: 1 }} exit={{ rotate: -90, opacity: 0 }}><Bot className="w-6 h-6" /></motion.span>
            }
          </AnimatePresence>
        </motion.button>

        {/* ── AI Chat Panel ───────────────────────────────────────────── */}
        <AnimatePresence>
          {chatOpen && (
            <motion.div
              initial={{ opacity: 0, y: 16, scale: 0.96 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 16, scale: 0.96 }}
              transition={{ duration: 0.25 }}
              className="fixed bottom-24 right-6 w-[380px] max-w-[calc(100vw-48px)] z-50"
            >
              <div className="bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl rounded-2xl border border-slate-200/80 dark:border-white/15 shadow-2xl overflow-hidden">
                <div className="p-4 border-b border-slate-100 dark:border-white/10 flex items-center gap-2">
                  <Bot className="w-5 h-5 text-indigo-600" />
                  <span className="font-semibold text-slate-900 dark:text-white">AI Academic Assistant</span>
                  <span className="ml-auto w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                </div>
                
                <div className="h-80 overflow-y-auto p-4 space-y-3">
                  {chatMessages.map((msg, idx) => (
                    <motion.div
                      key={idx}
                      initial={{ opacity: 0, y: 6 }}
                      animate={{ opacity: 1, y: 0 }}
                      className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
                    >
                      <div className={`max-w-[85%] p-3 rounded-2xl text-sm leading-relaxed whitespace-pre-line ${
                        msg.role === "user"
                          ? "bg-indigo-600 text-white rounded-br-sm"
                          : "bg-slate-100 dark:bg-white/10 text-slate-700 dark:text-slate-300 rounded-bl-sm"
                      }`}>
                        {msg.text}
                      </div>
                    </motion.div>
                  ))}
                  <div ref={chatEndRef} />
                </div>

                <div className="p-3 border-t border-slate-100 dark:border-white/10 flex flex-wrap gap-2">
                  {["My attendance?", "Weakest subject?", "Study plan"].map((q) => (
                    <button
                      key={q}
                      onClick={() => setChatInput(q)}
                      className="text-xs px-3 py-1.5 rounded-full bg-slate-100 dark:bg-white/10 text-slate-600 dark:text-slate-400 hover:bg-indigo-50 dark:hover:bg-indigo-500/20 hover:text-indigo-600 transition-colors"
                    >
                      {q}
                    </button>
                  ))}
                </div>

                <div className="p-4 border-t border-slate-100 dark:border-white/10 flex gap-2">
                  <input
                    value={chatInput}
                    onChange={(e) => setChatInput(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && handleChatSend()}
                    placeholder="Ask about your performance…"
                    className="flex-1 bg-slate-50 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-2 text-sm text-slate-800 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  />
                  <button
                    onClick={handleChatSend}
                    disabled={!chatInput.trim()}
                    className="w-10 h-10 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white rounded-xl flex items-center justify-center transition-colors"
                  >
                    <Send className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

      </div>
    </div>
  );
}

export { StudentDashboard };       
import { Suspense, lazy, useEffect, useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Sparkles, Shuffle, Flame, Compass, Wand2, Bookmark, TrendingUp, Zap, Star } from "lucide-react";
import OpportunityCard from "@/components/happenstance/OpportunityCard";
import SavedOpportunities from "@/components/happenstance/SavedOpportunities";
import { mockOpportunities, opportunityTags, type Opportunity } from "@/lib/happenstanceData";
import { useRandomizer } from "@/hooks/useRandomizer";
import { cn } from "@/lib/utils";
import { getJson, postJson } from "@/lib/api";
const LazyMarksChart = lazy(() => import("@/components/Charts/MarksChart"));

const CLICK_KEY = "happenstance_clicks";

type DomainStat = { domain: string; count: number };
type OpportunityStat = { id: number; title: string; saves: number };
type SerendipityScore = {
  score: number;
  uniqueDomains: number;
  totalInteractions: number;
  outOfComfortInteractions: number;
};
type HappenstanceAnalytics = {
  topClickedDomains: DomainStat[];
  mostSavedDomains: DomainStat[];
  mostSavedOpportunities: OpportunityStat[];
  totalClicks: number;
  totalSaves: number;
  serendipityScore: SerendipityScore;
};

const getDailyPick = (items: Opportunity[]) => {
  if (!items.length) return null;
  const date = new Date();
  const start = new Date(date.getFullYear(), 0, 0);
  const diff = date.getTime() - start.getTime();
  const dayIndex = Math.floor(diff / (1000 * 60 * 60 * 24)) % items.length;
  return items[dayIndex];
};

/* ── Floating ambient orbs ── */
function FloatingOrbs() {
  return (
    <div className="pointer-events-none fixed inset-0 overflow-hidden" aria-hidden>
      <div className="absolute -left-32 -top-32 h-[500px] w-[500px] rounded-full bg-gradient-to-br from-amber-200/40 via-orange-200/30 to-transparent blur-3xl dark:opacity-0 transition-opacity duration-700" />
      <div className="absolute -right-40 top-1/4 h-[600px] w-[600px] rounded-full bg-gradient-to-bl from-teal-200/35 via-cyan-200/25 to-transparent blur-3xl dark:opacity-0 transition-opacity duration-700" />
      <div className="absolute bottom-0 left-1/3 h-[400px] w-[400px] rounded-full bg-gradient-to-t from-rose-200/30 to-transparent blur-3xl dark:opacity-0 transition-opacity duration-700" />
      <div className="absolute -left-32 -top-32 h-[500px] w-[500px] rounded-full bg-gradient-to-br from-violet-900/50 via-indigo-900/30 to-transparent blur-3xl opacity-0 dark:opacity-100 transition-opacity duration-700" />
      <div className="absolute -right-40 top-1/4 h-[600px] w-[600px] rounded-full bg-gradient-to-bl from-cyan-900/40 via-teal-900/20 to-transparent blur-3xl opacity-0 dark:opacity-100 transition-opacity duration-700" />
      <div className="absolute bottom-0 left-1/3 h-[400px] w-[400px] rounded-full bg-gradient-to-t from-fuchsia-900/40 to-transparent blur-3xl opacity-0 dark:opacity-100 transition-opacity duration-700" />
    </div>
  );
}

/* ── Grain texture ── */
function GrainOverlay() {
  return (
    <div
      className="pointer-events-none fixed inset-0 opacity-[0.025] dark:opacity-[0.04]"
      style={{
        backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E")`,
        backgroundRepeat: "repeat",
        backgroundSize: "128px 128px",
      }}
      aria-hidden
    />
  );
}

/* ── Animated tag pill ── */
function TagPill({ tag, active, onClick, index }: { tag: string; active: boolean; onClick: () => void; index: number }) {
  return (
    <motion.button
      type="button"
      onClick={onClick}
      initial={{ opacity: 0, scale: 0.8, y: 8 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      transition={{ delay: index * 0.03, type: "spring", stiffness: 300, damping: 20 }}
      whileHover={{ scale: 1.08, y: -1 }}
      whileTap={{ scale: 0.94 }}
      className={cn(
        "relative rounded-full border px-3.5 py-1.5 text-xs font-semibold tracking-wide transition-colors duration-200",
        active
          ? "border-amber-400/80 bg-amber-400/15 text-amber-700 shadow-sm shadow-amber-400/20 dark:border-amber-400/60 dark:bg-amber-400/10 dark:text-amber-300"
          : "border-stone-200/80 bg-white/60 text-stone-500 hover:border-stone-300 hover:text-stone-700 dark:border-white/10 dark:bg-white/5 dark:text-stone-400 dark:hover:border-white/20 dark:hover:text-stone-200"
      )}
    >
      {active && (
        <motion.span layoutId="tag-active-bg" className="absolute inset-0 rounded-full bg-amber-400/10 dark:bg-amber-400/8" />
      )}
      {tag}
    </motion.button>
  );
}

/* ── Section reveal wrapper ── */
function Section({ children, delay = 0, className = "" }: { children: React.ReactNode; delay?: number; className?: string }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 28 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, delay, ease: [0.22, 1, 0.36, 1] }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

/* ── Glass card ── */
function Glass({ children, className = "", glow = false }: { children: React.ReactNode; className?: string; glow?: boolean }) {
  return (
    <div className={cn(
      "relative overflow-hidden rounded-3xl border backdrop-blur-xl transition-all duration-300",
      "border-stone-200/60 bg-white/70 shadow-sm",
      "dark:border-white/[0.08] dark:bg-white/[0.04]",
      glow && "hover:shadow-lg hover:shadow-amber-500/5 dark:hover:shadow-lg dark:hover:shadow-violet-500/10",
      className
    )}>
      {children}
    </div>
  );
}

/* ── SVG score ring ── */
function ScoreRing({ score }: { score: number }) {
  const r = 38;
  const circ = 2 * Math.PI * r;
  const dash = (score / 100) * circ;
  return (
    <div className="relative flex h-24 w-24 items-center justify-center">
      <svg className="-rotate-90" width="96" height="96" viewBox="0 0 96 96">
        <circle cx="48" cy="48" r={r} strokeWidth="6" className="fill-none stroke-stone-200 dark:stroke-white/10" />
        <motion.circle
          cx="48" cy="48" r={r} strokeWidth="6" fill="none"
          stroke="url(#sg)" strokeLinecap="round"
          strokeDasharray={circ}
          initial={{ strokeDashoffset: circ }}
          animate={{ strokeDashoffset: circ - dash }}
          transition={{ duration: 1.5, ease: "easeOut", delay: 0.3 }}
        />
        <defs>
          <linearGradient id="sg" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#f59e0b" />
            <stop offset="100%" stopColor="#ef4444" />
          </linearGradient>
        </defs>
      </svg>
      <div className="absolute flex flex-col items-center">
        <span className="text-xl font-bold text-stone-800 dark:text-stone-100">{score}</span>
        <span className="text-[9px] font-semibold uppercase tracking-widest text-stone-400 dark:text-stone-500">/100</span>
      </div>
    </div>
  );
}

/* ── Button with spinning icon ── */
function ActionButton({ onClick, label, icon: Icon, variant = "primary" }: {
  onClick: () => void; label: string; icon: React.ElementType; variant?: "primary" | "secondary" | "ghost";
}) {
  const [spinning, setSpinning] = useState(false);
  const handle = () => { setSpinning(true); setTimeout(() => setSpinning(false), 600); onClick(); };
  const base = "relative flex items-center gap-2 overflow-hidden rounded-2xl px-4 py-2.5 text-sm font-semibold transition-all duration-200 active:scale-95";
  const variants = {
    primary: "bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-lg shadow-amber-500/25 hover:shadow-amber-500/40 hover:from-amber-400 hover:to-orange-400",
    secondary: "border border-stone-200 bg-white/80 text-stone-700 hover:border-stone-300 dark:border-white/10 dark:bg-white/5 dark:text-stone-300 dark:hover:border-white/20",
    ghost: "text-stone-500 hover:bg-stone-100/60 hover:text-stone-700 dark:text-stone-400 dark:hover:bg-white/5 dark:hover:text-stone-200",
  };
  return (
    <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.96 }} onClick={handle} className={cn(base, variants[variant])}>
      <motion.span animate={spinning ? { rotate: 360 } : { rotate: 0 }} transition={{ duration: 0.5, ease: "easeInOut" }}>
        <Icon size={15} />
      </motion.span>
      {label}
    </motion.button>
  );
}

/* ── Empty placeholder ── */
function EmptySlot({ emoji, text }: { emoji: string; text: string }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="flex h-full min-h-[240px] flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-stone-200 bg-stone-50/80 dark:border-white/[0.06] dark:bg-white/[0.02]"
    >
      <span className="text-3xl">{emoji}</span>
      <p className="text-center text-sm text-stone-400 dark:text-stone-500">{text}</p>
    </motion.div>
  );
}

/* ═══════════════════ MAIN ═══════════════════ */
export default function HappenstanceEngine() {
  const [items, setItems] = useState<Opportunity[]>([]);
  const [recommended, setRecommended] = useState<Opportunity[]>([]);
  const [interestOptions, setInterestOptions] = useState<string[]>(opportunityTags);
  const [selectedInterest, setSelectedInterest] = useState<string>("AI");
  const [surprisePick, setSurprisePick] = useState<Opportunity | null>(null);
  const [newPick, setNewPick] = useState<Opportunity | null>(null);
  const [isShuffling, setIsShuffling] = useState(false);
  const [loading, setLoading] = useState(true);
  const [analytics, setAnalytics] = useState<HappenstanceAnalytics | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [exploreSeed, setExploreSeed] = useState(0);

  const randomizer = useRandomizer(items);
  const dailyPick = useMemo(() => getDailyPick(items), [items]);

  const trending = useMemo(() => {
    if (recommended.length) return recommended.slice(0, 3);
    const pool = items.filter((i) => i.trending);
    return (pool.length ? pool : items.slice(0, 3)).slice(0, 3);
  }, [items, recommended]);

  const exploreList = useMemo(
    () => randomizer.getExploreList(selectedInterest, 4 + (exploreSeed % 2)),
    [randomizer, selectedInterest, exploreSeed]
  );

  const refreshAnalytics = async (activeRef?: { current: boolean }) => {
    try {
      const data = await getJson<HappenstanceAnalytics>("/api/happenstance/analytics");
      if (!activeRef || activeRef.current) setAnalytics(data);
    } catch {
      if (!activeRef || activeRef.current) setAnalytics(null);
    }
  };

  useEffect(() => {
    const active = { current: true };
    (async () => {
      setLoading(true); setError(null);
      try {
        const data = await getJson<Opportunity[]>("/api/happenstance");
        if (!active.current) return;
        setItems(data.length ? data : mockOpportunities);
      } catch (err) {
        if (!active.current) return;
        setItems(mockOpportunities);
        setError(err instanceof Error ? err.message : "Failed to load feed.");
      } finally {
        if (active.current) setLoading(false);
      }
    })();
    (async () => {
      try {
        const data = await getJson<Opportunity[]>("/api/happenstance/recommendations");
        if (active.current) setRecommended(data);
      } catch { if (active.current) setRecommended([]); }
    })();
    (async () => {
      try {
        const data = await getJson<string[]>("/api/happenstance/interests");
        if (active.current && data.length) {
          setInterestOptions(Array.from(new Set([...data, ...opportunityTags])));
          setSelectedInterest(data[0]);
        }
      } catch { if (active.current) setInterestOptions(opportunityTags); }
    })();
    refreshAnalytics(active);
    return () => { active.current = false; };
  }, []);

  const handleSave = (item: Opportunity) => {
    postJson<{ opportunityId: number; saved: boolean }>("/api/happenstance/save", { opportunityId: item.id })
      .then((r) => {
        setItems((prev) => prev.map((e) => e.id === item.id ? { ...e, saved: r.saved } : e));
        refreshAnalytics();
      })
      .catch(() => setItems((prev) => prev.map((e) => e.id === item.id ? { ...e, saved: !e.saved } : e)));
  };

  const handleApply = (item: Opportunity) => {
    if (typeof window !== "undefined") {
      const raw = localStorage.getItem(CLICK_KEY);
      const clicks = raw ? (JSON.parse(raw) as Record<string, number>) : {};
      clicks[item.id] = (clicks[item.id] || 0) + 1;
      localStorage.setItem(CLICK_KEY, JSON.stringify(clicks));
    }
    postJson("/api/happenstance/click", { opportunityId: item.id }).then(() => refreshAnalytics()).catch(() => null);
    window.open(item.link, "_blank");
  };

  const runShuffle = (next: Opportunity | null) => {
    setIsShuffling(true);
    window.setTimeout(() => { setSurprisePick(next); setIsShuffling(false); }, 700);
  };

  const handleSurprise = () => { const p = randomizer.pickSmart(selectedInterest); if (p) runShuffle(p); };
  const handleTryNew = () => setNewPick(randomizer.pickOutOfComfort(selectedInterest));
  const savedItems = useMemo(() => items.filter((i) => i.saved), [items]);

  /* ─────────── RENDER ─────────── */
  return (
    <div className="relative min-h-screen bg-stone-50 dark:bg-[#0c0c0f] px-4 py-10 sm:px-6">
      <FloatingOrbs />
      <GrainOverlay />

      <div className="relative mx-auto max-w-6xl space-y-8">

        {/* ── Hero ── */}
        <Section delay={0}>
          <div className="relative overflow-hidden rounded-3xl border border-stone-200/50 bg-gradient-to-br from-stone-900 via-stone-800 to-stone-950 p-8 shadow-2xl dark:border-white/5 dark:from-[#111114] dark:via-[#16161a] dark:to-[#0e0e12]">
            {/* Accent lines */}
            <div className="pointer-events-none absolute inset-0">
              <div className="absolute left-0 top-0 h-px w-1/2 bg-gradient-to-r from-transparent via-amber-400/60 to-transparent" />
              <div className="absolute bottom-0 right-0 h-px w-1/2 bg-gradient-to-l from-transparent via-orange-400/40 to-transparent" />
              <div className="absolute right-0 top-0 h-1/2 w-px bg-gradient-to-b from-transparent via-amber-400/30 to-transparent" />
              <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-gradient-to-bl from-amber-500/20 to-transparent blur-2xl" />
              <div className="absolute -bottom-12 left-12 h-32 w-32 rounded-full bg-gradient-to-tr from-orange-500/10 to-transparent blur-xl" />
            </div>

            <div className="relative flex flex-wrap items-end justify-between gap-6">
              <div className="space-y-3">
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.5 }}
                  className="flex items-center gap-2"
                >
                  <span className="flex h-6 w-6 items-center justify-center rounded-full bg-amber-500/20 text-amber-400 ring-1 ring-amber-500/30">
                    <Zap size={12} />
                  </span>
                  <p className="text-[11px] font-semibold uppercase tracking-[0.25em] text-stone-400">Happenstance Engine</p>
                </motion.div>

                <motion.h1
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.1, ease: [0.22, 1, 0.36, 1] }}
                  className="text-3xl font-bold tracking-tight text-white sm:text-4xl"
                >
                  Discover unexpected
                  <br />
                  <span className="bg-gradient-to-r from-amber-300 via-orange-300 to-red-300 bg-clip-text text-transparent">
                    opportunities
                  </span>
                </motion.h1>

                <motion.p
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.2 }}
                  className="max-w-md text-sm leading-relaxed text-stone-400"
                >
                  Build luck through action. Explore beyond comfort. Let serendipity shape your path.
                </motion.p>
              </div>

              {analytics?.serendipityScore && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.6, delay: 0.4, type: "spring" }}
                  className="flex flex-col items-center gap-1"
                >
                  <ScoreRing score={analytics.serendipityScore.score} />
                  <p className="text-[10px] font-semibold uppercase tracking-widest text-stone-500">Serendipity</p>
                </motion.div>
              )}
            </div>
          </div>
        </Section>

        {/* ── Surprise Me ── */}
        <Section delay={0.1}>
          <Glass glow>
            {/* Header */}
            <div className="flex flex-wrap items-center justify-between gap-4 border-b border-stone-200/60 p-5 dark:border-white/[0.06]">
              <div className="flex items-center gap-3">
                <motion.div
                  animate={{ rotate: [0, 10, -10, 0] }}
                  transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
                  className="flex h-9 w-9 items-center justify-center rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 text-white shadow-md shadow-amber-500/30"
                >
                  <Sparkles size={16} />
                </motion.div>
                <div>
                  <p className="font-bold text-stone-800 dark:text-stone-100">Surprise Me</p>
                  <p className="text-xs text-stone-400 dark:text-stone-500">Random + interest + out-of-comfort discoveries</p>
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                <ActionButton onClick={handleSurprise} label="Surprise Me" icon={Shuffle} variant="primary" />
                <ActionButton onClick={handleTryNew} label="Try Something New" icon={Compass} variant="secondary" />
                <ActionButton onClick={() => setExploreSeed((p) => p + 1)} label="Refresh" icon={Wand2} variant="ghost" />
              </div>
            </div>

            {/* Error banner */}
            <AnimatePresence>
              {error && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                  className="overflow-hidden"
                >
                  <div className="flex items-center gap-2 bg-rose-50 px-5 py-2.5 text-xs text-rose-600 dark:bg-rose-900/20 dark:text-rose-300">
                    <span>⚠</span> {error}
                    <button onClick={() => setError(null)} className="ml-auto opacity-60 hover:opacity-100">✕</button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Interest tags */}
            <div className="flex flex-wrap gap-2 px-5 py-4">
              {interestOptions.map((tag, i) => (
                <TagPill key={tag} tag={tag} active={selectedInterest === tag} onClick={() => setSelectedInterest(tag)} index={i} />
              ))}
            </div>

            {/* Panels */}
            <div className="grid gap-5 p-5 pt-0 lg:grid-cols-[1.3fr_0.7fr]">
              {/* Feeling Lucky */}
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <motion.div animate={{ scale: [1, 1.2, 1] }} transition={{ repeat: Infinity, duration: 2 }}>
                    <Flame size={15} className="text-orange-500" />
                  </motion.div>
                  <p className="text-sm font-bold text-stone-700 dark:text-stone-200">Feeling Lucky?</p>
                </div>
                <div className="relative min-h-[240px]">
                  <AnimatePresence mode="wait">
                    {isShuffling ? (
                      <motion.div
                        key="shuffling"
                        initial={{ opacity: 0, scale: 0.92 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.92 }}
                        className="absolute inset-0 flex flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-amber-400/40 bg-amber-50/50 dark:bg-amber-400/5"
                      >
                        <motion.div animate={{ rotate: 360 }} transition={{ repeat: Infinity, duration: 0.8, ease: "linear" }} className="text-2xl">🎲</motion.div>
                        <p className="text-sm font-semibold text-amber-600 dark:text-amber-400">Shuffling…</p>
                        <div className="flex gap-1.5">
                          {[0, 1, 2].map((i) => (
                            <motion.span key={i} animate={{ y: [0, -6, 0] }} transition={{ repeat: Infinity, duration: 0.6, delay: i * 0.15 }} className="h-1.5 w-1.5 rounded-full bg-amber-400" />
                          ))}
                        </div>
                      </motion.div>
                    ) : surprisePick ? (
                      <motion.div
                        key={`s-${surprisePick.id}`}
                        initial={{ opacity: 0, y: 20, rotate: -1 }}
                        animate={{ opacity: 1, y: 0, rotate: 0 }}
                        exit={{ opacity: 0, y: -20 }}
                        transition={{ type: "spring", stiffness: 280, damping: 22 }}
                      >
                        <OpportunityCard item={surprisePick} saved={surprisePick.saved} onSave={handleSave} onApply={handleApply} variant="highlight" />
                      </motion.div>
                    ) : (
                      <EmptySlot key="e-s" emoji="🎲" text={loading ? "Loading opportunities…" : 'Hit "Surprise Me" to reveal a chance.'} />
                    )}
                  </AnimatePresence>
                </div>
              </div>

              {/* Try Something New */}
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <Compass size={15} className="text-teal-500" />
                  <p className="text-sm font-bold text-stone-700 dark:text-stone-200">Try Something New</p>
                </div>
                <div className="min-h-[240px]">
                  <AnimatePresence mode="wait">
                    {newPick ? (
                      <motion.div
                        key={`n-${newPick.id}`}
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: -20 }}
                        transition={{ type: "spring", stiffness: 280, damping: 22 }}
                      >
                        <OpportunityCard item={newPick} saved={newPick.saved} onSave={handleSave} onApply={handleApply} />
                      </motion.div>
                    ) : (
                      <EmptySlot key="e-n" emoji="🧭" text={loading ? "Loading…" : 'Choose an interest, then press "Try Something New".'} />
                    )}
                  </AnimatePresence>
                </div>
              </div>
            </div>
          </Glass>
        </Section>

        {/* ── Daily Pick + Recommended ── */}
        <Section delay={0.18}>
          <div className="grid gap-5 lg:grid-cols-[1.15fr_0.85fr]">
            <Glass glow className="p-5">
              <div className="mb-4 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <motion.span animate={{ scale: [1, 1.15, 1] }} transition={{ repeat: Infinity, duration: 2.5 }} className="text-base">☀️</motion.span>
                    <p className="font-bold text-stone-800 dark:text-stone-100">Daily Recommendation</p>
                  </div>
                  <p className="mt-0.5 text-xs text-stone-400">Same pick for everyone today.</p>
                </div>
                <span className="rounded-full border border-amber-300/60 bg-amber-50 px-3 py-1 text-xs font-bold text-amber-700 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-300">
                  Daily Pick
                </span>
              </div>
              {dailyPick ? (
                <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
                  <OpportunityCard item={dailyPick} saved={dailyPick.saved} onSave={handleSave} onApply={handleApply} />
                </motion.div>
              ) : (
                <p className="text-sm text-stone-400">No daily recommendation yet.</p>
              )}
            </Glass>

            <Glass glow className="p-5">
              <div className="mb-4 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <TrendingUp size={15} className="text-rose-500" />
                    <p className="font-bold text-stone-800 dark:text-stone-100">Recommended</p>
                  </div>
                  <p className="mt-0.5 text-xs text-stone-400">Based on your interests.</p>
                </div>
                <motion.div animate={{ rotate: [0, 15, -5, 0] }} transition={{ repeat: Infinity, duration: 3, ease: "easeInOut" }}>
                  <Flame size={16} className="text-orange-400" />
                </motion.div>
              </div>
              <div className="grid gap-3">
                {trending.map((item, i) => (
                  <motion.div key={item.id} initial={{ opacity: 0, x: -12 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.3 + i * 0.1 }}>
                    <OpportunityCard item={item} saved={item.saved} onSave={handleSave} onApply={handleApply} />
                  </motion.div>
                ))}
              </div>
            </Glass>
          </div>
        </Section>

        {/* ── Explore New Domains ── */}
        <Section delay={0.24}>
          <Glass glow>
            <div className="flex items-center justify-between border-b border-stone-200/60 p-5 dark:border-white/[0.06]">
              <div>
                <div className="flex items-center gap-2">
                  <Star size={15} className="text-violet-500" />
                  <p className="font-bold text-stone-800 dark:text-stone-100">Explore New Domains</p>
                </div>
                <p className="mt-0.5 text-xs text-stone-400">
                  Fresh picks outside <span className="font-semibold text-stone-600 dark:text-stone-300">{selectedInterest}</span>.
                </p>
              </div>
              <ActionButton onClick={() => setExploreSeed((p) => p + 1)} label="Shuffle" icon={Shuffle} variant="ghost" />
            </div>
            <div className="grid gap-4 p-5 sm:grid-cols-2 lg:grid-cols-4">
              <AnimatePresence mode="popLayout">
                {exploreList.map((item, i) => (
                  <motion.div
                    key={`${item.id}-${exploreSeed}`}
                    layout
                    initial={{ opacity: 0, scale: 0.88, y: 16 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.88, y: -16 }}
                    transition={{ delay: i * 0.07, type: "spring", stiffness: 280, damping: 22 }}
                  >
                    <OpportunityCard item={item} saved={item.saved} onSave={handleSave} onApply={handleApply} />
                  </motion.div>
                ))}
              </AnimatePresence>
            </div>
          </Glass>
        </Section>

        {/* ── Analytics ── */}
        <Section delay={0.3}>
          <Glass>
            <div className="flex flex-wrap items-center justify-between gap-4 border-b border-stone-200/60 p-5 dark:border-white/[0.06]">
              <div>
                <div className="flex items-center gap-2">
                  <TrendingUp size={15} className="text-teal-500" />
                  <p className="font-bold text-stone-800 dark:text-stone-100">Serendipity Analytics</p>
                </div>
                <p className="mt-0.5 text-xs text-stone-400">Your exploration diversity at a glance.</p>
              </div>
              {analytics?.serendipityScore && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: 0.5 }}
                  className="flex items-center gap-4 rounded-2xl border border-stone-200/60 bg-stone-50/80 px-4 py-3 dark:border-white/[0.06] dark:bg-white/[0.03]"
                >
                  <ScoreRing score={analytics.serendipityScore.score} />
                  <div className="space-y-1 text-xs text-stone-500 dark:text-stone-400">
                    <p><span className="font-semibold text-stone-700 dark:text-stone-200">{analytics.serendipityScore.uniqueDomains}</span> domains explored</p>
                    <p><span className="font-semibold text-stone-700 dark:text-stone-200">{analytics.serendipityScore.totalInteractions}</span> total interactions</p>
                    <p><span className="font-semibold text-amber-600 dark:text-amber-400">{analytics.serendipityScore.outOfComfortInteractions}</span> out-of-comfort</p>
                  </div>
                </motion.div>
              )}
            </div>

            <div className="grid gap-5 p-5 lg:grid-cols-2">
              {[
                { label: "Top Clicked Domains", domains: analytics?.topClickedDomains, color: "rgba(251,146,60,0.7)" },
                { label: "Most Saved Domains", domains: analytics?.mostSavedDomains, color: "rgba(52,211,153,0.7)" },
              ].map(({ label, domains, color }) => (
                <motion.div
                  key={label}
                  initial={{ opacity: 0, y: 16 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.4 }}
                  className="rounded-2xl border border-stone-200/60 bg-stone-50/60 p-4 dark:border-white/[0.06] dark:bg-white/[0.02]"
                >
                  <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-stone-400">{label}</p>
                  <div className="h-52">
                    <Suspense fallback={
                      <div className="flex h-full items-center justify-center">
                        <motion.span animate={{ rotate: 360 }} transition={{ repeat: Infinity, duration: 1, ease: "linear" }} className="text-xl">📊</motion.span>
                      </div>
                    }>
                      <LazyMarksChart
                        labels={(domains ?? []).map((s) => s.domain)}
                        datasets={[{ label, data: (domains ?? []).map((s) => s.count), color }]}
                        height={208}
                      />
                    </Suspense>
                  </div>
                </motion.div>
              ))}
            </div>

            {analytics?.mostSavedOpportunities?.length ? (
              <div className="mx-5 mb-5 rounded-2xl border border-stone-200/60 bg-stone-50/60 p-4 dark:border-white/[0.06] dark:bg-white/[0.02]">
                <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-stone-400">Most Saved</p>
                <ul className="space-y-2">
                  {analytics.mostSavedOpportunities.map((item, i) => (
                    <motion.li
                      key={`ss-${item.id}`}
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.5 + i * 0.07 }}
                      className="flex items-center justify-between"
                    >
                      <div className="flex items-center gap-2">
                        <span className="flex h-5 w-5 items-center justify-center rounded-full bg-stone-200 text-[10px] font-bold text-stone-500 dark:bg-white/10 dark:text-stone-400">{i + 1}</span>
                        <span className="text-sm font-medium text-stone-700 dark:text-stone-200">{item.title}</span>
                      </div>
                      <span className="flex items-center gap-1 rounded-full bg-stone-100 px-2.5 py-0.5 text-xs font-semibold text-stone-500 dark:bg-white/[0.06] dark:text-stone-400">
                        <Bookmark size={10} /> {item.saves}
                      </span>
                    </motion.li>
                  ))}
                </ul>
              </div>
            ) : null}
          </Glass>
        </Section>

        {/* ── Saved Opportunities ── */}
        <Section delay={0.36}>
          <Glass>
            <div className="flex items-center gap-3 border-b border-stone-200/60 p-5 dark:border-white/[0.06]">
              <motion.div
                animate={{ scale: [1, 1.15, 1] }}
                transition={{ repeat: Infinity, duration: 3, ease: "easeInOut" }}
                className="flex h-8 w-8 items-center justify-center rounded-xl bg-rose-50 text-rose-500 dark:bg-rose-500/10"
              >
                <Bookmark size={15} />
              </motion.div>
              <div>
                <p className="font-bold text-stone-800 dark:text-stone-100">Saved Opportunities</p>
                <p className="text-xs text-stone-400">{savedItems.length} saved</p>
              </div>
            </div>
            <div className="p-5">
              <SavedOpportunities items={savedItems} onRemove={handleSave} onApply={handleApply} />
            </div>
          </Glass>
        </Section>

      </div>
    </div>
  );
}
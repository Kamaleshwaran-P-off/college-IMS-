import { useEffect, useMemo, useState } from "react";
import HackathonCard, { type HackathonItem } from "@/components/hackathons/HackathonCard";
import CardSkeleton from "@/components/skeletons/CardSkeleton";
import LottieAnimation from "@/components/lottie/LottieAnimation";
import { cn } from "@/lib/utils";

const tabs = ["All", "Chennai", "Online"] as const;
type HackathonTab = (typeof tabs)[number];

const mockHackathons: HackathonItem[] = [
  {
    id: "h1",
    title: "Chennai AI Hack Sprint",
    date: "Apr 12, 2026",
    location: "Chennai",
    platform: "Knowafest",
    type: "Chennai",
    description: "Build AI-powered campus tools. Teams of 2-4, prizes and internships.",
    link: "https://www.knowafest.com/explore/events?city=Chennai"
  },
  {
    id: "h2",
    title: "Smart City Hack Chennai",
    date: "May 03, 2026",
    location: "Chennai",
    platform: "Knowafest",
    type: "Chennai",
    description: "IoT + civic tech challenges focused on mobility and sustainability.",
    link: "https://www.knowafest.com/explore/events?city=Chennai"
  },
  {
    id: "h3",
    title: "Web3 Builders Weekend",
    date: "May 24, 2026",
    location: "Chennai",
    platform: "Knowafest",
    type: "Chennai",
    description: "Prototype blockchain products, learn from mentors, and pitch to judges.",
    link: "https://www.knowafest.com/explore/events?city=Chennai"
  },
  {
    id: "h4",
    title: "EdTech Innovation Hack",
    date: "Jun 10, 2026",
    location: "Chennai",
    platform: "Knowafest",
    type: "Chennai",
    description: "Create microlearning and assessment innovations for modern classrooms.",
    link: "https://www.knowafest.com/explore/events?city=Chennai"
  },
  {
    id: "u1",
    title: "Unstop AI Innovators Challenge",
    date: "Apr 28, 2026",
    location: "Online",
    platform: "Unstop",
    type: "Online",
    description: "Online hackathon for AI/ML builders with mentor sessions and cash prizes.",
    link: "https://unstop.com/hackathons"
  },
  {
    id: "u2",
    title: "FinTech Sprint Online",
    date: "May 18, 2026",
    location: "Online",
    platform: "Unstop",
    type: "Online",
    description: "Design fintech products, APIs, and secure payment prototypes in 48 hours.",
    link: "https://unstop.com/hackathons"
  },
  {
    id: "u3",
    title: "Campus Creator Hack",
    date: "Jun 06, 2026",
    location: "Online",
    platform: "Unstop",
    type: "Online",
    description: "Build student-focused products with guidance from industry mentors.",
    link: "https://unstop.com/hackathons"
  }
];

export default function HackathonHub() {
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState<HackathonItem[]>([]);
  const [activeTab, setActiveTab] = useState<HackathonTab>("All");

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setItems(mockHackathons);
      setLoading(false);
    }, 700);
    return () => window.clearTimeout(timer);
  }, []);

  const filteredItems = useMemo(() => {
    if (activeTab === "All") return items;
    return items.filter((item) => item.type === activeTab);
  }, [activeTab, items]);

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Hackathon Hub</p>
          <h2 className="text-2xl font-semibold">Chennai + online hackathons, curated for students</h2>
          <p className="mt-1 text-sm text-muted-foreground">Discover events via Knowafest and Unstop.</p>
        </div>

        <div className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab}
              type="button"
              onClick={() => setActiveTab(tab)}
              className={cn(
                "rounded-full border px-4 py-1 text-xs font-medium transition",
                activeTab === tab
                  ? "border-primary bg-primary/10 text-primary"
                  : "border-border/60 bg-white/70 text-muted-foreground hover:text-foreground dark:bg-white/5"
              )}
            >
              {tab}
            </button>
          ))}
        </div>

        {loading && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, idx) => (
              <CardSkeleton key={`hackathon-skeleton-${idx}`} />
            ))}
          </div>
        )}

        {!loading && filteredItems.length === 0 && (
          <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-border/60 bg-white/70 p-8 text-center dark:bg-white/10">
            <LottieAnimation src="/lottie/empty-state.json" className="h-40 w-40" />
            <p className="text-sm text-muted-foreground">No hackathons available right now.</p>
          </div>
        )}

        {!loading && filteredItems.length > 0 && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {filteredItems.map((item) => (
              <HackathonCard key={item.id} item={item} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

import { useEffect, useMemo, useState } from "react";
import { Bookmark, ExternalLink, Sparkles } from "lucide-react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import CardSkeleton from "@/components/skeletons/CardSkeleton";
import LottieAnimation from "@/components/lottie/LottieAnimation";
import { getJson, postJson } from "@/lib/api";
import { cn } from "@/lib/utils";

const categories = ["All", "Web Development", "AI / ML", "Internships", "Placements", "General"] as const;

type Category = (typeof categories)[number];

type CareerItem = {
  id: string;
  title: string;
  creator: string;
  description: string;
  category: Exclude<Category, "All">;
  sourceUrl: string;
  thumbnail?: string;
  saved?: boolean;
};

type CareerApiItem = {
  id: number;
  title: string;
  creator: string;
  description: string;
  category: string;
  sourceUrl: string;
  thumbnailUrl?: string | null;
  saved: boolean;
};

const CATEGORY_STYLE: Record<Exclude<Category, "All">, string> = {
  "Web Development": "bg-blue-500/15 text-blue-600",
  "AI / ML": "bg-purple-500/15 text-purple-600",
  Internships: "bg-emerald-500/15 text-emerald-600",
  Placements: "bg-orange-500/15 text-orange-600",
  General: "bg-slate-500/15 text-slate-600"
};

type SummaryState = {
  loading: boolean;
  summary?: string;
  error?: string;
};

const toUiCategory = (value: string): Exclude<Category, "All"> => {
  switch (value?.toUpperCase()) {
    case "WEB_DEVELOPMENT":
      return "Web Development";
    case "AI_ML":
      return "AI / ML";
    case "INTERNSHIPS":
      return "Internships";
    case "PLACEMENTS":
      return "Placements";
    case "GENERAL":
      return "General";
    default:
      return "General";
  }
};

export default function CareerFeed() {
  const [activeCategory, setActiveCategory] = useState<Category>("All");
  const [saved, setSaved] = useState<string[]>([]);
  const [summaries, setSummaries] = useState<Record<string, SummaryState>>({});
  const [items, setItems] = useState<CareerItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await getJson<CareerApiItem[]>("/api/career-feed/items");
        const mapped = response.map((item) => ({
          id: String(item.id),
          title: item.title,
          creator: item.creator,
          description: item.description,
          category: toUiCategory(item.category),
          sourceUrl: item.sourceUrl,
          thumbnail: item.thumbnailUrl || undefined,
          saved: item.saved
        }));
        setItems(mapped);
        setSaved(mapped.filter((item) => item.saved).map((item) => item.id));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load feed");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const filtered = useMemo(() => {
    if (activeCategory === "All") return items;
    return items.filter((item) => item.category === activeCategory);
  }, [activeCategory, items]);

  const toggleSave = async (id: string) => {
    try {
      const response = await postJson<CareerApiItem>(`/api/career-feed/items/${id}/save`, {});
      const updated = {
        id: String(response.id),
        title: response.title,
        creator: response.creator,
        description: response.description,
        category: toUiCategory(response.category),
        sourceUrl: response.sourceUrl,
        thumbnail: response.thumbnailUrl || undefined,
        saved: response.saved
      };
      setItems((prev) => prev.map((item) => (item.id === id ? updated : item)));
      setSaved((prev) => (response.saved ? [...new Set([...prev, id])] : prev.filter((item) => item !== id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  };

  const handleSummarize = async (item: CareerItem) => {
    setSummaries((prev) => ({
      ...prev,
      [item.id]: { loading: true }
    }));

    try {
      const response = await postJson<{ result: string }>("/api/ai/summarize", {
        content: `${item.title}. ${item.description}`
      });
      setSummaries((prev) => ({
        ...prev,
        [item.id]: { loading: false, summary: response.result }
      }));
    } catch (err) {
      setSummaries((prev) => ({
        ...prev,
        [item.id]: { loading: false, error: "Summary failed" }
      }));
    }
  };

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-5xl space-y-6">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Career Feed</p>
          <h2 className="text-2xl font-semibold">Roadmaps, internships, and placements</h2>
          <p className="mt-1 text-sm text-muted-foreground">Save insights, summarize with AI, and explore resources fast.</p>
        </div>

        <div className="flex flex-wrap gap-2">
          {categories.map((category) => (
            <button
              key={category}
              type="button"
              onClick={() => setActiveCategory(category)}
              className={cn(
                "rounded-full border px-4 py-1 text-xs font-medium transition",
                activeCategory === category
                  ? "border-primary bg-primary/10 text-primary"
                  : "border-border/60 bg-white/70 text-muted-foreground hover:text-foreground dark:bg-white/5"
              )}
            >
              {category}
            </button>
          ))}
        </div>

        <div className="space-y-4">
          {loading && Array.from({ length: 3 }).map((_, idx) => <CardSkeleton key={`career-skeleton-${idx}`} />)}

          {!loading && filtered.length === 0 && (
            <Card className="flex flex-col items-center justify-center gap-3 p-8 text-center">
              <LottieAnimation src="/lottie/empty-state.json" className="h-40 w-40" />
              <p className="text-sm text-muted-foreground">No content in this category yet.</p>
            </Card>
          )}

          {error && <p className="text-sm text-red-600">{error}</p>}

          {!loading &&
            filtered.map((item) => (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4 }}
              >
                <Card className="space-y-4 p-6">
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div className="space-y-2">
                      <span className={cn("rounded-full px-3 py-1 text-xs font-semibold", CATEGORY_STYLE[item.category])}>
                        {item.category}
                      </span>
                      <h3 className="text-xl font-semibold">{item.title}</h3>
                      <p className="text-sm text-muted-foreground">by {item.creator}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button variant="secondary" size="sm" onClick={() => toggleSave(item.id)} className="gap-1">
                        <Bookmark size={14} /> {saved.includes(item.id) ? "Saved" : "Save"}
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => window.open(item.sourceUrl, "_blank")}
                        className="gap-1"
                      >
                        <ExternalLink size={14} /> View Source
                      </Button>
                    </div>
                  </div>

                  <p className="text-sm text-muted-foreground">{item.description}</p>

                  <div className="flex flex-wrap items-center gap-2">
                    <Button size="sm" onClick={() => handleSummarize(item)} className="gap-2">
                      <Sparkles size={14} /> Summarize
                    </Button>
                    {summaries[item.id]?.loading && <span className="text-xs text-muted-foreground">Summarizing...</span>}
                    {summaries[item.id]?.error && <span className="text-xs text-red-500">{summaries[item.id]?.error}</span>}
                  </div>

                  {summaries[item.id]?.summary && (
                    <div className="rounded-2xl border border-border/60 bg-muted/40 p-4 text-sm">
                      <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">AI Summary</p>
                      <p className="mt-2 whitespace-pre-line">{summaries[item.id]?.summary}</p>
                    </div>
                  )}
                </Card>
              </motion.div>
            ))}
        </div>
      </div>
    </div>
  );
}

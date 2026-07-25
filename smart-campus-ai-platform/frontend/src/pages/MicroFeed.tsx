import { useEffect, useMemo, useRef, useState } from "react";
import { Heart, Sparkles, Bookmark } from "lucide-react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import LottieAnimation from "@/components/lottie/LottieAnimation";
import { getJson, postJson } from "@/lib/api";
import { cn } from "@/lib/utils";

interface MicroItem {
  id: string;
  title: string;
  description: string;
  type: "video" | "text";
  videoUrl?: string;
}

type MicroApiItem = {
  id: number;
  title: string;
  description: string;
  type: string;
  videoUrl?: string | null;
  saved: boolean;
  liked: boolean;
};

export default function MicroFeed() {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [activeIndex, setActiveIndex] = useState(0);
  const [saved, setSaved] = useState<string[]>([]);
  const [liked, setLiked] = useState<string[]>([]);
  const [items, setItems] = useState<MicroItem[]>([]);
  const [explanations, setExplanations] = useState<Record<string, string>>({});
  const [loadingExplain, setLoadingExplain] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await getJson<MicroApiItem[]>("/api/micro-feed/items");
        const mapped = response.map((item) => ({
          id: String(item.id),
          title: item.title,
          description: item.description,
          type: item.type.toUpperCase() === "VIDEO" ? "video" : "text",
          videoUrl: item.videoUrl || undefined
        }));
        setItems(mapped);
        setSaved(response.filter((item) => item.saved).map((item) => String(item.id)));
        setLiked(response.filter((item) => item.liked).map((item) => String(item.id)));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load feed");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const toggleSave = async (id: string) => {
    try {
      const response = await postJson<MicroApiItem>(`/api/micro-feed/items/${id}/save`, {});
      setSaved((prev) => (response.saved ? [...new Set([...prev, id])] : prev.filter((item) => item !== id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  };

  const toggleLike = async (id: string) => {
    try {
      const response = await postJson<MicroApiItem>(`/api/micro-feed/items/${id}/like`, {});
      setLiked((prev) => (response.liked ? [...new Set([...prev, id])] : prev.filter((item) => item !== id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Like failed");
    }
  };

  const handleExplain = async (item: MicroItem) => {
    setLoadingExplain(item.id);
    try {
      const response = await postJson<{ result: string }>("/api/ai/explain", {
        topic: item.title,
        context: item.description
      });
      setExplanations((prev) => ({ ...prev, [item.id]: response.result }));
    } catch (err) {
      setExplanations((prev) => ({ ...prev, [item.id]: "Unable to fetch explanation right now." }));
    } finally {
      setLoadingExplain(null);
    }
  };

  const handleScroll = () => {
    const container = containerRef.current;
    if (!container) return;
    const index = Math.round(container.scrollTop / container.clientHeight);
    setActiveIndex(Math.min(Math.max(index, 0), Math.max(items.length - 1, 0)));
  };

  const scrollToIndex = (index: number) => {
    const container = containerRef.current;
    if (!container) return;
    container.scrollTo({ top: index * container.clientHeight, behavior: "smooth" });
  };

  const visibleItems = useMemo(() => items, [items]);

  if (!loading && visibleItems.length === 0) {
    return (
      <div className="min-h-screen bg-campus px-6 py-10">
        <Card className="mx-auto flex max-w-xl flex-col items-center justify-center gap-3 p-8 text-center">
          <LottieAnimation src="/lottie/empty-state.json" className="h-40 w-40" />
          <p className="text-sm text-muted-foreground">No micro lessons yet.</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-campus px-6 py-6">
      <div className="mx-auto max-w-4xl">
        <div className="mb-4">
          <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Microlearning</p>
          <h2 className="text-2xl font-semibold">Swipe through bite-sized lessons</h2>
        </div>

        <div className="flex items-center justify-between pb-3 text-xs text-muted-foreground">
          <span>Swipe or scroll to move</span>
          <div className="flex gap-2">
            <Button size="sm" variant="secondary" onClick={() => scrollToIndex(Math.max(activeIndex - 1, 0))}>Prev</Button>
            <Button size="sm" variant="secondary" onClick={() => scrollToIndex(Math.min(activeIndex + 1, visibleItems.length - 1))}>Next</Button>
          </div>
        </div>

        <div
          ref={containerRef}
          onScroll={handleScroll}
          className="h-[72vh] snap-y snap-mandatory overflow-y-auto rounded-3xl border border-border/60 bg-black/90 shadow-2xl"
        >
          {loading && (
            <div className="flex h-full items-center justify-center">
              <div className="w-full max-w-lg animate-pulse space-y-4 rounded-3xl border border-white/10 bg-white/5 p-8 text-white/60">
                <div className="h-3 w-24 rounded-full bg-white/10" />
                <div className="h-6 w-3/4 rounded-full bg-white/10" />
                <div className="h-4 w-full rounded-full bg-white/10" />
                <div className="h-4 w-5/6 rounded-full bg-white/10" />
                <div className="flex gap-3 pt-4">
                  <div className="h-9 w-24 rounded-full bg-white/10" />
                  <div className="h-9 w-24 rounded-full bg-white/10" />
                  <div className="h-9 w-24 rounded-full bg-white/10" />
                </div>
              </div>
            </div>
          )}

          {!loading &&
            visibleItems.map((item, index) => (
              <section
                key={item.id}
                className="relative flex h-[72vh] snap-start items-center justify-center px-6 py-10"
              >
                <motion.div
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.45 }}
                  className="w-full max-w-2xl"
                >
                  <Card className="relative overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-slate-900 via-slate-950 to-black p-8 text-white">
                    <div className="space-y-3">
                      <p className="text-xs uppercase tracking-[0.3em] text-white/50">Lesson {index + 1}</p>
                      <h3 className="text-2xl font-semibold">{item.title}</h3>
                      <p className="text-sm text-white/70">{item.description}</p>
                    </div>

                    {item.type === "video" && item.videoUrl && (
                      <div className="mt-6 overflow-hidden rounded-2xl border border-white/10 bg-black/40">
                        <video
                          src={item.videoUrl}
                          autoPlay
                          loop
                          muted
                          playsInline
                          className="h-56 w-full object-cover"
                          onError={() => {
                            setExplanations((prev) => ({
                              ...prev,
                              [item.id]: "Video failed to load. Please check your connection."
                            }));
                          }}
                        />
                      </div>
                    )}

                    {explanations[item.id] && (
                      <div className="mt-5 rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-white/80">
                        {explanations[item.id]}
                      </div>
                    )}

                    <div className="mt-6 flex flex-wrap items-center gap-3">
                      <Button size="sm" variant="secondary" onClick={() => toggleLike(item.id)} className="gap-1">
                        <Heart size={14} className={cn(liked.includes(item.id) && "text-rose-400")} />
                        {liked.includes(item.id) ? "Liked" : "Like"}
                      </Button>
                      <Button size="sm" variant="secondary" onClick={() => toggleSave(item.id)} className="gap-1">
                        <Bookmark size={14} /> {saved.includes(item.id) ? "Saved" : "Save"}
                      </Button>
                      <Button size="sm" onClick={() => handleExplain(item)} className="gap-2">
                        <Sparkles size={14} /> {loadingExplain === item.id ? "Explaining..." : "Explain"}
                      </Button>
                    </div>
                  </Card>
                </motion.div>
              </section>
            ))}
        </div>
        {error && <p className="mt-3 text-sm text-red-500">{error}</p>}
      </div>
    </div>
  );
}

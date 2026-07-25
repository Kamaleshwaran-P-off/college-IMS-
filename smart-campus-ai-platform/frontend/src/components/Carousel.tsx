import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { API_BASE_URL, getJson } from "@/lib/api";

type CarouselItem = {
  id: number;
  url: string;
  createdAt: string;
};

export default function Carousel() {
  const [items, setItems] = useState<CarouselItem[]>([]);
  const [index, setIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    getJson<CarouselItem[]>("/api/carousel")
      .then((data) => {
        if (!active) return;
        setItems(data);
      })
      .catch(() => {
        if (!active) return;
        setItems([]);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (items.length <= 1) return;
    const timer = setInterval(() => {
      setIndex((prev) => (prev + 1) % items.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [items.length]);

  useEffect(() => {
    if (index >= items.length) {
      setIndex(0);
    }
    setImageError(false);
  }, [index, items.length]);

  const activeItem = items[index];
  const imageUrl = useMemo(() => {
    if (!activeItem) return null;
    return activeItem.url.startsWith("http") ? activeItem.url : `${API_BASE_URL}${activeItem.url}`;
  }, [activeItem]);

  if (loading) {
    return (
      <div className="h-40 w-full animate-pulse rounded-3xl border border-slate-200/60 bg-white/70 shadow-sm dark:border-white/10 dark:bg-white/10" />
    );
  }

  if (!activeItem || !imageUrl) {
    return (
      <div className="rounded-3xl border border-slate-200/60 bg-white/70 px-6 py-4 text-sm text-slate-600 shadow-sm dark:border-white/10 dark:bg-white/10 dark:text-white/70">
        No announcements available.
      </div>
    );
  }

  if (imageError) {
    return (
      <div className="rounded-3xl border border-slate-200/60 bg-white/70 px-6 py-4 text-sm text-slate-600 shadow-sm dark:border-white/10 dark:bg-white/10 dark:text-white/70">
        Image failed to load.
      </div>
    );
  }

  return (
    <div className="relative overflow-hidden rounded-3xl border border-slate-200/60 bg-white/70 shadow-[0_30px_60px_-40px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-white/10">
      <AnimatePresence mode="wait">
        <motion.img
          key={activeItem.id}
          src={imageUrl}
          alt="Campus announcement"
          className="h-44 w-full object-cover md:h-52"
          loading="lazy"
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -12 }}
          transition={{ duration: 0.45 }}
          onError={() => setImageError(true)}
        />
      </AnimatePresence>

      <div className="absolute inset-0 bg-gradient-to-r from-slate-900/30 via-slate-900/10 to-transparent" />
      <div className="absolute bottom-4 left-4">
        <p className="text-xs uppercase tracking-[0.3em] text-white/70">Campus Bulletin</p>
        <p className="text-sm font-semibold text-white">Latest updates for everyone</p>
      </div>

      {items.length > 1 && (
        <div className="absolute bottom-4 right-4 flex items-center gap-2">
          {items.map((item, dotIndex) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setIndex(dotIndex)}
              className={`h-2.5 w-2.5 rounded-full transition ${
                dotIndex === index ? "bg-white" : "bg-white/50 hover:bg-white/80"
              }`}
              aria-label={`Go to slide ${dotIndex + 1}`}
            />
          ))}
        </div>
      )}

      {items.length > 1 && (
        <>
          <button
            type="button"
            onClick={() => setIndex((prev) => (prev - 1 + items.length) % items.length)}
            className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-white/80 px-2 py-1 text-xs font-semibold text-slate-700 shadow hover:bg-white"
            aria-label="Previous slide"
          >
            ◀
          </button>
          <button
            type="button"
            onClick={() => setIndex((prev) => (prev + 1) % items.length)}
            className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-white/80 px-2 py-1 text-xs font-semibold text-slate-700 shadow hover:bg-white"
            aria-label="Next slide"
          >
            ▶
          </button>
        </>
      )}
    </div>
  );
}

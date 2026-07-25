import { useCallback, useRef } from "react";
import type { Opportunity } from "@/lib/happenstanceData";

const MAX_ATTEMPTS = 6;

const shuffle = <T,>(items: T[]) => {
  const copy = [...items];
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
};

const getDayOfYear = (date: Date) => {
  const start = new Date(date.getFullYear(), 0, 0);
  const diff = date.getTime() - start.getTime();
  return Math.floor(diff / (1000 * 60 * 60 * 24));
};

export function useRandomizer(items: Opportunity[]) {
  const lastIdRef = useRef<number | null>(null);

  const pickUnique = useCallback(
    (pool: Opportunity[]) => {
      if (pool.length === 0) return null;
      if (pool.length === 1) {
        lastIdRef.current = pool[0].id;
        return pool[0];
      }
      let candidate: Opportunity | null = null;
      for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt += 1) {
        const idx = Math.floor(Math.random() * pool.length);
        const next = pool[idx];
        if (next.id !== lastIdRef.current) {
          candidate = next;
          break;
        }
      }
      candidate = candidate ?? pool[Math.floor(Math.random() * pool.length)];
      lastIdRef.current = candidate.id;
      return candidate;
    },
    [items]
  );

  const pickRandom = useCallback(() => pickUnique(items), [items, pickUnique]);

  const pickInterest = useCallback(
    (interest: string | null) => {
      if (!interest) return pickRandom();
      const pool = items.filter((item) => item.domain === interest);
      return pickUnique(pool.length ? pool : items);
    },
    [items, pickRandom, pickUnique]
  );

  const pickOutOfComfort = useCallback(
    (interest: string | null) => {
      if (!interest) return pickRandom();
      const pool = items.filter((item) => item.domain !== interest);
      return pickUnique(pool.length ? pool : items);
    },
    [items, pickRandom, pickUnique]
  );

  const pickSmart = useCallback(
    (interest: string | null) => {
      const roll = Math.random();
      if (roll < 0.4) return pickRandom();
      if (roll < 0.7) return pickInterest(interest);
      return pickOutOfComfort(interest);
    },
    [pickInterest, pickOutOfComfort, pickRandom]
  );

  const getDailyPick = useCallback(() => {
    if (!items.length) return null;
    const dayIndex = getDayOfYear(new Date()) % items.length;
    return items[dayIndex];
  }, [items]);

  const getExploreList = useCallback(
    (interest: string | null, count = 4) => {
      const pool = interest
        ? items.filter((item) => item.domain !== interest)
        : items;
      return shuffle(pool).slice(0, count);
    },
    [items]
  );

  return {
    pickRandom,
    pickInterest,
    pickOutOfComfort,
    pickSmart,
    getDailyPick,
    getExploreList
  };
}

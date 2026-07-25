import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import type { ToastPayload, ToastVariant } from "@/lib/toast";

type ToastItem = ToastPayload & { id: string };

const VARIANT_STYLES: Record<ToastVariant, string> = {
  success: "border-emerald-200/70 bg-emerald-50 text-emerald-800",
  error: "border-rose-200/70 bg-rose-50 text-rose-800",
  info: "border-sky-200/70 bg-sky-50 text-sky-800",
};

export default function ToastHost() {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  useEffect(() => {
    const handler = (event: Event) => {
      const detail = (event as CustomEvent<ToastPayload>).detail;
      if (!detail?.title) return;
      const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
      const toast: ToastItem = {
        id,
        title: detail.title,
        description: detail.description,
        variant: detail.variant ?? "info",
        durationMs: detail.durationMs ?? 3200
      };
      setToasts((prev) => [...prev, toast]);
      window.setTimeout(() => {
        setToasts((prev) => prev.filter((item) => item.id !== id));
      }, toast.durationMs);
    };
    window.addEventListener("app-toast", handler);
    return () => window.removeEventListener("app-toast", handler);
  }, []);

  const content = useMemo(
    () =>
      toasts.map((toast) => {
        const style = VARIANT_STYLES[toast.variant ?? "info"];
        return (
          <motion.div
            key={toast.id}
            initial={{ opacity: 0, y: 12, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 8, scale: 0.98 }}
            transition={{ duration: 0.2 }}
            className={`w-full max-w-sm rounded-2xl border px-4 py-3 shadow-lg ${style}`}
          >
            <p className="text-sm font-semibold">{toast.title}</p>
            {toast.description && (
              <p className="mt-1 text-xs text-slate-600">{toast.description}</p>
            )}
          </motion.div>
        );
      }),
    [toasts]
  );

  return (
    <div className="pointer-events-none fixed right-4 top-4 z-[70] flex flex-col gap-3">
      <AnimatePresence>{content}</AnimatePresence>
    </div>
  );
}

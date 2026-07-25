import { motion } from "framer-motion";
import type { ReactNode } from "react";

type CardProps = {
  title: string;
  description: string;
  icon: ReactNode;
  onClick?: () => void;
  accent?: string;
};

export default function Card({ title, description, icon, onClick, accent }: CardProps) {
  return (
    <motion.button
      type="button"
      whileHover={{ scale: 1.03, y: -4 }}
      whileTap={{ scale: 0.98 }}
      onClick={onClick}
      className="group relative flex h-full w-full flex-col items-start gap-4 rounded-2xl border border-slate-200/70 bg-white/70 p-6 text-left shadow-[0_20px_60px_-30px_rgba(15,23,42,0.25)] backdrop-blur-xl transition dark:border-white/10 dark:bg-white/10 dark:shadow-[0_20px_60px_-30px_rgba(15,23,42,0.7)]"
    >
      <div
        className="flex h-12 w-12 items-center justify-center rounded-2xl border border-slate-200/60 bg-slate-900/5 text-slate-900 dark:border-white/10 dark:bg-white/10 dark:text-white"
        style={{ boxShadow: accent ? `0 12px 30px -12px ${accent}` : undefined }}
      >
        {icon}
      </div>
      <div className="space-y-2">
        <h3 className="text-lg font-semibold text-slate-900 dark:text-white">{title}</h3>
        <p className="text-sm text-slate-600 dark:text-white/70">{description}</p>
      </div>
      <span className="absolute inset-x-6 bottom-6 h-px bg-gradient-to-r from-transparent via-slate-900/20 to-transparent opacity-0 transition group-hover:opacity-100 dark:via-white/40" />
    </motion.button>
  );
}

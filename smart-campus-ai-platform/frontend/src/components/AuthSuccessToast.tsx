import { AnimatePresence, motion } from "framer-motion";

type AuthSuccessToastProps = {
  open: boolean;
  title: string;
  description?: string;
};

export default function AuthSuccessToast({ open, title, description }: AuthSuccessToastProps) {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="pointer-events-none fixed inset-0 z-50 flex items-center justify-center px-4"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 10 }}
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="w-full max-w-sm rounded-2xl border border-emerald-200/70 bg-white/90 px-5 py-4 text-center shadow-[0_20px_40px_-24px_rgba(16,185,129,0.7)] backdrop-blur"
          >
            <p className="text-sm font-semibold text-emerald-700">{title}</p>
            {description && <p className="mt-1 text-xs text-emerald-600">{description}</p>}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

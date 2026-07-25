import LottieAnimation from "@/components/lottie/LottieAnimation";
import { cn } from "@/lib/utils";

type SuccessStateProps = {
  title: string;
  description?: string;
  className?: string;
};

export default function SuccessState({ title, description, className }: SuccessStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 rounded-3xl border border-emerald-200/60 bg-emerald-50 px-6 py-8 text-center shadow-sm dark:border-emerald-400/30 dark:bg-emerald-500/10",
        className
      )}
    >
      <LottieAnimation src="/lottie/success.json" className="h-32 w-32" />
      <div>
        <h3 className="text-base font-semibold text-emerald-700 dark:text-emerald-200">{title}</h3>
        {description && <p className="mt-1 text-sm text-emerald-600 dark:text-emerald-200/70">{description}</p>}
      </div>
    </div>
  );
}

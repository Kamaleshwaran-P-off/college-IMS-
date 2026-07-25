import LottieAnimation from "@/components/lottie/LottieAnimation";
import { cn } from "@/lib/utils";

type EmptyStateProps = {
  title: string;
  description?: string;
  className?: string;
  action?: React.ReactNode;
};

export default function EmptyState({ title, description, className, action }: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 rounded-3xl border border-border/60 bg-white/70 px-6 py-10 text-center shadow-sm dark:bg-white/10",
        className
      )}
    >
      <LottieAnimation src="/lottie/empty.json" className="h-36 w-36" />
      <div>
        <h3 className="text-base font-semibold text-foreground">{title}</h3>
        {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
      </div>
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}

import Skeleton from "@/components/skeletons/Skeleton";
import { cn } from "@/lib/utils";

type CardSkeletonProps = {
  className?: string;
  lines?: number;
};

export default function CardSkeleton({ className, lines = 3 }: CardSkeletonProps) {
  return (
    <div className={cn("rounded-3xl border border-border/60 bg-white/70 p-6 shadow-sm dark:bg-white/10", className)}>
      <Skeleton className="h-5 w-32 rounded-xl" />
      <Skeleton className="mt-4 h-7 w-48 rounded-xl" />
      <div className="mt-5 space-y-3">
        {Array.from({ length: lines }).map((_, index) => (
          <Skeleton key={`line-${index}`} className="h-3 w-full rounded-lg" />
        ))}
      </div>
    </div>
  );
}

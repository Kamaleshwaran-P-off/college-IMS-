import Skeleton from "@/components/skeletons/Skeleton";
import { cn } from "@/lib/utils";

type TableSkeletonProps = {
  rows?: number;
  columns?: number;
  className?: string;
};

export default function TableSkeleton({ rows = 5, columns = 3, className }: TableSkeletonProps) {
  return (
    <div className={cn("space-y-3", className)}>
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <div
          key={`row-${rowIndex}`}
          className="grid items-center gap-3 rounded-2xl border border-border/60 bg-white/70 px-4 py-3 dark:bg-white/5"
          style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
        >
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Skeleton key={`cell-${rowIndex}-${colIndex}`} className="h-3 w-full rounded-lg" />
          ))}
        </div>
      ))}
    </div>
  );
}

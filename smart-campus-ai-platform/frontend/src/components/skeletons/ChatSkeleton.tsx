import Skeleton from "@/components/skeletons/Skeleton";

export default function ChatSkeleton() {
  return (
    <div className="space-y-4">
      <div className="flex justify-start">
        <Skeleton className="h-12 w-2/3 rounded-2xl" />
      </div>
      <div className="flex justify-end">
        <Skeleton className="h-10 w-1/2 rounded-2xl" />
      </div>
      <div className="flex justify-start">
        <Skeleton className="h-16 w-3/4 rounded-2xl" />
      </div>
      <div className="flex justify-end">
        <Skeleton className="h-12 w-1/3 rounded-2xl" />
      </div>
    </div>
  );
}

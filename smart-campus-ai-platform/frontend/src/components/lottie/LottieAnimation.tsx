import { Suspense, lazy, useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import Skeleton from "@/components/skeletons/Skeleton";

const LottiePlayer = lazy(() => import("lottie-react"));

type LottieAnimationProps = {
  src: string;
  loop?: boolean;
  autoplay?: boolean;
  className?: string;
  onComplete?: () => void;
};

export default function LottieAnimation({
  src,
  loop = false,
  autoplay = true,
  className,
  onComplete
}: LottieAnimationProps) {
  const [animationData, setAnimationData] = useState<any | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let active = true;
    setFailed(false);
    setAnimationData(null);

    fetch(src)
      .then((res) => res.json())
      .then((data) => {
        if (!active) return;
        setAnimationData(data);
      })
      .catch(() => {
        if (!active) return;
        setFailed(true);
      });

    return () => {
      active = false;
    };
  }, [src]);

  if (failed) {
    return <Skeleton className={cn("h-40 w-40 rounded-3xl", className)} />;
  }

  if (!animationData) {
    return <Skeleton className={cn("h-40 w-40 rounded-3xl", className)} />;
  }

  return (
    <Suspense fallback={<Skeleton className={cn("h-40 w-40 rounded-3xl", className)} />}>
      <LottiePlayer
        animationData={animationData}
        loop={loop}
        autoplay={autoplay}
        className={className}
        onComplete={onComplete}
      />
    </Suspense>
  );
}

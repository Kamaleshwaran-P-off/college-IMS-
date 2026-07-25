import LottieAnimation from "@/components/lottie/LottieAnimation";

export default function PageLoader() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      <LottieAnimation src="/lottie/loading.json" loop className="h-40 w-40" />
      <p className="text-sm text-muted-foreground">Loading your workspace...</p>
    </div>
  );
}

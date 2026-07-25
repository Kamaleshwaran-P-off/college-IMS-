import { Button } from "@/components/ui/button";

export default function AccessDenied() {
  return (
    <div className="min-h-screen bg-campus flex items-center justify-center px-6">
      <div className="max-w-md rounded-2xl border border-border/60 bg-white/80 p-8 text-center shadow-lg dark:bg-white/10">
        <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Access Denied</p>
        <h2 className="mt-2 text-2xl font-semibold">Students only</h2>
        <p className="mt-2 text-sm text-muted-foreground">
          Hackathon Hub is available exclusively for student accounts.
        </p>
        <Button className="mt-6" onClick={() => window.location.assign("/dashboard")}>Go to Dashboard</Button>
      </div>
    </div>
  );
}

import { CalendarDays, MapPin } from "lucide-react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

export interface HackathonItem {
  id: string;
  title: string;
  date?: string;
  location: string;
  platform: "Knowafest" | "Unstop";
  type: "Chennai" | "Online";
  description?: string;
  link: string;
}

export default function HackathonCard({ item }: { item: HackathonItem }) {
  const platformStyle =
    item.platform === "Knowafest"
      ? "bg-blue-500/15 text-blue-600"
      : "bg-purple-500/15 text-purple-600";
  const typeStyle =
    item.type === "Chennai"
      ? "bg-emerald-500/15 text-emerald-600"
      : "bg-orange-500/15 text-orange-600";

  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
      <Card className="group flex h-full flex-col gap-4 rounded-2xl border border-border/60 bg-white/80 p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-lg dark:bg-white/10">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2 text-xs font-semibold">
            <span className={`rounded-full px-3 py-1 ${platformStyle}`}>{item.platform}</span>
            <span className={`rounded-full px-3 py-1 ${typeStyle}`}>{item.type}</span>
          </div>
          <h3 className="text-lg font-semibold text-foreground">{item.title}</h3>
          <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
            {item.date && (
              <span className="flex items-center gap-1">
                <CalendarDays size={14} /> {item.date}
              </span>
            )}
            <span className="flex items-center gap-1">
              <MapPin size={14} /> {item.location}
            </span>
          </div>
          {item.description && <p className="text-sm text-muted-foreground">{item.description}</p>}
        </div>
        <div className="mt-auto">
          <Button
            className="w-full"
            onClick={() => window.open(item.link, "_blank")}
          >
            Apply Now
          </Button>
        </div>
      </Card>
    </motion.div>
  );
}

import { Card } from "@/components/ui/card";
import type { CourseTopic } from "@/components/learning/learningFlowData";

interface TopicViewerProps {
  topic: CourseTopic;
}

export default function TopicViewer({ topic }: TopicViewerProps) {
  return (
    <Card className="flex h-full flex-col overflow-hidden border-border/60 bg-white/85 shadow-sm dark:bg-white/10">
      <div className="border-b border-border/60 px-5 py-4">
        <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Topic Content</p>
        <h3 className="mt-2 text-xl font-semibold">{topic.title}</h3>
        <p className="mt-2 text-sm text-muted-foreground">{topic.description}</p>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto px-5 py-5 text-sm leading-7 text-foreground/90">
        {topic.content.map((paragraph, index) => (
          <div key={`${topic.title}-content-${index}`} className="rounded-2xl border border-border/50 bg-muted/30 p-4">
            <p>{paragraph}</p>
          </div>
        ))}
      </div>
    </Card>
  );
}

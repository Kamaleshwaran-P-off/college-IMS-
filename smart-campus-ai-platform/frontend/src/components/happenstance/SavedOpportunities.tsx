import OpportunityCard from "@/components/happenstance/OpportunityCard";
import type { Opportunity } from "@/lib/happenstanceData";

type SavedOpportunitiesProps = {
  items: Opportunity[];
  onRemove: (item: Opportunity) => void;
  onApply: (item: Opportunity) => void;
};

export default function SavedOpportunities({ items, onRemove, onApply }: SavedOpportunitiesProps) {
  if (items.length === 0) {
    return (
      <div className="rounded-2xl border border-border/60 bg-white/70 p-6 text-sm text-muted-foreground dark:bg-white/10">
        No saved opportunities yet. Bookmark the ones that feel exciting.
      </div>
    );
  }

  return (
    <div className="grid gap-4 md:grid-cols-2">
      {items.map((item) => (
        <OpportunityCard
          key={item.id}
          item={item}
          saved
          onSave={onRemove}
          onApply={onApply}
        />
      ))}
    </div>
  );
}

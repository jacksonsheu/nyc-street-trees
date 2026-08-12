import { useTreeInteractions } from '@/hooks/useTreeInteractions';
import type { TreeInteraction } from '@/types/tree';

function formatTimestamp(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function describeInteraction(interaction: TreeInteraction): string {
  if (interaction.type === 'WATERING') {
    const liters = interaction.details.liters;
    const userId = interaction.details.userId;
    return `Watered with ${liters}L by ${userId}`;
  }
  const issueType = interaction.details.issueType;
  const requestedBy = interaction.details.requestedBy;
  return `${issueType} reported by ${requestedBy}`;
}

/** Chronological list of prior interactions (waterings, maintenance requests) for a tree. */
export function InteractionHistory({ treeId }: { treeId: string }) {
  const { data: interactions, isLoading, isError } = useTreeInteractions(treeId);

  if (isLoading) {
    return <p className="muted-text">Loading activity…</p>;
  }

  if (isError) {
    return <p className="form-error">Could not load activity history.</p>;
  }

  if (!interactions || interactions.length === 0) {
    return <p className="muted-text">No activity logged yet. Be the first!</p>;
  }

  const sorted = [...interactions].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
  );

  return (
    <ul className="interaction-history">
      {sorted.map((interaction) => (
        <li key={interaction.id} className={`interaction-history__item interaction-history__item--${interaction.type.toLowerCase()}`}>
          <span className="interaction-history__icon" aria-hidden="true">
            {interaction.type === 'WATERING' ? '💧' : '🛠️'}
          </span>
          <div>
            <p className="interaction-history__description">{describeInteraction(interaction)}</p>
            <p className="interaction-history__timestamp">{formatTimestamp(interaction.createdAt)}</p>
          </div>
        </li>
      ))}
    </ul>
  );
}

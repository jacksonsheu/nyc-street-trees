import { useState } from 'react';
import { useTreeDetails } from '@/hooks/useTreeDetails';
import { InteractionHistory } from './InteractionHistory';
import { MaintenanceRequestForm } from './MaintenanceRequestForm';
import { WateringForm } from './WateringForm';

type ActiveAction = 'none' | 'watering' | 'maintenance';

interface TreeDetailPanelProps {
  treeId: string;
  onClose: () => void;
}

function DetailRow({ label, value }: { label: string; value: string | number | null | undefined }) {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  return (
    <div className="detail-row">
      <span className="detail-row__label">{label}</span>
      <span className="detail-row__value">{value}</span>
    </div>
  );
}

/**
 * The source dataset packs problems as a comma-separated list of unspaced
 * CamelCase tags (e.g. "MetalGrates,RootOther"). Split and re-space them into
 * readable, comma-separated words for display.
 */
function formatProblems(raw: string | null | undefined): string | undefined {
  if (!raw || raw === 'None') {
    return undefined;
  }
  return raw
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
    .map((tag) => tag.replace(/([a-z])([A-Z])/g, '$1 $2'))
    .join(', ');
}

/** Side panel showing tree details, action buttons, and interaction history for the selected tree. */
export function TreeDetailPanel({ treeId, onClose }: TreeDetailPanelProps) {
  const { data: tree, isLoading, isError } = useTreeDetails(treeId);
  const [activeAction, setActiveAction] = useState<ActiveAction>('none');

  function resetAction() {
    setActiveAction('none');
  }

  return (
    <aside className="detail-panel" aria-label="Tree details">
      <button className="detail-panel__close" onClick={onClose} aria-label="Close tree details">
        ✕
      </button>

      {isLoading && <p className="muted-text">Loading tree details…</p>}
      {isError && <p className="form-error">Could not load this tree's details.</p>}

      {tree && (
        <>
          <header className="detail-panel__header">
            <h2>{tree.commonName ?? 'Unknown species'}</h2>
            {tree.latinName && <p className="detail-panel__subtitle">{tree.latinName}</p>}
          </header>

          <section className="detail-panel__section">
            <DetailRow label="Health" value={tree.health} />
            <DetailRow label="Status" value={tree.status} />
            <DetailRow
              label="Trunk diameter"
              value={tree.diameterAtBreastHeightInches ? `${tree.diameterAtBreastHeightInches} in` : undefined}
            />
            <DetailRow label="Known problems" value={formatProblems(tree.problems)} />
            <DetailRow label="Address" value={tree.address} />
            <DetailRow label="Neighborhood" value={tree.neighborhoodTabulationAreaName} />
          </section>

          {activeAction === 'none' && (
            <section className="detail-panel__actions">
              <button className="button button--primary" onClick={() => setActiveAction('watering')}>
                💧 Mark as watered
              </button>
              <button className="button button--secondary" onClick={() => setActiveAction('maintenance')}>
                🛠️ Report an issue
              </button>
            </section>
          )}

          {activeAction === 'watering' && (
            <WateringForm treeId={tree.treeId} onSubmitted={resetAction} onCancel={resetAction} />
          )}

          {activeAction === 'maintenance' && (
            <MaintenanceRequestForm treeId={tree.treeId} onSubmitted={resetAction} onCancel={resetAction} />
          )}

          <section className="detail-panel__section">
            <h3 className="detail-panel__section-title">Recent activity</h3>
            <InteractionHistory treeId={tree.treeId} />
          </section>
        </>
      )}
    </aside>
  );
}

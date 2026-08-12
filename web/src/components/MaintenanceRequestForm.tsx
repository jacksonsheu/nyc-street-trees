import { useState, type FormEvent } from 'react';
import { useSubmitMaintenanceRequest } from '@/hooks/useTreeInteractions';
import { getInteractionErrorMessage } from '@/api/client';
import { useToast } from './Toast';

const REMEMBERED_USER_ID_KEY = 'nyc-trees.userId';

const ISSUE_TYPES = [
  'Fallen branch',
  'Trunk damage',
  'Pest or disease',
  'Sidewalk hazard',
  'Needs pruning',
  'Other',
] as const;

interface MaintenanceRequestFormProps {
  treeId: string;
  onSubmitted: () => void;
  onCancel: () => void;
}

/** Form for submitting a maintenance request against a tree. */
export function MaintenanceRequestForm({ treeId, onSubmitted, onCancel }: MaintenanceRequestFormProps) {
  const [requestedBy, setRequestedBy] = useState(() => localStorage.getItem(REMEMBERED_USER_ID_KEY) ?? '');
  const [issueType, setIssueType] = useState<string>(ISSUE_TYPES[0]);
  const [description, setDescription] = useState('');
  const mutation = useSubmitMaintenanceRequest(treeId);
  const { showToast } = useToast();

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!requestedBy.trim() || !description.trim()) {
      return;
    }

    localStorage.setItem(REMEMBERED_USER_ID_KEY, requestedBy.trim());
    mutation.mutate(
      { requestedBy: requestedBy.trim(), issueType, description: description.trim() },
      {
        onSuccess: () => {
          showToast('Maintenance request submitted. Thanks for reporting this!');
          onSubmitted();
        },
        onError: (error) => {
          showToast(
            getInteractionErrorMessage(error, 'Could not submit the request. Please try again.'),
            'error',
          );
        },
      },
    );
  }

  return (
    <form className="interaction-form" onSubmit={handleSubmit}>
      <h3>Report a maintenance issue</h3>
      <label className="form-field">
        <span>Your name or ID</span>
        <input
          type="text"
          value={requestedBy}
          onChange={(event) => setRequestedBy(event.target.value)}
          placeholder="e.g. jane_doe"
          required
        />
      </label>
      <label className="form-field">
        <span>Issue type</span>
        <select value={issueType} onChange={(event) => setIssueType(event.target.value)}>
          {ISSUE_TYPES.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </label>
      <label className="form-field">
        <span>Description</span>
        <textarea
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          placeholder="Describe what you observed…"
          rows={3}
          required
        />
      </label>
      {mutation.isError && <p className="form-error">Something went wrong. Please try again.</p>}
      <div className="form-actions">
        <button type="button" className="button button--ghost" onClick={onCancel} disabled={mutation.isPending}>
          Cancel
        </button>
        <button type="submit" className="button button--primary" disabled={mutation.isPending}>
          {mutation.isPending ? 'Submitting…' : 'Submit request'}
        </button>
      </div>
    </form>
  );
}

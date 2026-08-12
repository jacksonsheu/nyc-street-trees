import { useState, type FormEvent } from 'react';
import { useSubmitWatering } from '@/hooks/useTreeInteractions';
import { getInteractionErrorMessage } from '@/api/client';
import { useToast } from './Toast';

const REMEMBERED_USER_ID_KEY = 'nyc-trees.userId';

interface WateringFormProps {
  treeId: string;
  onSubmitted: () => void;
  onCancel: () => void;
}

/** Form for logging a watering event against a tree. */
export function WateringForm({ treeId, onSubmitted, onCancel }: WateringFormProps) {
  const [userId, setUserId] = useState(() => localStorage.getItem(REMEMBERED_USER_ID_KEY) ?? '');
  const [liters, setLiters] = useState('10');
  const mutation = useSubmitWatering(treeId);
  const { showToast } = useToast();

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const parsedLiters = Number(liters);
    if (!userId.trim() || !(parsedLiters > 0)) {
      return;
    }

    localStorage.setItem(REMEMBERED_USER_ID_KEY, userId.trim());
    mutation.mutate(
      { userId: userId.trim(), liters: parsedLiters },
      {
        onSuccess: () => {
          showToast('Thanks for watering this tree! 💧');
          onSubmitted();
        },
        onError: (error) => {
          showToast(
            getInteractionErrorMessage(error, 'Could not log the watering event. Please try again.'),
            'error',
          );
        },
      },
    );
  }

  return (
    <form className="interaction-form" onSubmit={handleSubmit}>
      <h3>Log a watering</h3>
      <label className="form-field">
        <span>Your name or ID</span>
        <input
          type="text"
          value={userId}
          onChange={(event) => setUserId(event.target.value)}
          placeholder="e.g. jane_doe"
          required
        />
      </label>
      <label className="form-field">
        <span>Water applied (liters)</span>
        <input
          type="number"
          min="0.1"
          step="0.1"
          value={liters}
          onChange={(event) => setLiters(event.target.value)}
          required
        />
      </label>
      {mutation.isError && <p className="form-error">Something went wrong. Please try again.</p>}
      <div className="form-actions">
        <button type="button" className="button button--ghost" onClick={onCancel} disabled={mutation.isPending}>
          Cancel
        </button>
        <button type="submit" className="button button--primary" disabled={mutation.isPending}>
          {mutation.isPending ? 'Submitting…' : 'Confirm watering'}
        </button>
      </div>
    </form>
  );
}

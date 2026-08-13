import { useEffect, useState, type FormEvent } from 'react';
import { useAddressSearch } from '@/hooks/useAddressSearch';
import { useDelayedLoading } from '@/hooks/useDelayedLoading';
import type { GeoPosition } from '@/types/tree';
import { Spinner } from './Spinner';

const ERROR_AUTO_DISMISS_MS = 3000;

interface AddressSearchProps {
  onLocationFound: (position: GeoPosition, label: string) => void;
}

/** Search bar for jumping the map to a specific street address. */
export function AddressSearch({ onLocationFound }: AddressSearchProps) {
  const [query, setQuery] = useState('');
  const [showError, setShowError] = useState(false);
  const mutation = useAddressSearch();
  const showSearchSpinner = useDelayedLoading(mutation.isPending);

  // Dismiss the error banner automatically after a few seconds, or
  // immediately on the next click anywhere, whichever happens first.
  useEffect(() => {
    if (!showError) return;

    const timer = setTimeout(() => setShowError(false), ERROR_AUTO_DISMISS_MS);
    const handleClick = () => setShowError(false);
    document.addEventListener('click', handleClick);

    return () => {
      clearTimeout(timer);
      document.removeEventListener('click', handleClick);
    };
  }, [showError]);

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) return;

    setShowError(false);
    mutation.mutate(trimmed, {
      onSuccess: (result) => onLocationFound(result.position, result.label),
      onError: () => setShowError(true),
    });
  }

  function handleClear() {
    setQuery('');
    setShowError(false);
    mutation.reset();
  }

  return (
    <form className="address-search" onSubmit={handleSubmit}>
      <div className="address-search__input-wrap">
        <input
          type="text"
          className="address-search__input"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search by address…"
          aria-label="Search by address"
        />
        {query && (
          <button
            type="button"
            className="address-search__clear"
            onClick={handleClear}
            aria-label="Clear address search"
          >
            ✕
          </button>
        )}
        <button
          type="submit"
          className="address-search__submit"
          disabled={mutation.isPending || !query.trim()}
          aria-label="Search"
        >
          {showSearchSpinner ? <Spinner /> : '🔍'}
        </button>
      </div>
      {showError && <p className="address-search__error">{(mutation.error as Error).message}</p>}
    </form>
  );
}

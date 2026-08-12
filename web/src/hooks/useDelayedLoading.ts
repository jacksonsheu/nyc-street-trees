import { useEffect, useState } from 'react';

/**
 * Debounces a loading flag so brief loads (e.g. a fast API response) never
 * flash a spinner on screen. Only reports `true` if `isLoading` stays true
 * for longer than `delayMs`; reports `false` immediately as soon as loading
 * ends.
 */
export function useDelayedLoading(isLoading: boolean, delayMs = 1000): boolean {
  const [showLoading, setShowLoading] = useState(false);

  useEffect(() => {
    if (!isLoading) {
      setShowLoading(false);
      return;
    }

    const timer = setTimeout(() => setShowLoading(true), delayMs);
    return () => clearTimeout(timer);
  }, [isLoading, delayMs]);

  return showLoading;
}

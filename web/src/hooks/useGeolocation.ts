import { useCallback, useEffect, useState } from 'react';
import type { GeoPosition } from '@/types/tree';

/** Default map center: Manhattan, used when geolocation is unavailable or denied. */
export const DEFAULT_POSITION: GeoPosition = { latitude: 40.7831, longitude: -73.9712 };

interface GeolocationState {
  position: GeoPosition;
  isUsingDeviceLocation: boolean;
  isLoading: boolean;
  error: string | null;
}

/**
 * Resolves the user's current location via the browser geolocation API,
 * falling back to a sensible NYC default when permission is denied or
 * the API is unavailable (e.g. non-HTTPS contexts, older browsers).
 *
 * Exposes `refresh()` so callers (e.g. a "use my location" button) can
 * re-request a fresh fix rather than reusing the initial one-time result.
 */
export function useGeolocation(): GeolocationState & { refresh: () => void } {
  const [state, setState] = useState<GeolocationState>({
    position: DEFAULT_POSITION,
    isUsingDeviceLocation: false,
    isLoading: true,
    error: null,
  });

  const locate = useCallback(() => {
    if (!('geolocation' in navigator)) {
      setState((prev) => ({ ...prev, isLoading: false, error: 'Geolocation is not supported by this browser.' }));
      return;
    }

    setState((prev) => ({ ...prev, isLoading: true, error: null }));

    navigator.geolocation.getCurrentPosition(
      (result) => {
        setState({
          // Always create a new object so callers relying on reference
          // identity (e.g. to force a map recenter) reliably see a change.
          position: { latitude: result.coords.latitude, longitude: result.coords.longitude },
          isUsingDeviceLocation: true,
          isLoading: false,
          error: null,
        });
      },
      (geoError) => {
        setState({
          position: DEFAULT_POSITION,
          isUsingDeviceLocation: false,
          isLoading: false,
          error: geoError.message,
        });
      },
      // maximumAge: 0 forces a fresh fix on manual refresh instead of an
      // implicitly cached one, so "use my location" reflects real movement.
      { enableHighAccuracy: true, timeout: 10_000, maximumAge: 0 },
    );
  }, []);

  useEffect(() => {
    locate();
  }, [locate]);

  return { ...state, refresh: locate };
}

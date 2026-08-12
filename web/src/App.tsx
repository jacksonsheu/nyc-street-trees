import { useEffect, useMemo, useState } from 'react';
import 'leaflet/dist/leaflet.css';
import { AddressSearch } from '@/components/AddressSearch';
import { MapView } from '@/components/MapView';
import { Spinner } from '@/components/Spinner';
import { TreeDetailPanel } from '@/components/TreeDetailPanel';
import { useDelayedLoading } from '@/hooks/useDelayedLoading';
import { useGeolocation } from '@/hooks/useGeolocation';
import { useNearbyTrees } from '@/hooks/useNearbyTrees';
import type { GeoPosition } from '@/types/tree';

const SEARCH_RADIUS_METERS = 500;
const SEARCH_RESULT_LIMIT = 500;
/** Minimum movement (in degrees, roughly ~40m) before offering to search a newly panned area. */
const MOVE_THRESHOLD_DEGREES = 0.0004;

function distanceExceeds(a: GeoPosition, b: GeoPosition, threshold: number): boolean {
  return Math.abs(a.latitude - b.latitude) > threshold || Math.abs(a.longitude - b.longitude) > threshold;
}

export function App() {
  const geolocation = useGeolocation();
  const [searchCenter, setSearchCenter] = useState<GeoPosition | null>(null);
  const [pendingCenter, setPendingCenter] = useState<GeoPosition | null>(null);
  const [selectedTreeId, setSelectedTreeId] = useState<string | null>(null);
  // Tracks that the user explicitly asked to recenter, so we know to apply
  // the next resolved geolocation fix even if it's numerically the same
  // position as before (e.g. user hasn't moved).
  const [awaitingRecenter, setAwaitingRecenter] = useState(false);
  const showRecenterSpinner = useDelayedLoading(awaitingRecenter);

  // Once geolocation resolves (device location or default), start the first search there.
  const effectiveSearchCenter = searchCenter ?? (geolocation.isLoading ? null : geolocation.position);

  const nearbyTreesQuery = useNearbyTrees(
    effectiveSearchCenter
      ? { ...effectiveSearchCenter, radiusMeters: SEARCH_RADIUS_METERS, limit: SEARCH_RESULT_LIMIT }
      : null,
  );

  const showSearchAreaButton = useMemo(() => {
    if (!pendingCenter || !effectiveSearchCenter) return false;
    return distanceExceeds(pendingCenter, effectiveSearchCenter, MOVE_THRESHOLD_DEGREES);
  }, [pendingCenter, effectiveSearchCenter]);

  useEffect(() => {
    if (awaitingRecenter && !geolocation.isLoading) {
      setSearchCenter(geolocation.position);
      setPendingCenter(null);
      setAwaitingRecenter(false);
    }
  }, [awaitingRecenter, geolocation.isLoading, geolocation.position]);

  function handleMapMoved(center: GeoPosition) {
    setPendingCenter(center);
  }

  function handleSearchThisArea() {
    if (pendingCenter) {
      setSearchCenter(pendingCenter);
    }
  }

  function handleRecenter() {
    setAwaitingRecenter(true);
    geolocation.refresh();
  }

  function handleAddressFound(position: GeoPosition) {
    setSearchCenter(position);
    setPendingCenter(null);
    setSelectedTreeId(null);
  }

  const trees = nearbyTreesQuery.data ?? [];

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>🌳 NYC Street Trees</h1>
        <AddressSearch onLocationFound={handleAddressFound} />
        <button className="button button--ghost button--small" onClick={handleRecenter} disabled={awaitingRecenter}>
          <span className="button__icon">{showRecenterSpinner ? <Spinner /> : '📍'}</span>
          Use my location
        </button>
      </header>

      <div className="app-body">
        <div className="map-container">
          {effectiveSearchCenter && (
            <MapView
              center={effectiveSearchCenter}
              userPosition={geolocation.isUsingDeviceLocation ? geolocation.position : null}
              trees={trees}
              selectedTreeId={selectedTreeId}
              onSelectTree={setSelectedTreeId}
              onMapMoved={handleMapMoved}
            />
          )}

          {showSearchAreaButton && (
            <button className="button button--primary search-area-button" onClick={handleSearchThisArea}>
              Search this area
            </button>
          )}

          {nearbyTreesQuery.isLoading && <div className="map-status-banner">Loading nearby trees…</div>}
          {nearbyTreesQuery.isError && (
            <div className="map-status-banner map-status-banner--error">
              Could not load trees. Check that the API is running.
            </div>
          )}
          {!nearbyTreesQuery.isLoading && !nearbyTreesQuery.isError && trees.length === 0 && effectiveSearchCenter && (
            <div className="map-status-banner">No trees found nearby. Try panning and searching another area.</div>
          )}
        </div>

        {selectedTreeId && (
          <TreeDetailPanel treeId={selectedTreeId} onClose={() => setSelectedTreeId(null)} />
        )}
      </div>
    </div>
  );
}

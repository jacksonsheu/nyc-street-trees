import { useQuery } from '@tanstack/react-query';
import { fetchNearbyTrees, type NearbyTreesQuery } from '@/api/trees';

/**
 * Fetches trees near a map position with caching keyed by rounded coordinates
 * so panning by tiny amounts doesn't trigger redundant network requests.
 */
export function useNearbyTrees(query: NearbyTreesQuery | null) {
  return useQuery({
    queryKey: ['nearby-trees', query && roundQueryKey(query)],
    queryFn: () => fetchNearbyTrees(query as NearbyTreesQuery),
    enabled: query !== null,
    staleTime: 30_000,
  });
}

function roundQueryKey(query: NearbyTreesQuery) {
  return {
    latitude: Math.round(query.latitude * 1000) / 1000,
    longitude: Math.round(query.longitude * 1000) / 1000,
    radiusMeters: query.radiusMeters,
    limit: query.limit,
  };
}

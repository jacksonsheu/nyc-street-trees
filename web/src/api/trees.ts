import { apiRequest } from './client';
import type {
  MaintenanceRequestInput,
  TreeInteraction,
  TreeSummary,
  WateringInput,
} from '@/types/tree';

export interface NearbyTreesQuery {
  latitude: number;
  longitude: number;
  radiusMeters?: number;
  limit?: number;
}

/** Fetches trees near a given map position. */
export function fetchNearbyTrees(query: NearbyTreesQuery): Promise<TreeSummary[]> {
  return apiRequest<TreeSummary[]>('/trees/nearby', {
    params: {
      latitude: query.latitude,
      longitude: query.longitude,
      radiusMeters: query.radiusMeters,
      limit: query.limit,
    },
  });
}

/** Fetches full details for a single tree by ID. */
export function fetchTreeDetails(treeId: string): Promise<TreeSummary> {
  return apiRequest<TreeSummary>(`/trees/${encodeURIComponent(treeId)}`);
}

/** Fetches the interaction history for a tree. */
export function fetchTreeInteractions(treeId: string): Promise<TreeInteraction[]> {
  return apiRequest<TreeInteraction[]>(`/trees/${encodeURIComponent(treeId)}/interactions`);
}

/** Submits a maintenance request for a tree. */
export function submitMaintenanceRequest(
  treeId: string,
  input: MaintenanceRequestInput,
): Promise<TreeInteraction> {
  return apiRequest<TreeInteraction>(`/trees/${encodeURIComponent(treeId)}/maintenance-requests`, {
    method: 'POST',
    body: input,
  });
}

/** Submits a watering event for a tree. */
export function submitWatering(treeId: string, input: WateringInput): Promise<TreeInteraction> {
  return apiRequest<TreeInteraction>(`/trees/${encodeURIComponent(treeId)}/waterings`, {
    method: 'POST',
    body: input,
  });
}

import type { components } from './api.generated';

/**
 * Domain types derived from the generated OpenAPI contract (see
 * shared/contracts/openapi.json — regenerate via `npm run generate:types`
 * in this package after changing backend DTOs/controllers, using
 * `../scripts/generate-openapi-spec.sh` to refresh the spec first).
 *
 * A few fields are narrowed from optional to required below to reflect
 * invariants the backend guarantees but that the OpenAPI schema alone
 * can't express (springdoc can't see that Java response DTOs are never
 * partially null in practice): `/trees/nearby` only ever returns trees
 * with resolved coordinates, and a successfully created interaction
 * always has an id/type/timestamp.
 */

type GeneratedTreeSummary = components['schemas']['TreeSummary'];
type GeneratedTreeInteraction = components['schemas']['TreeInteraction'];

export type TreeSummary = Omit<GeneratedTreeSummary, 'treeId' | 'latitude' | 'longitude'> & {
  treeId: string;
  latitude: number;
  longitude: number;
};

export type InteractionType = NonNullable<GeneratedTreeInteraction['type']>;

export type TreeInteraction = Omit<GeneratedTreeInteraction, 'id' | 'treeId' | 'type' | 'createdAt' | 'details'> & {
  id: string;
  treeId: string;
  type: InteractionType;
  createdAt: string;
  details: Record<string, unknown>;
};

export type MaintenanceRequestInput = components['schemas']['MaintenanceRequestInput'];
export type WateringInput = components['schemas']['WateringInput'];

/** Not part of the API contract — a plain lat/lng pair used for map centers. */
export interface GeoPosition {
  latitude: number;
  longitude: number;
}

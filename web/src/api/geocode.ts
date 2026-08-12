import type { GeoPosition } from '@/types/tree';

/**
 * Address geocoding via OpenStreetMap's Nominatim service (the same provider
 * behind our map tiles, so no additional API key/account is needed for this
 * POC). Only suitable for light, ad-hoc usage per Nominatim's usage policy —
 * a production app with real traffic should move to a paid/dedicated
 * geocoding provider with its own rate limits.
 */

export interface GeocodeResult {
  position: GeoPosition;
  label: string;
}

export async function geocodeAddress(query: string): Promise<GeocodeResult> {
  const url = new URL('https://nominatim.openstreetmap.org/search');
  url.searchParams.set('format', 'jsonv2');
  url.searchParams.set('limit', '1');
  // Bias toward NYC since that's the only data this app has, without
  // strictly excluding results outside the box.
  const biasedQuery = /new york|nyc|ny\b/i.test(query) ? query : `${query}, New York, NY`;
  url.searchParams.set('q', biasedQuery);

  const response = await fetch(url.toString(), {
    headers: { Accept: 'application/json' },
  });

  if (!response.ok) {
    throw new Error('Address lookup failed. Please try again.');
  }

  const results = (await response.json()) as Array<{ lat: string; lon: string; display_name: string }>;
  const [first] = results;
  if (!first) {
    throw new Error('No matching address found.');
  }

  return {
    position: { latitude: Number(first.lat), longitude: Number(first.lon) },
    label: first.display_name,
  };
}

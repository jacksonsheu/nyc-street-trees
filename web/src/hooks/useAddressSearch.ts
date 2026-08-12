import { useMutation } from '@tanstack/react-query';
import { geocodeAddress } from '@/api/geocode';

/** Geocodes a free-text address into a map position via Nominatim. */
export function useAddressSearch() {
  return useMutation({
    mutationFn: geocodeAddress,
  });
}

import { useQuery } from '@tanstack/react-query';
import { fetchTreeDetails } from '@/api/trees';

/** Fetches full details for a single tree, used when a marker is selected. */
export function useTreeDetails(treeId: string | null) {
  return useQuery({
    queryKey: ['tree-details', treeId],
    queryFn: () => fetchTreeDetails(treeId as string),
    enabled: treeId !== null,
  });
}

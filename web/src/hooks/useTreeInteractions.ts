import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  fetchTreeInteractions,
  submitMaintenanceRequest,
  submitWatering,
} from '@/api/trees';
import type { MaintenanceRequestInput, WateringInput } from '@/types/tree';

/** Fetches the interaction history (maintenance requests, waterings) for a tree. */
export function useTreeInteractions(treeId: string | null) {
  return useQuery({
    queryKey: ['tree-interactions', treeId],
    queryFn: () => fetchTreeInteractions(treeId as string),
    enabled: treeId !== null,
  });
}

/** Submits a maintenance request and refreshes the interaction history on success. */
export function useSubmitMaintenanceRequest(treeId: string | null) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: MaintenanceRequestInput) => submitMaintenanceRequest(treeId as string, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tree-interactions', treeId] });
    },
  });
}

/** Submits a watering event and refreshes the interaction history on success. */
export function useSubmitWatering(treeId: string | null) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: WateringInput) => submitWatering(treeId as string, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tree-interactions', treeId] });
    },
  });
}

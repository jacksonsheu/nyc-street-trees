package com.nyctrees.backend.tree.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for submitting a tree maintenance request.
 *
 * @param requestedBy user identifier for the requester
 * @param issueType high-level category of the issue
 * @param description free-form issue details
 */
public record MaintenanceRequestInput(
        @NotBlank @Size(max = 100) String requestedBy,
        @NotBlank @Size(max = 50) String issueType,
        @NotBlank @Size(max = 1000) String description
) {
}

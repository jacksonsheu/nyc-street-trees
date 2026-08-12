package com.nyctrees.backend.tree.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for recording a watering event for a tree.
 *
 * @param userId user identifier for who watered the tree
 * @param liters amount of water applied in liters
 */
public record WateringInput(
        @NotBlank @Size(max = 100) String userId,
        @Positive double liters
) {
}

package com.nyctrees.backend.tree.model;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable interaction event recorded against a tree.
 *
 * @param id unique interaction identifier
 * @param treeId tree identifier this interaction belongs to
 * @param type interaction category
 * @param createdAt timestamp when the interaction was recorded
 * @param details additional interaction-specific attributes
 */
public record TreeInteraction(
        String id,
        String treeId,
        InteractionType type,
        Instant createdAt,
        Map<String, Object> details
) {
}

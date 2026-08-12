package com.nyctrees.backend.tree.service;

import com.nyctrees.backend.tree.dto.MaintenanceRequestInput;
import com.nyctrees.backend.tree.dto.WateringInput;
import com.nyctrees.backend.tree.exception.InteractionCapacityExceededException;
import com.nyctrees.backend.tree.model.InteractionType;
import com.nyctrees.backend.tree.model.TreeInteraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for capturing and reading user interactions associated with trees.
 */
@Service
public class TreeInteractionService {
    private final Map<String, List<TreeInteraction>> interactionsByTree = new ConcurrentHashMap<>();
    private final AtomicInteger totalInteractionCount = new AtomicInteger(0);
    private final int maxTotalInteractions;

    /**
     * Creates the service with a configurable global cap on stored interactions,
     * protecting low-tier demo hosts from unbounded in-memory growth.
     *
     * @param maxTotalInteractions maximum number of interactions retained across all trees
     */
    public TreeInteractionService(@Value("${app.interactions.max-total:1000}") int maxTotalInteractions) {
        this.maxTotalInteractions = maxTotalInteractions;
    }

    /**
     * Records a maintenance request interaction for a tree.
     *
     * @param treeId tree identifier
     * @param request maintenance request payload
     * @return persisted interaction event
     */
    public TreeInteraction submitMaintenanceRequest(String treeId, MaintenanceRequestInput request) {
        Map<String, Object> details = new LinkedHashMap<>();
        // Preserve request context in a consistent key/value structure.
        details.put("requestedBy", request.requestedBy());
        details.put("issueType", request.issueType());
        details.put("description", request.description());
        details.put("status", "OPEN");

        return appendInteraction(treeId, InteractionType.MAINTENANCE_REQUEST, details);
    }

    /**
     * Records a watering interaction for a tree.
     *
     * @param treeId tree identifier
     * @param request watering payload
     * @return persisted interaction event
     */
    public TreeInteraction submitWatering(String treeId, WateringInput request) {
        Map<String, Object> details = new LinkedHashMap<>();
        // Keep interaction details payload shape consistent across events.
        details.put("userId", request.userId());
        details.put("liters", request.liters());

        return appendInteraction(treeId, InteractionType.WATERING, details);
    }

    /**
     * Returns all interactions recorded for a tree.
     *
     * @param treeId tree identifier
     * @return immutable snapshot of interactions for the given tree
     */
    public List<TreeInteraction> getInteractions(String treeId) {
        return List.copyOf(interactionsByTree.getOrDefault(treeId, List.of()));
    }

    /**
     * Creates and stores a new interaction event in memory.
     *
     * @param treeId tree identifier
     * @param type interaction type
     * @param details interaction details payload
     * @return newly created interaction
     */
    private TreeInteraction appendInteraction(String treeId, InteractionType type, Map<String, Object> details) {
        reserveCapacityOrThrow();

        TreeInteraction interaction = new TreeInteraction(
                UUID.randomUUID().toString(),
                treeId,
                type,
                Instant.now(),
                // Store immutable details to prevent accidental mutation after creation.
                Map.copyOf(details)
        );
        // Maintain per-tree append-only history for this POC.
        interactionsByTree
                .computeIfAbsent(treeId, ignored -> new CopyOnWriteArrayList<>())
                .add(interaction);
        return interaction;
    }

    /**
     * Atomically reserves capacity for a new interaction, throwing when the configured
     * demo limit has already been reached. Uses a CAS loop so concurrent submissions
     * can't both slip past the check before either one increments the counter.
     */
    private void reserveCapacityOrThrow() {
        int current;
        do {
            current = totalInteractionCount.get();
            if (current >= maxTotalInteractions) {
                throw new InteractionCapacityExceededException(
                        "This demo has reached its interaction limit. Thanks for checking it out "
                                + "\u2014 please try again later."
                );
            }
        } while (!totalInteractionCount.compareAndSet(current, current + 1));
    }
}

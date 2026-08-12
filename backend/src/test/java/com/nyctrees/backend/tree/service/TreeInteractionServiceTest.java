package com.nyctrees.backend.tree.service;

import com.nyctrees.backend.tree.dto.MaintenanceRequestInput;
import com.nyctrees.backend.tree.dto.WateringInput;
import com.nyctrees.backend.tree.exception.InteractionCapacityExceededException;
import com.nyctrees.backend.tree.model.InteractionType;
import com.nyctrees.backend.tree.model.TreeInteraction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for in-memory tree interaction recording behavior.
 */
class TreeInteractionServiceTest {

    /**
     * Verifies interactions are stored independently per tree ID.
     */
    @Test
    void storesAndReadsInteractionsPerTree() {
        TreeInteractionService service = new TreeInteractionService(1000);

        service.submitWatering("tree-1", new WateringInput("sam", 2.0));
        service.submitMaintenanceRequest("tree-1", new MaintenanceRequestInput("sam", "pests", "Noticed insects"));
        service.submitWatering("tree-2", new WateringInput("kim", 1.0));

        List<TreeInteraction> treeOneInteractions = service.getInteractions("tree-1");
        List<TreeInteraction> treeTwoInteractions = service.getInteractions("tree-2");

        assertEquals(2, treeOneInteractions.size());
        assertEquals(1, treeTwoInteractions.size());
        assertEquals(InteractionType.WATERING, treeOneInteractions.get(0).type());
        assertEquals(InteractionType.MAINTENANCE_REQUEST, treeOneInteractions.get(1).type());
        assertFalse(treeOneInteractions.get(0).id().isBlank());
    }

    /**
     * Verifies that once the configured global cap is reached, further
     * submissions are rejected instead of growing the in-memory store further.
     */
    @Test
    void rejectsSubmissionsOnceCapacityReached() {
        TreeInteractionService service = new TreeInteractionService(2);

        service.submitWatering("tree-1", new WateringInput("sam", 2.0));
        service.submitWatering("tree-1", new WateringInput("sam", 2.0));

        assertThrows(
                InteractionCapacityExceededException.class,
                () -> service.submitWatering("tree-1", new WateringInput("sam", 2.0))
        );
        assertEquals(2, service.getInteractions("tree-1").size());
    }
}


package com.nyctrees.backend.tree.controller;

import com.nyctrees.backend.tree.dto.MaintenanceRequestInput;
import com.nyctrees.backend.tree.dto.TreeSummary;
import com.nyctrees.backend.tree.model.InteractionType;
import com.nyctrees.backend.tree.model.TreeInteraction;
import com.nyctrees.backend.tree.service.NycTreeService;
import com.nyctrees.backend.tree.service.TreeInteractionService;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TreeController} endpoint delegation behavior.
 */
class TreeControllerTest {

    /**
     * Ensures the test endpoint returns the expected heartbeat message.
     */
    @Test
    void testEndpointReturnsRunningMessage() {
        NycTreeService nycTreeService = mock(NycTreeService.class);
        TreeInteractionService interactionService = mock(TreeInteractionService.class);
        TreeController controller = new TreeController(nycTreeService, interactionService);

        assertEquals("NYC Trees API is running", controller.test());
    }

    /**
     * Ensures nearby lookup delegates to {@link NycTreeService}.
     */
    @Test
    void getNearbyTreesDelegatesToService() {
        NycTreeService nycTreeService = mock(NycTreeService.class);
        TreeInteractionService interactionService = mock(TreeInteractionService.class);
        TreeController controller = new TreeController(nycTreeService, interactionService);

        List<TreeSummary> expected = List.of(new TreeSummary(
                "12345", "red maple", "Acer rubrum", 16, "Good", "Alive",
                "Trunk wire; Branch lights", "Midtown-Midtown South",
                "Manhattan", "350 5th Ave", "10118", 40.7484, -73.9857
        ));
        when(nycTreeService.findNearbyTrees(40.7484, -73.9857, 250, 25)).thenReturn(expected);

        List<TreeSummary> result = controller.getNearbyTrees(40.7484, -73.9857, 250, 25);

        assertEquals(expected, result);
        verify(nycTreeService).findNearbyTrees(40.7484, -73.9857, 250, 25);
    }

    /**
     * Ensures maintenance requests are delegated to the interaction service.
     */
    @Test
    void submitMaintenanceRequestDelegatesToService() {
        NycTreeService nycTreeService = mock(NycTreeService.class);
        TreeInteractionService interactionService = mock(TreeInteractionService.class);
        TreeController controller = new TreeController(nycTreeService, interactionService);

        MaintenanceRequestInput input = new MaintenanceRequestInput(
                "alex", "broken branch", "Large branch hanging over sidewalk"
        );
        TreeInteraction expected = new TreeInteraction(
                "itx-1",
                "12345",
                InteractionType.MAINTENANCE_REQUEST,
                Instant.parse("2026-01-01T12:00:00Z"),
                Map.of("issueType", "broken branch")
        );
        when(interactionService.submitMaintenanceRequest("12345", input)).thenReturn(expected);

        TreeInteraction result = controller.submitMaintenanceRequest("12345", input);

        assertEquals(expected, result);
        verify(interactionService).submitMaintenanceRequest("12345", input);
    }

    /**
     * Ensures single-tree detail lookup delegates to {@link NycTreeService}.
     */
    @Test
    void getTreeDetailsDelegatesToService() {
        NycTreeService nycTreeService = mock(NycTreeService.class);
        TreeInteractionService interactionService = mock(TreeInteractionService.class);
        TreeController controller = new TreeController(nycTreeService, interactionService);

        TreeSummary expected = new TreeSummary(
                "12345", "red maple", "Acer rubrum", 16, "Good", "Alive",
                "Trunk wire; Branch lights", "Midtown-Midtown South",
                "Manhattan", "350 5th Ave", "10118", 40.7484, -73.9857
        );
        when(nycTreeService.getTreeDetails("12345")).thenReturn(expected);

        var response = controller.getTreeDetails("12345");

        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
        verify(nycTreeService).getTreeDetails("12345");
    }
}

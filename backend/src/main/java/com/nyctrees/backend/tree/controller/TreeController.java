package com.nyctrees.backend.tree.controller;

import com.nyctrees.backend.tree.dto.MaintenanceRequestInput;
import com.nyctrees.backend.tree.dto.TreeSummary;
import com.nyctrees.backend.tree.dto.WateringInput;
import com.nyctrees.backend.tree.model.TreeInteraction;
import com.nyctrees.backend.tree.service.NycTreeService;
import com.nyctrees.backend.tree.service.TreeInteractionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for tree discovery, detail retrieval, and interaction submission.
 */
@Validated
@RestController
@RequestMapping("/api/trees")
public class TreeController {
    private final NycTreeService nycTreeService;
    private final TreeInteractionService treeInteractionService;

    /**
     * Creates the controller with tree read and interaction services.
     *
     * @param nycTreeService service for retrieving tree data
     * @param treeInteractionService service for recording and reading interactions
     */
    public TreeController(NycTreeService nycTreeService, TreeInteractionService treeInteractionService) {
        this.nycTreeService = nycTreeService;
        this.treeInteractionService = treeInteractionService;
    }

    /**
     * Lightweight endpoint to confirm the API is available.
     *
     * @return service status message
     */
    @GetMapping("/test")
    public String test() {
        return "NYC Trees API is running";
    }

    /**
     * Returns trees near the provided latitude/longitude.
     *
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusMeters search radius in meters
     * @param limit max number of trees to return
     * @return nearby tree summaries
     */
    @GetMapping("/nearby")
    public List<TreeSummary> getNearbyTrees(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @RequestParam(defaultValue = "250") @Positive @Max(2000) int radiusMeters,
            @RequestParam(defaultValue = "25") @Min(1) @Max(500) int limit
    ) {
        return nycTreeService.findNearbyTrees(latitude, longitude, radiusMeters, limit);
    }

    /**
     * Returns one tree by ID for map click/detail views.
     *
     * @param treeId NYC tree identifier
     * @return 200 with tree details, or 404 when the tree is not found
     */
    @GetMapping("/{treeId}")
    public ResponseEntity<TreeSummary> getTreeDetails(@PathVariable String treeId) {
        TreeSummary treeDetails = nycTreeService.getTreeDetails(treeId);
        if (treeDetails == null) {
            // Explicitly communicate when an ID is valid format but not present in the dataset.
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(treeDetails);
    }

    /**
     * Records a maintenance request interaction for the given tree.
     *
     * @param treeId NYC tree identifier
     * @param request maintenance request payload
     * @return created interaction event
     */
    @PostMapping("/{treeId}/maintenance-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public TreeInteraction submitMaintenanceRequest(
            @PathVariable String treeId,
            @Valid @RequestBody MaintenanceRequestInput request
    ) {
        return treeInteractionService.submitMaintenanceRequest(treeId, request);
    }

    /**
     * Records a watering interaction for the given tree.brew install nodeynode -vnpm -v
     *
     * @param treeId NYC tree identifier
     * @param request watering payload
     * @return created interaction event
     */
    @PostMapping("/{treeId}/waterings")
    @ResponseStatus(HttpStatus.CREATED)
    public TreeInteraction submitWatering(
            @PathVariable String treeId,
            @Valid @RequestBody WateringInput request
    ) {
        return treeInteractionService.submitWatering(treeId, request);
    }

    /**
     * Returns interaction history recorded for a tree.
     *
     * @param treeId NYC tree identifier
     * @return interaction list for the given tree
     */
    @GetMapping("/{treeId}/interactions")
    public List<TreeInteraction> getInteractions(@PathVariable String treeId) {
        return treeInteractionService.getInteractions(treeId);
    }
}

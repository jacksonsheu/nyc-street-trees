package com.nyctrees.backend.tree.service;

import com.nyctrees.backend.tree.client.NycOpenDataTreeClient;
import com.nyctrees.backend.tree.dto.TreeSummary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service exposing tree-related read operations to controllers.
 */
@Service
public class NycTreeService {
    private final NycOpenDataTreeClient nycOpenDataTreeClient;

    /**
     * Creates the service with the NYC Open Data client dependency.
     *
     * @param nycOpenDataTreeClient API client used to retrieve tree data
     */
    public NycTreeService(NycOpenDataTreeClient nycOpenDataTreeClient) {
        this.nycOpenDataTreeClient = nycOpenDataTreeClient;
    }

    /**
     * Finds trees near a map position.
     *
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusMeters search radius in meters
     * @param limit max trees to return
     * @return nearby tree summaries
     */
    public List<TreeSummary> findNearbyTrees(double latitude, double longitude, int radiusMeters, int limit) {
        // Keep controller logic thin by delegating external data lookup to the client.
        return nycOpenDataTreeClient.findNearbyTrees(latitude, longitude, radiusMeters, limit);
    }

    /**
     * Retrieves details for a single tree.
     *
     * @param treeId NYC tree identifier
     * @return tree details, or null when not found
     */
    public TreeSummary getTreeDetails(String treeId) {
        return nycOpenDataTreeClient.findTreeById(treeId);
    }
}

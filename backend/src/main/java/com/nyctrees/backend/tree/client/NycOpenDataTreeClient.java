package com.nyctrees.backend.tree.client;

import com.nyctrees.backend.tree.dto.TreeSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Low-level client for reading street tree data from NYC Open Data.
 */
@Component
public class NycOpenDataTreeClient {
    private static final String DATASET_PATH = "/resource/uvpi-gqnh.json";
    private static final String SELECT_COLUMNS =
            "tree_id,spc_common,spc_latin,tree_dbh,health,status,problems,nta_name,boroname,address,zipcode,latitude,longitude";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates the client with HTTP and JSON dependencies.
     *
     * @param restClientBuilder builder used to create the scoped HTTP client
     * @param objectMapper mapper used to parse JSON responses
     */
    public NycOpenDataTreeClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl("https://data.cityofnewyork.us").build();
        this.objectMapper = objectMapper;
    }

    /**
     * Finds trees within a radius of the given coordinate.
     *
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusMeters search radius in meters
     * @param limit max trees to return
     * @return nearby tree summaries sorted by distance
     */
    public List<TreeSummary> findNearbyTrees(double latitude, double longitude, int radiusMeters, int limit) {
        // Convert the radius to a rough bounding box to keep remote query size reasonable.
        double latDelta = radiusMeters / 111_320.0;
        double lonDelta = radiusMeters / (111_320.0 * Math.max(0.1, Math.cos(Math.toRadians(latitude))));
        String whereClause = String.format(
                Locale.US,
                "latitude between %.6f and %.6f and longitude between %.6f and %.6f",
                latitude - latDelta,
                latitude + latDelta,
                longitude - lonDelta,
                longitude + lonDelta
        );
        String responseBody = queryTrees(whereClause, Math.min(5000, Math.max(limit * 5, 100)));

        // Apply exact distance filtering and ordering after coarse server-side bounding.
        return parseTrees(responseBody).stream()
                .filter(tree -> tree.latitude() != null && tree.longitude() != null)
                .filter(tree -> distanceMeters(latitude, longitude, tree.latitude(), tree.longitude()) <= radiusMeters)
                .sorted(Comparator.comparingDouble(tree ->
                        distanceMeters(latitude, longitude, tree.latitude(), tree.longitude())))
                .limit(limit)
                .toList();
    }

    /**
     * Looks up a single tree by NYC tree identifier.
     *
     * @param treeId NYC tree identifier
     * @return tree summary or null when not found
     */
    public TreeSummary findTreeById(String treeId) {
        // Escape single quotes for a safe SoQL filter literal.
        String safeTreeId = treeId.replace("'", "''");
        String whereClause = "tree_id = '" + safeTreeId + "'";
        String responseBody = queryTrees(whereClause, 1);
        return parseTrees(responseBody).stream().findFirst().orElse(null);
    }

    /**
     * Executes a SoQL query against the configured dataset.
     *
     * @param whereClause SoQL where clause
     * @param limit max rows to return
     * @return raw JSON response body
     */
    private String queryTrees(String whereClause, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(DATASET_PATH)
                        .queryParam("$select", SELECT_COLUMNS)
                        .queryParam("$where", whereClause)
                        .queryParam("$limit", limit)
                        .build())
                .retrieve()
                .body(String.class);
    }

    /**
     * Parses the dataset JSON response into {@link TreeSummary} objects.
     *
     * @param json raw JSON array
     * @return parsed tree summaries
     */
    private List<TreeSummary> parseTrees(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }

            List<TreeSummary> trees = new ArrayList<>(root.size());
            for (JsonNode treeNode : root) {
                // Normalize selected dataset fields into the API response schema.
                trees.add(new TreeSummary(
                        nodeText(treeNode, "tree_id"),
                        nodeText(treeNode, "spc_common"),
                        nodeText(treeNode, "spc_latin"),
                        nodeInteger(treeNode, "tree_dbh"),
                        nodeText(treeNode, "health"),
                        nodeText(treeNode, "status"),
                        nodeText(treeNode, "problems"),
                        nodeText(treeNode, "nta_name"),
                        nodeText(treeNode, "boroname"),
                        nodeText(treeNode, "address"),
                        nodeText(treeNode, "zipcode"),
                        nodeDouble(treeNode, "latitude"),
                        nodeDouble(treeNode, "longitude")
                ));
            }
            return trees;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse tree data from NYC Open Data", exception);
        }
    }

    /**
     * Reads a nullable string field from a JSON node.
     *
     * @param node source node
     * @param field field name to read
     * @return string value or null
     */
    private static String nodeText(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        return valueNode.asText();
    }

    /**
     * Reads a nullable numeric field and converts it to {@link Double}.
     *
     * @param node source node
     * @param field field name to read
     * @return double value or null
     */
    private static Double nodeDouble(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText();
        if (value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value);
    }

    /**
     * Reads a nullable numeric field and converts it to {@link Integer}.
     *
     * @param node source node
     * @param field field name to read
     * @return integer value or null
     */
    private static Integer nodeInteger(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (valueNode.isInt()) {
            return valueNode.asInt();
        }
        String value = valueNode.asText();
        if (value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    /**
     * Computes great-circle distance using the Haversine formula.
     *
     * @param lat1 first point latitude
     * @param lon1 first point longitude
     * @param lat2 second point latitude
     * @param lon2 second point longitude
     * @return distance in meters
     */
    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusMeters = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }
}

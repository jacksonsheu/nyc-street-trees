package com.nyctrees.backend.tree.dto;

/**
 * Response DTO representing tree attributes returned to API consumers.
 *
 * @param treeId NYC tree identifier
 * @param commonName common species name
 * @param latinName scientific species name
 * @param diameterAtBreastHeightInches trunk diameter at breast height in inches
 * @param health health rating from the dataset
 * @param status tree status from the dataset
 * @param problems observed problems/flags for the tree
 * @param neighborhoodTabulationAreaName NTA name for neighborhood context
 * @param borough borough name
 * @param address nearest address provided by the dataset
 * @param zipCode ZIP code
 * @param latitude latitude coordinate
 * @param longitude longitude coordinate
 */
public record TreeSummary(
        String treeId,
        String commonName,
        String latinName,
        Integer diameterAtBreastHeightInches,
        String health,
        String status,
        String problems,
        String neighborhoodTabulationAreaName,
        String borough,
        String address,
        String zipCode,
        Double latitude,
        Double longitude
) {
}

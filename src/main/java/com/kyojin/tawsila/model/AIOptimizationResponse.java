package com.kyojin.tawsila.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIOptimizationResponse {

    /**
     * The sequence of Delivery IDs representing the optimal route.
     */
    @JsonProperty("ordered_ids")
    private List<Long> orderedIds;

    @JsonProperty("route_summary")
    private RouteSummary routeSummary;

    @JsonProperty("key_metrics")
    private KeyMetrics keyMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteSummary {
        @JsonProperty("total_stops")
        private Integer totalStops;

        @JsonProperty("estimated_duration_minutes")
        private Integer estimatedDurationMinutes;

        @JsonProperty("total_distance_km")
        private Double totalDistanceKm;

        @JsonProperty("capacity_utilization_percentage")
        private Double capacityUtilization;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyMetrics {
        @JsonProperty("total_waiting_time_minutes")
        private Integer waitingTime;

        @JsonProperty("overtime_risk")
        private String overtimeRisk;

        @JsonProperty("constraint_violations")
        private List<String> constraintViolations;
    }
}
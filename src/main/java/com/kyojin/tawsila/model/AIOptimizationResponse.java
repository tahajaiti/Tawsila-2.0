package com.kyojin.tawsila.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIOptimizationResponse {
    /**
     * The ONLY thing we need: the sorted list of IDs.
     */
    @JsonProperty("ordered_ids")
    private List<Long> orderedIds;
}
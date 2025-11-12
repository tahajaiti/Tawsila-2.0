package com.kyojin.tawsila.criteria;

import lombok.Data;

@Data
public class VehicleSearchCriteria {
    private String type;
    private Double minPayload;
    private Double minVolume;
    private Integer minDeliveries;
}
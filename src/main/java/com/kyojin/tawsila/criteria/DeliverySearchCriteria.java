package com.kyojin.tawsila.criteria;

import com.kyojin.tawsila.enums.DeliveryStatus;
import lombok.Data;

@Data
public class DeliverySearchCriteria {

    private DeliveryStatus status;   // PENDING, IN_PROGRESS, COMPLETED

    private Double minWeightKg;      // minimum weight filter
    private Double maxWeightKg;      // maximum weight filter

    private Double minVolumeM3;      // minimum volume filter
    private Double maxVolumeM3;      // maximum volume filter

    private String timeSlot;         // ex: "09:00-12:00"

    private Long tourId;             // deliveries belonging to a specific tour
    private Long customerId;         // deliveries belonging to a specific customer
}

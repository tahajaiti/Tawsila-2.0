package com.kyojin.tawsila.criteria;

import com.kyojin.tawsila.enums.TourStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TourSearchCriteria {
    private LocalDate startDate;     // search from this date
    private LocalDate endDate;       // search until this date
    private TourStatus status;       // filter by status (PLANNED, COMPLETED, etc.)
    private Long vehicleId;          // filter by assigned vehicle
    private Integer minDeliveries;   // minimum deliveries per tour
    private Integer maxDeliveries;   // optional upper limit if needed
}

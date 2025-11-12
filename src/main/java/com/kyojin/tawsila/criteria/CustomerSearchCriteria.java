package com.kyojin.tawsila.criteria;

import lombok.Data;

import java.time.LocalTime;

@Data
public class CustomerSearchCriteria {

    private String name;                   // search by name
    private String address;                // filter by address or city keyword

    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;

    private LocalTime preferredTimeSlotStart; // earliest preferred time
    private LocalTime preferredTimeSlotEnd;   // latest preferred time
}

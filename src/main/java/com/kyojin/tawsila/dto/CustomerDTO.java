package com.kyojin.tawsila.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalTime preferredTimeSlotStart;
    private LocalTime preferredTimeSlotEnd;
}

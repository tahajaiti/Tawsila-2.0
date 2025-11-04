package com.kyojin.tawsila.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime preferredTimeSlotStart;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime preferredTimeSlotEnd;
}

package com.kyojin.tawsila.entity;

import com.kyojin.tawsila.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(name = "max_weight_kg", nullable = false)
    private double maxWeightKg;

    @Column(name = "max_volume_m3", nullable = false)
    private double maxVolumeM3;

    @Column(name = "max_deliveries", nullable = false)
    private int maxDeliveries;

    public Vehicle(VehicleType type) {
        this.maxWeightKg = type.getMaxWeightKg();
        this.maxVolumeM3 = type.getMaxVolumeM3();
        this.maxDeliveries = type.getMaxDeliveries();
    }
}


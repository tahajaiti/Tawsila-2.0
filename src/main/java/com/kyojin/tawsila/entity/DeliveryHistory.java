package com.kyojin.tawsila.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="delivery_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @NotNull
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @NotNull
    @Column(name = "planned_time", nullable = false)
    private LocalTime plannedTime;

    @NotNull
    @Column(name = "actual_time", nullable = false)
    private LocalTime actualTime;

    @Column(name = "delay_minutes")
    private Long delayMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @PrePersist
    @PreUpdate
    protected void computeDelayAndDay() {
        if (actualTime != null && plannedTime != null) {
            this.delayMinutes = Duration.between(plannedTime, actualTime).toMinutes();
        }
        if (deliveryDate != null) {
            this.dayOfWeek = deliveryDate.getDayOfWeek();
        }

        if (deliveryDate == null) deliveryDate = LocalDate.now();
        if (dayOfWeek == null) dayOfWeek = deliveryDate.getDayOfWeek();
    }

}

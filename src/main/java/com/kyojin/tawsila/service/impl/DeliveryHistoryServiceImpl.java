package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.entity.DeliveryHistory;
import com.kyojin.tawsila.entity.Tour;
import com.kyojin.tawsila.repository.DeliveryHistoryRepository;
import com.kyojin.tawsila.service.DeliveryHistoryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
public class DeliveryHistoryServiceImpl implements DeliveryHistoryService {

    private final DeliveryHistoryRepository deliveryHistoryRepository;

    @Autowired
    public DeliveryHistoryServiceImpl(DeliveryHistoryRepository deliveryHistoryRepository) {
        this.deliveryHistoryRepository = deliveryHistoryRepository;
    }

    @Override
    @Transactional
    public void logDeliveryHistory(Tour tour) {
        var deliveries = tour.getDeliveries();
        if (deliveries == null || deliveries.isEmpty()) {
            log.warn("No deliveries to archive for tour {}", tour.getId());
            return;
        }

        deliveries.forEach(delivery -> {
            var customer = delivery.getCustomer();
            if (customer == null) {
                log.warn("Delivery {} has no associated customer. Skipping.", delivery.getId());
                return;
            }

            // prevent duplicates
            if (deliveryHistoryRepository.existsByDeliveryAndDeliveryDate(delivery, tour.getDate())) {
                log.info("History already exists for delivery {} on {}", delivery.getId(), tour.getDate());
                return;
            }

            var history = DeliveryHistory.builder()
                    .delivery(delivery)
                    .customer(customer)
                    .tour(tour)
                    .deliveryDate(tour.getDate() != null ? tour.getDate() : LocalDate.now())
                    .plannedTime(customer.getPreferredTimeSlotStart())
                    .actualTime(LocalTime.now())
                    .delayMinutes(null)
                    .build();

            deliveryHistoryRepository.save(history);
        });

        log.info("Archived {} deliveries for tour {}", deliveries.size(), tour.getId());
    }

}

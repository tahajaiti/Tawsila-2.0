package com.kyojin.tawsila.repository;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.DeliveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {
    boolean existsByDeliveryAndDeliveryDate(Delivery delivery, LocalDate date);
    List<DeliveryHistory> findTop50ByOrderByDeliveryDateDesc();
}

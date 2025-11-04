package com.kyojin.tawsila.repository;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findAllByStatus(DeliveryStatus status);
}

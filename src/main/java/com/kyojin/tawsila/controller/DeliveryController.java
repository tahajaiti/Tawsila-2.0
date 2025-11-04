package com.kyojin.tawsila.controller;

import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.dto.DeliveryStatusDTO;
import com.kyojin.tawsila.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Autowired
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody DeliveryDTO dto) {
        var createdDelivery = deliveryService.createDelivery(dto);
        return ResponseEntity.ok(createdDelivery);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{id}" )
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}" )
    public ResponseEntity<DeliveryDTO> updateDelivery(@PathVariable Long id, @Valid @RequestBody DeliveryDTO dto) {
        var updatedDelivery = deliveryService.updateDelivery(id, dto);
        return ResponseEntity.ok(updatedDelivery);
    }

    @DeleteMapping("/{id}" )
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/status" )
    public ResponseEntity<DeliveryDTO> updateDeliveryStatus(@PathVariable Long id, @Valid @RequestBody DeliveryStatusDTO dto) {
        var updatedDelivery = deliveryService.updateDeliveryStatus(id, dto.getStatus());
        return ResponseEntity.ok(updatedDelivery);
    }
}

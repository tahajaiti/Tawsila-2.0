package com.kyojin.tawsila.service;

import com.kyojin.tawsila.criteria.DeliverySearchCriteria;
import com.kyojin.tawsila.dto.DeliveryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Delivery operations.
 * Handles CRUD operations for Delivery entities.
 */
public interface DeliveryService {

    /**
     * Create a new delivery.
     *
     * @param dto the DTO containing delivery data
     * @return the created DeliveryResponseDTO
     */
    DeliveryDTO createDelivery(DeliveryDTO dto);

    /**
     * Get a delivery by its ID.
     *
     * @param id the ID of the delivery
     * @return an Optional containing the DeliveryResponseDTO if found
     */
    Optional<DeliveryDTO> getDeliveryById(Long id);

    /**
     * Get all deliveries.
     *
     * @return a list of DeliveryResponseDTOs
     */
    List<DeliveryDTO> getAllDeliveries();

    /**
     * Get deliveries based on search criteria with pagination.
     *
     * @param criteria the search criteria
     * @param pageable the pagination information
     * @return a paginated list of DeliveryDTOs
     */
    Page<DeliveryDTO> getDeliveries(DeliverySearchCriteria criteria, Pageable pageable);

    /**
     * Update an existing delivery.
     *
     * @param deliveryId      the ID of the delivery to update
     * @param deliveryDetails the DTO containing updated delivery data
     * @return the updated DeliveryResponseDTO
     */
    DeliveryDTO updateDelivery(Long deliveryId, DeliveryDTO deliveryDetails);

    /**
     * Delete a delivery by its ID.
     *
     * @param id the ID of the delivery to delete
     */
    void deleteDelivery(Long id);


    /**
     * Update the status of an existing delivery.
     *
     * @param deliveryId the ID of the delivery to update
     * @param status     the new status of the delivery
     * @return the updated DeliveryDTO
     */
    DeliveryDTO updateDeliveryStatus(Long deliveryId, String status);
}

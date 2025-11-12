package com.kyojin.tawsila.service;

import com.kyojin.tawsila.criteria.TourSearchCriteria;
import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.dto.TourDTO;
import com.kyojin.tawsila.dto.TourDistanceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TourService {

    /**
     * Create a new tour
     * @param dto Data transfer object containing tour details
     * @return Created tour DTO
     */
    TourDTO createTour(TourDTO dto);

    /**
     * Retrieve a tour by its ID
     * @param id ID of the tour
     * @return Optional containing the tour DTO if found, empty otherwise
     */
    Optional<TourDTO> getTourById(Long id);

    /**
     * Retrieve all tours
     * @return List of all tour DTOs
     */
    List<TourDTO> getAllTours();

    /**
     * Retrieve tours based on search criteria with pagination
     * @param criteria Search criteria for filtering tours
     * @param pageable Pagination information
     * @return Page of tour DTOs matching the criteria
     */
    Page<TourDTO> getTours(TourSearchCriteria criteria, Pageable pageable);

    /**
     * Update an existing tour
     * @param id ID of the tour to update
     * @param dto Data transfer object containing updated tour details
     * @return Updated tour DTO
     */
    TourDTO updateTour(Long id, TourDTO dto);

    /**
     * Delete a tour by its ID
     * @param id ID of the tour to delete
     */
    void deleteTour(Long id);

    /**
     * Get optimized tour using specified algorithm
     * @param tourId ID of the tour
     * @return TourDTO with optimized route
     */
    TourDTO getOptimizedTour(Long tourId);

    /**
     * Calculate total distance of the tour
     * @param tourId ID of the tour
     * @return Total distance of the tour in kilometers
     */
    TourDistanceDTO getTotalDistance(Long tourId);

    /**
     * Update a tour status
     * @param tourId ID of the tour
     * @param status The status to change to
     * @return Updated tour DTO
     */
    TourDTO updateTourStatus(Long tourId, String status);
}

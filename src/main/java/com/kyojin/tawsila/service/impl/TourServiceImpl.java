package com.kyojin.tawsila.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.kyojin.tawsila.criteria.TourSearchCriteria;
import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.dto.TourDTO;
import com.kyojin.tawsila.dto.TourDistanceDTO;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Tour;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.enums.TourStatus;
import com.kyojin.tawsila.exception.BadRequestException;
import com.kyojin.tawsila.exception.NotFoundException;
import com.kyojin.tawsila.mapper.TourMapper;
import com.kyojin.tawsila.model.GenericSpecification;
import com.kyojin.tawsila.model.SearchInput;
import com.kyojin.tawsila.optimizer.TourOptimizer;
import com.kyojin.tawsila.repository.DeliveryRepository;
import com.kyojin.tawsila.repository.TourRepository;
import com.kyojin.tawsila.repository.VehicleRepository;
import com.kyojin.tawsila.service.DeliveryHistoryService;
import com.kyojin.tawsila.service.TourService;
import com.kyojin.tawsila.util.DistanceCalculator;
import com.kyojin.tawsila.util.ParseUtil;
import com.kyojin.tawsila.util.TourValidator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourMapper tourMapper;
    private final TourRepository tourRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryHistoryService deliveryHistoryService;
    private final Warehouse warehouse;
    private final TourOptimizer optimizer;


    @Override
    @Transactional
    public TourDTO createTour(TourDTO dto) {
        Tour tour = tourMapper.toEntity(dto);

        if (dto.getVehicle() != null && dto.getVehicle().getId() != null) {
            var vehicle = vehicleRepository.findById(dto.getVehicle().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle ID"));
            tour.setVehicle(vehicle);
        }

        if (dto.getDeliveries() != null && !dto.getDeliveries().isEmpty()) {
            List<Delivery> deliveries = findAndLinkDeliveries(dto.getDeliveries(), tour);
            tour.setDeliveries(deliveries);
        }

        TourValidator.validateCapactity(tour);

        var savedTour = tourRepository.save(tour);

        return tourMapper.toDTO(savedTour);
    }

    @Override
    public Optional<TourDTO> getTourById(Long id) {
        return tourRepository.findById(id)
                .map(tourMapper::toDTO);
    }

    @Override
    public List<TourDTO> getAllTours() {
        return tourRepository.findAll().stream()
                .map(tourMapper::toDTO)
                .toList();
    }

    @Override
    public Page<TourDTO> getTours(TourSearchCriteria criteria, Pageable pageable) {
        Specification<Tour> spec = Specification.unrestricted();

        if (criteria != null) {
            if (criteria.getStatus() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("status", ":", criteria.getStatus())
                ));
            }

            if (criteria.getVehicleId() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("vehicle").get("id"), criteria.getVehicleId())
                );
            }

            if (criteria.getStartDate() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("date", ">", criteria.getStartDate())
                ));
            }
            if (criteria.getEndDate() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("date", "<", criteria.getEndDate())
                ));
            }

            if (criteria.getMinDeliveries() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(cb.size(root.get("deliveries")), criteria.getMinDeliveries())
                );
            }

            if (criteria.getMaxDeliveries() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.lessThanOrEqualTo(cb.size(root.get("deliveries")), criteria.getMaxDeliveries())
                );
            }
        }

        Page<Tour> tourPage = tourRepository.findAll(spec, pageable);
        return tourPage.map(tourMapper::toDTO);
    }

    @Override
    @Transactional
    public TourDTO updateTour(Long id, TourDTO dto) {
        var tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found with id: " + id));

        var previousStatus = tour.getStatus();

        tourMapper.updateEntityFromDTO(dto, tour);

        // handle vehicle
        if (dto.getVehicle() != null && dto.getVehicle().getId() != null) {
            var vehicle = vehicleRepository.findById(dto.getVehicle().getId())
                    .orElseThrow(() -> new BadRequestException("Invalid vehicle ID"));
            tour.setVehicle(vehicle);
        } else {
            tour.setVehicle(null);
        }

        // handle deliveries
        if (dto.getDeliveries() != null) {
            if (tour.getDeliveries() != null) {
                tour.getDeliveries().forEach(d -> d.setTour(null));
            }
            var updatedDeliveries = findAndLinkDeliveries(dto.getDeliveries(), tour);
            tour.setDeliveries(updatedDeliveries);
        }

        TourValidator.validateCapactity(tour);

        var updatedTour = tourRepository.save(tour);

        if (previousStatus != tour.getStatus() && tour.getStatus() == TourStatus.COMPLETED) {
            deliveryHistoryService.logDeliveryHistory(tour);
        }

        return tourMapper.toDTO(updatedTour);
    }



    @Override
    @Transactional
    public void deleteTour(Long id) {
        tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found with id: " + id));

        tourRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TourDTO getOptimizedTour(Long tourId) {
        var tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Tour not found with id: " + tourId));

        var deliveries = tour.getDeliveries();
        var vehicle = tour.getVehicle();

        if (deliveries == null || deliveries.isEmpty()) {
            return tourMapper.toDTO(tour);
        }

        var optimizedDeliveries = optimizer.calculateOptimalTour(warehouse, deliveries, vehicle);

//        tour.getDeliveries().clear();
//        tour.getDeliveries().addAll(optimizedDeliveries);

        tour.getDeliveries().sort(Comparator.comparingInt(optimizedDeliveries::indexOf));

        var optimizedTour = tourRepository.save(tour);

        return tourMapper.toDTO(optimizedTour);
    }


    @Override
    @Transactional
    public TourDistanceDTO getTotalDistance(Long tourId) {
        var tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Tour not found with id: " + tourId));

        var deliveries = tour.getDeliveries();

        if (deliveries == null || deliveries.isEmpty()) {
            return tourMapper.toDistanceDTO(0.0);
        }


        // sum of the distances
        double totalDistance = 0.0;

        // storing the previous distances
        double prevLat = warehouse.getLatitude();
        double prevLon = warehouse.getLongitude();


        // calculate distance from warehouse to first delivery
        for (var delivery : deliveries) {
            totalDistance += DistanceCalculator.calculateDistance(
                    prevLat,
                    prevLon,
                    delivery.getCustomer().getLatitude(),
                    delivery.getCustomer().getLongitude()
            );

            // update previous location to current delivery
            prevLat = delivery.getCustomer().getLatitude();
            prevLon = delivery.getCustomer().getLongitude();
        }


        // marking the final distance to the warehouse
        totalDistance += DistanceCalculator.calculateDistance(
                prevLat,
                prevLon,
                warehouse.getLatitude(),
                warehouse.getLongitude()
        );

        return tourMapper.toDistanceDTO(totalDistance);
    }

    @Override
    public TourDTO updateTourStatus(Long tourId, String status) {
        var tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Tour not found with id: " + tourId));

        var newStatus = ParseUtil.parseType(status, TourStatus.class);

        tour.setStatus(newStatus);

        tourRepository.save(tour);

        return tourMapper.toDTO(tour);
    }

    private List<Delivery> findAndLinkDeliveries(List<DeliveryDTO> deliveryDTOs, Tour tour) {
        Set<Long> deliveryIds = deliveryDTOs.stream()
                .map(DeliveryDTO::getId)
                .collect(Collectors.toSet());

        List<Delivery> deliveries = deliveryRepository.findAllById(deliveryIds);

        if (deliveries.size() != deliveryIds.size()) {
            Set<Long> foundIds = deliveries.stream()
                    .map(Delivery::getId)
                    .collect(Collectors.toSet());
            Long missingId = deliveryIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElse(null);
            throw new NotFoundException("Invalid delivery ID: " + missingId);
        }

        for (Delivery delivery : deliveries) {
            if (delivery.getTour() != null) {
                throw new BadRequestException("Delivery " + delivery.getId()
                        + " is already assigned to tour " + delivery.getTour().getId());
            }
            delivery.setTour(tour);
        }

        return deliveries;

    }
}

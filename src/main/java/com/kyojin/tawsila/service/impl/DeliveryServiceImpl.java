package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.criteria.DeliverySearchCriteria;
import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Tour;
import com.kyojin.tawsila.enums.DeliveryStatus;
import com.kyojin.tawsila.exception.BadRequestException;
import com.kyojin.tawsila.exception.NotFoundException;
import com.kyojin.tawsila.mapper.DeliveryMapper;
import com.kyojin.tawsila.model.GenericSpecification;
import com.kyojin.tawsila.model.SearchInput;
import com.kyojin.tawsila.repository.DeliveryRepository;
import com.kyojin.tawsila.repository.TourRepository;
import com.kyojin.tawsila.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final TourRepository tourRepository;

    @Autowired
    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               DeliveryMapper deliveryMapper,
                               TourRepository tourRepository) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryMapper = deliveryMapper;
        this.tourRepository = tourRepository;
    }

    @Override
    public DeliveryDTO createDelivery(DeliveryDTO deliveryDTO) {
        var deliveryEntity = deliveryMapper.toEntity(deliveryDTO);

        // we check if the dto has a tour associated
        // if it is the case, we fetch the tour entity and set it to the delivery
        if (deliveryDTO.getTour() != null && deliveryDTO.getTour().getId() != null) {
            Tour tour = tourRepository.findById(deliveryDTO.getTour().getId())
                    .orElseThrow(() -> new NotFoundException("Tour not found with id: " + deliveryDTO.getTour().getId()));
            deliveryEntity.setTour(tour);
        }

        var savedEntity = deliveryRepository.save(deliveryEntity);
        return deliveryMapper.toDTO(savedEntity);
    }

    @Override
    public Optional<DeliveryDTO> getDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .map(deliveryMapper::toDTO);
    }

    @Override
    public List<DeliveryDTO> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(deliveryMapper::toDTO)
                .toList();
    }

    @Override
    public Page<DeliveryDTO> getDeliveries(DeliverySearchCriteria criteria, Pageable pageable) {
        Specification<Delivery> spec = Specification.unrestricted();

        if (criteria != null) {
            if (criteria.getStatus() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("status", ":", criteria.getStatus())
                ));
            }


            if (criteria.getMinWeightKg() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("weightKg", ">", criteria.getMinWeightKg())
                ));
            }
            if (criteria.getMaxWeightKg() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("weightKg", "<", criteria.getMaxWeightKg())
                ));
            }

            if (criteria.getMinVolumeM3() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("volumeM3", ">", criteria.getMinVolumeM3())
                ));
            }
            if (criteria.getMaxVolumeM3() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("volumeM3", "<", criteria.getMaxVolumeM3())
                ));
            }

            if (criteria.getTimeSlot() != null && !criteria.getTimeSlot().isEmpty()) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("timeSlot", ":", criteria.getTimeSlot())
                ));
            }


            if (criteria.getTourId() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("tour").get("id"), criteria.getTourId())
                );
            }

            if (criteria.getCustomerId() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("customer").get("id"), criteria.getCustomerId())
                );
            }
        }

        Page<Delivery> deliveryPage = deliveryRepository.findAll(spec, pageable);
        return deliveryPage.map(deliveryMapper::toDTO);
    }

    @Override
    public DeliveryDTO updateDelivery(Long deliveryId, DeliveryDTO deliveryDetails) {
        var deliveryEntity = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with id: " + deliveryId));

        deliveryMapper.updateEntityFromDTO(deliveryDetails, deliveryEntity);

        // we update the tour if provided
        if (deliveryDetails.getTour() != null && deliveryDetails.getTour().getId() != null) {
            Tour tour = tourRepository.findById(deliveryDetails.getTour().getId())
                    .orElseThrow(() -> new NotFoundException("Tour not found with id: " + deliveryDetails.getTour().getId()));
            deliveryEntity.setTour(tour);
        }

        var updatedEntity = deliveryRepository.save(deliveryEntity);
        return deliveryMapper.toDTO(updatedEntity);
    }

    @Override
    public void deleteDelivery(Long id) {
        deliveryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Delivery not found with id: " + id));

        deliveryRepository.deleteById(id);
    }

    @Override
    public DeliveryDTO updateDeliveryStatus(Long deliveryId, String status) {
        var deliveryEntity = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with id: " + deliveryId));

        try {
            deliveryEntity.setStatus(DeliveryStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + status);
        }

        var updatedEntity = deliveryRepository.save(deliveryEntity);
        return deliveryMapper.toDTO(updatedEntity);
    }
}

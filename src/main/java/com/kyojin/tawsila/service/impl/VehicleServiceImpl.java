package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.criteria.VehicleSearchCriteria;
import com.kyojin.tawsila.dto.VehicleDTO;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.enums.VehicleType;
import com.kyojin.tawsila.exception.NotFoundException;
import com.kyojin.tawsila.mapper.VehicleMapper;
import com.kyojin.tawsila.model.GenericSpecification;
import com.kyojin.tawsila.model.SearchInput;
import com.kyojin.tawsila.repository.VehicleRepository;
import com.kyojin.tawsila.service.VehicleService;
import com.kyojin.tawsila.util.ParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {


    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Autowired
    public  VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
    }

    public VehicleDTO createVehicle(VehicleDTO dto) {
        VehicleType vehicleType = ParseUtil.parseVehicleType(dto.getType());

        dto.setType(vehicleType.name());

        var vehicle = vehicleMapper.toEntity(dto);
        var savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(savedVehicle);
    }


    @Override
    public Optional<VehicleDTO> getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toDTO);
    }


    @Override
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    @Override
    public Page<VehicleDTO> getVehicles(VehicleSearchCriteria criteria, Pageable pageable) {
        Specification<Vehicle> spec = Specification.unrestricted();

        if (criteria != null) {
            if (criteria.getType() != null && !criteria.getType().isEmpty()) {
                VehicleType typeEnum = ParseUtil.parseVehicleType(criteria.getType());
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("type", ":", typeEnum)
                ));
            }

            if (criteria.getMinPayload() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("maxWeightKg", ">", criteria.getMinPayload())
                ));
            }

            if (criteria.getMinVolume() != null) {
                spec = spec.and(new GenericSpecification<>(
                        new SearchInput("maxVolumeM3", ">", criteria.getMinVolume())
                ));
            }
        }

        Page<Vehicle> vehiclePage = vehicleRepository.findAll(spec, pageable);

        return vehiclePage.map(vehicleMapper::toDTO);
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found with id: " + id));

        vehicleRepository.deleteById(id);
    }

    @Override
    public VehicleDTO updateVehicle(Long vehicleId, VehicleDTO vehicleDetailsDTO) {
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found with id: " + vehicleId));


        VehicleType vehicleType = ParseUtil.parseVehicleType(vehicleDetailsDTO.getType());
        vehicle.setType(vehicleType);
        vehicle.setMaxWeightKg(vehicleType.getMaxWeightKg());
        vehicle.setMaxVolumeM3(vehicleType.getMaxVolumeM3());
        vehicle.setMaxDeliveries(vehicleType.getMaxDeliveries());

        return vehicleMapper.toDTO(vehicleRepository.save(vehicle));
    }

}

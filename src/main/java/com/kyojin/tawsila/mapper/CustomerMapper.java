package com.kyojin.tawsila.mapper;

import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerDTO customerDTO);
    CustomerDTO toDTO(Customer customer);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(CustomerDTO customerDTO, @MappingTarget Customer customer);
}

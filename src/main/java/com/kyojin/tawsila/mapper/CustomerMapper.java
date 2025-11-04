package com.kyojin.tawsila.mapper;

import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerDTO customerDTO);
    CustomerDTO toDTO(Customer customer);

    void updateEntityFromDTO(CustomerDTO customerDTO, Customer customer);
}

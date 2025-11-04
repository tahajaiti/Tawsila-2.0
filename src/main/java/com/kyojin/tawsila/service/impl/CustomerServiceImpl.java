package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.entity.Customer;
import com.kyojin.tawsila.mapper.CustomerMapper;
import com.kyojin.tawsila.repository.CustomerRepository;
import com.kyojin.tawsila.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = customerMapper.toEntity(customerDTO);

        var savedCustomer = customerRepository.save(customer);
        return customerMapper.toDTO(savedCustomer);
    }

}

package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.entity.Customer;
import com.kyojin.tawsila.exception.NotFoundException;
import com.kyojin.tawsila.mapper.CustomerMapper;
import com.kyojin.tawsila.repository.CustomerRepository;
import com.kyojin.tawsila.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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


    public Optional<CustomerDTO> getCustomerById(Long id) {
        var customer = customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Entity not found with id: " + id));
        return Optional.of(customerMapper.toDTO(customer));
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO)
                .toList();
    }

    public void deleteCustomerById(Long id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        customerRepository.deleteById(id);
    }

    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        Long id = customerDTO.getId();
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        customerMapper.updateEntityFromDTO(customerDTO, existingCustomer);

        var updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toDTO(updatedCustomer);
    }
}

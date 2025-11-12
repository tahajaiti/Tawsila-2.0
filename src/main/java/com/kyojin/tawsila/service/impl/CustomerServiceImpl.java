package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.criteria.CustomerSearchCriteria;
import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.entity.Customer;
import com.kyojin.tawsila.exception.NotFoundException;
import com.kyojin.tawsila.mapper.CustomerMapper;
import com.kyojin.tawsila.model.GenericSpecification;
import com.kyojin.tawsila.model.SearchInput;
import com.kyojin.tawsila.repository.CustomerRepository;
import com.kyojin.tawsila.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Objects.requireNonNull(customerDTO, "customerDTO must not be null");
        if (customerDTO.getId() != null) {
            throw new IllegalArgumentException("New customer must not have an id");
        }

        log.debug("Creating customer: {}", customerDTO);
        Customer customer = customerMapper.toEntity(customerDTO);

        var savedCustomer = customerRepository.save(customer);
        var dto = customerMapper.toDTO(savedCustomer);
        log.info("Created customer with id {}", dto.getId());
        return dto;
    }


    public Optional<CustomerDTO> getCustomerById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Fetching customer by id {}", id);
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entity not found with id: " + id));
        return Optional.of(customerMapper.toDTO(customer));
    }

    public List<CustomerDTO> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO)
                .toList();
    }

    @Override
    public Page<CustomerDTO> getCustomers(CustomerSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching customers with criteria {} and pageable {}", criteria, pageable);
        if (pageable == null) {
            pageable = Pageable.unpaged();
        }

        Specification<Customer> spec = buildSpecificationFromCriteria(criteria);

        Page<Customer> page = customerRepository.findAll(spec, pageable);
        return page.map(customerMapper::toDTO);
    }

    @Transactional
    public void deleteCustomerById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Deleting customer with id {}", id);
        customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        customerRepository.deleteById(id);
        log.info("Deleted customer with id {}", id);
    }

    @Transactional
    public CustomerDTO updateCustomer(CustomerDTO customerDTO, Long id) {
        Objects.requireNonNull(customerDTO, "customerDTO must not be null");
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Updating customer id {} with data {}", id, customerDTO);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        customerMapper.updateEntityFromDTO(customerDTO, existingCustomer);

        var updatedCustomer = customerRepository.save(existingCustomer);
        var dto = customerMapper.toDTO(updatedCustomer);
        log.info("Updated customer with id {}", dto.getId());
        return dto;
    }

    private Specification<Customer> buildSpecificationFromCriteria(CustomerSearchCriteria criteria) {
        Specification<Customer> spec = Specification.unrestricted();

        if (criteria == null) {
            return spec;
        }

        Function<String, String> norm = s -> s == null ? null : s.trim();

        String name = norm.apply(criteria.getName());
        if (name != null && !name.isEmpty()) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("name", ":", name)));
        }

        String address = norm.apply(criteria.getAddress());
        if (address != null && !address.isEmpty()) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("address", ":", address)));
        }

        if (criteria.getMinLatitude() != null) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("latitude", ">", criteria.getMinLatitude())));
        }
        if (criteria.getMaxLatitude() != null) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("latitude", "<", criteria.getMaxLatitude())));
        }

        if (criteria.getMinLongitude() != null) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("longitude", ">", criteria.getMinLongitude())));
        }
        if (criteria.getMaxLongitude() != null) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("longitude", "<", criteria.getMaxLongitude())));
        }

        if (criteria.getPreferredTimeSlotStart() != null) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("preferredTime", ">", criteria.getPreferredTimeSlotStart())));
        }
        if (criteria.getPreferredTimeSlotEnd() != null) {
            spec = spec.and(new GenericSpecification<>(new SearchInput("preferredTime", "<", criteria.getPreferredTimeSlotEnd())));
        }

        return spec;
    }
}

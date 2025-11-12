package com.kyojin.tawsila.service;

import com.kyojin.tawsila.criteria.CustomerSearchCriteria;
import com.kyojin.tawsila.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    /**
     * Create a new customer.
     *
     * @param customerDTO the DTO containing customer data
     * @return the created CustomerDTO
     */
    CustomerDTO createCustomer(CustomerDTO customerDTO);

    /**
     * Get a customer by its ID.
     *
     * @param id the ID of the customer
     * @return an Optional containing the CustomerDTO if found
     */
    Optional<CustomerDTO> getCustomerById(Long id);

    /**
     * Get all customers.
     *
     * @return a list of CustomerDTOs
     */
    List<CustomerDTO> getAllCustomers();

    /**
     * Get customers based on search criteria with pagination.
     *
     * @param criteria the search criteria
     * @param pageable the pagination information
     * @return a paginated list of CustomerDTOs
     */
    Page<CustomerDTO> getCustomers(CustomerSearchCriteria criteria, Pageable pageable);

    /**
     * Delete a customer by its ID.
     *
     * @param id the ID of the customer to delete
     */
    void deleteCustomerById(Long id);

    /**
     * Update an existing customer.
     *
     * @param customerDTO the DTO containing updated customer data
     * @return the updated CustomerDTO
     */
    CustomerDTO updateCustomer(CustomerDTO customerDTO, Long id);
}

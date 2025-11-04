package com.kyojin.tawsila.controller;

import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.service.CustomerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customers")
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        log.info("Fetching customer with ID: {}", id);
        return customerService.getCustomerById(id)
                .map(customer -> {
                    log.debug("Customer found: {}", customer);
                    return ResponseEntity.ok(customer);
                })
                .orElseGet(() -> {
                    log.warn("Customer with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        log.info("Fetching all customers");
        var customers = customerService.getAllCustomers();
        log.debug("Fetched {} customers", customers.size());
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        log.info("Creating new customer: {}", customerDTO.getName());
        var createdCustomer = customerService.createCustomer(customerDTO);
        log.debug("Customer created successfully: {}", createdCustomer);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerDTO
    ) {
        log.info("Updating customer with ID: {}", id);
        var updatedCustomer = customerService.updateCustomer(customerDTO, id);
        log.debug("Customer updated successfully: {}", updatedCustomer);
        return ResponseEntity.ok(updatedCustomer);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomerById(id);
        log.debug("Customer with ID {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}

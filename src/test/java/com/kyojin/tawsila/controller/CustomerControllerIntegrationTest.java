package com.kyojin.tawsila.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyojin.tawsila.dto.CustomerDTO;
import com.kyojin.tawsila.entity.Customer;
import com.kyojin.tawsila.mapper.CustomerMapper;
import com.kyojin.tawsila.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomerRepository customerRepository;


    @BeforeEach
    void cleanDb() {
        customerRepository.deleteAll();
    }

    private Customer createCustomer(String name) {
        return customerRepository.save(Customer.builder()
                .name(name)
                .address("Rue Casablanca")
                .latitude(33.589886)
                .longitude(-7.603869)
                .preferredTimeSlotStart(LocalTime.of(9, 0))
                .preferredTimeSlotEnd(LocalTime.of(11, 0))
                .build());
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        var dto = CustomerDTO.builder()
                .name("John Doe")
                .address("123 Main St")
                .latitude(33.6)
                .longitude(-7.5)
                .preferredTimeSlotStart(LocalTime.of(9, 0))
                .preferredTimeSlotEnd(LocalTime.of(11, 0))
                .build();

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.address").value("123 Main St"));

        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        var customer = createCustomer("Jane Doe");

        mockMvc.perform(get("/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.address").value("Rue Casablanca"));
    }

    @Test
    void shouldReturn404IfCustomerNotFound() throws Exception {
        mockMvc.perform(get("/customers/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        var existing = createCustomer("Old Name");

        var updateDto = CustomerDTO.builder()
                .name("Updated Name")
                .address("Updated Address")
                .latitude(33.7)
                .longitude(-7.4)
                .preferredTimeSlotStart(LocalTime.of(10, 0))
                .preferredTimeSlotEnd(LocalTime.of(12, 0))
                .build();

        mockMvc.perform(put("/customers/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.address").value("Updated Address"));

        var updated = customerRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getAddress()).isEqualTo("Updated Address");
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        var customer = createCustomer("Delete Me");

        mockMvc.perform(delete("/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        assertThat(customerRepository.existsById(customer.getId())).isFalse();
    }

    @Test
    void shouldReturnPaginatedCustomers() throws Exception {
        for (int i = 1; i <= 15; i++) {
            createCustomer("Customer " + i);
        }

        mockMvc.perform(get("/customers")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15));
    }
}

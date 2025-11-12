package com.kyojin.tawsila.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.dto.TourDTO;
import com.kyojin.tawsila.dto.VehicleDTO;
import com.kyojin.tawsila.entity.Customer;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.enums.DeliveryStatus;
import com.kyojin.tawsila.repository.CustomerRepository;
import com.kyojin.tawsila.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TourControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testOptimizeTourEndpoint() throws Exception {
        VehicleDTO vehicleDTO = new VehicleDTO();
        vehicleDTO.setType("TRUCK");
        vehicleDTO.setMaxDeliveries(10);

        String vehicleJson = objectMapper.writeValueAsString(vehicleDTO);

        VehicleDTO createdVehicle = objectMapper.readValue(
                mockMvc.perform(post("/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(vehicleJson))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                VehicleDTO.class
        );

        DeliveryDTO delivery1 = saveDelivery(10.0, 10.0, 5.0, 2.0, "Customer A");
        DeliveryDTO delivery2 = saveDelivery(1.0, 1.0, 5.0, 2.0, "Customer B");
        DeliveryDTO delivery3 = saveDelivery(5.0, 5.0, 5.0, 2.0, "Customer C");

        TourDTO tourDTO = new TourDTO();
        tourDTO.setVehicle(createdVehicle);
        tourDTO.setDate(LocalDate.now());
        tourDTO.setDeliveries(List.of(delivery1, delivery2, delivery3));

        String tourJson = objectMapper.writeValueAsString(tourDTO);

        TourDTO createdTour = objectMapper.readValue(
                mockMvc.perform(post("/tours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(tourJson))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                TourDTO.class
        );

        Long tourId = createdTour.getId();
        assertThat(createdTour.getDeliveries()).hasSize(3);

        MvcResult result = mockMvc.perform(get("/tours/{id}/optimize", tourId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TourDTO optimizedTour = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TourDTO.class
        );

        assertThat(optimizedTour.getId()).isEqualTo(tourId);

        List<Long> originalIds = createdTour.getDeliveries().stream().map(DeliveryDTO::getId).collect(Collectors.toList());
        List<Long> optimizedIds = optimizedTour.getDeliveries().stream().map(DeliveryDTO::getId).collect(Collectors.toList());

        assertThat(optimizedIds)
                .as("Optimized tour must contain exactly the same delivery IDs as the original")
                .containsExactlyInAnyOrderElementsOf(originalIds);

        System.out.println("Original Order: " + originalIds);
        System.out.println("Optimized Order: " + optimizedIds);
    }

    private DeliveryDTO saveDelivery(double latitude, double longitude, double weightKg, double volumeM3, String customerName) {
        var customer = new Customer();
        customer.setName(customerName);
        customer.setLatitude(latitude);
        customer.setLongitude(longitude);
        customer.setPreferredTimeSlotStart(LocalTime.of(8, 0));
        customer.setPreferredTimeSlotEnd(LocalTime.of(18, 0));

        customer = customerRepository.save(customer);

        Delivery delivery = new Delivery();
        delivery.setCustomer(customer);
        delivery.setWeightKg(weightKg);
        delivery.setVolumeM3(volumeM3);
        delivery.setStatus(DeliveryStatus.PENDING);


        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(savedDelivery.getId());
        dto.setLatitude(savedDelivery.getCustomer().getLatitude());
        dto.setLongitude(savedDelivery.getCustomer().getLongitude());
        dto.setWeightKg(savedDelivery.getWeightKg());
        dto.setVolumeM3(savedDelivery.getVolumeM3());
        dto.setStatus("PENDING");

        return dto;
    }
}
package com.kyojin.tawsila.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.dto.TourDTO;
import com.kyojin.tawsila.dto.VehicleDTO;
import com.kyojin.tawsila.entity.Customer;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.repository.CustomerRepository;
import com.kyojin.tawsila.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        // Create vehicle
        VehicleDTO vehicleDTO = new VehicleDTO();
        vehicleDTO.setType("TRUCK");
        vehicleDTO.setMaxDeliveries(10);

        String vehicleJson = objectMapper.writeValueAsString(vehicleDTO);

        VehicleDTO createdVehicle = objectMapper.readValue(
                mockMvc.perform(post("/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(vehicleJson))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                VehicleDTO.class
        );

        DeliveryDTO delivery1 = saveDelivery(1.0, 1.0, 5.0, 2.0);
        DeliveryDTO delivery2 = saveDelivery(2.0, 2.0, 5.0, 2.0);
        DeliveryDTO delivery3 = saveDelivery(3.0, 3.0, 5.0, 2.0);

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
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                TourDTO.class
        );

        Long tourId = createdTour.getId();
        assertThat(createdTour.getDeliveries()).hasSize(3);

        TourDTO optimizedTour = objectMapper.readValue(
                mockMvc.perform(get("/tours/{id}/optimize", tourId))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                TourDTO.class
        );

        assertThat(optimizedTour.getId()).isEqualTo(tourId);
        assertThat(optimizedTour.getDeliveries()).hasSize(3);

        List<Double> originalLatitudes = createdTour.getDeliveries().stream()
                .map(DeliveryDTO::getLatitude).toList();
        List<Double> optimizedLatitudes = optimizedTour.getDeliveries().stream()
                .map(DeliveryDTO::getLatitude).toList();

        assertThat(optimizedLatitudes).isNotEqualTo(originalLatitudes);
    }

    private DeliveryDTO saveDelivery(double latitude, double longitude, double weightKg, double volumeM3) {
        var customer = new Customer();
        customer.setName("Test Customer");
        customer.setLatitude(latitude);
        customer.setLongitude(longitude);
        customer.setPreferredTimeSlotStart(java.time.LocalTime.of(8, 0));
        customer.setPreferredTimeSlotEnd(java.time.LocalTime.of(18, 0));

        customer = customerRepository.save(customer);

        Delivery delivery = new Delivery();
        delivery.setCustomer(customer);
        delivery.setWeightKg(weightKg);
        delivery.setVolumeM3(volumeM3);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(savedDelivery.getId());
        dto.setLatitude(savedDelivery.getCustomer().getLatitude());
        dto.setLongitude(savedDelivery.getCustomer().getLongitude());
        dto.setWeightKg(savedDelivery.getWeightKg());
        dto.setVolumeM3(savedDelivery.getVolumeM3());

        return dto;
    }
}

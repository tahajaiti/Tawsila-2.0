package com.kyojin.tawsila.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyojin.tawsila.dto.DeliveryDTO;
import com.kyojin.tawsila.dto.TourDTO;
import com.kyojin.tawsila.dto.VehicleDTO;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import jakarta.transaction.Transactional;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TourControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private DeliveryRepository deliveryRepository;

    public TourControllerIntegrationTest() {
        MockServletContext servletContext = new MockServletContext();
        GenericWebApplicationContext webContext = new GenericWebApplicationContext(servletContext);
        new XmlBeanDefinitionReader(webContext).loadBeanDefinitions(new ClassPathResource("applicationContext-test.xml"));
        webContext.refresh();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        this.objectMapper = webContext.getBean(ObjectMapper.class);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.deliveryRepository = webContext.getBean(DeliveryRepository.class);
    }



    @Test
    @Transactional
    void testOptimizeTourEndpoint() throws Exception {
        VehicleDTO vehicleDTO = new VehicleDTO();
        vehicleDTO.setType("TRUCK");
        vehicleDTO.setMaxDeliveries(10);


        MvcResult result = mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDTO)))
                .andExpect(status().isOk())
                .andReturn();

        VehicleDTO createdVehicle = objectMapper.readValue(result.getResponse().getContentAsString(), VehicleDTO.class);

        TourDTO tourDTO = new TourDTO();
        tourDTO.setVehicle(createdVehicle);
        tourDTO.setDate(LocalDate.now());

        DeliveryDTO delivery1 = delivery(1.0, 1.0, 5.0, 2.0);
        DeliveryDTO delivery2 = delivery(2.0, 2.0, 5.0, 2.0);
        DeliveryDTO delivery3 = delivery(3.0, 3.0, 5.0, 2.0);

        tourDTO.setDeliveries(List.of(delivery1, delivery2, delivery3));

        MvcResult createTourResult = mockMvc.perform(post("/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tourDTO)))
                .andExpect(status().isOk())
                .andReturn();

        TourDTO createdTour = objectMapper.readValue(createTourResult.getResponse().getContentAsString(), TourDTO.class);

        Long tourId = createdTour.getId();

        assertThat(createdTour.getDeliveries()).hasSize(3);

        MvcResult optimizeResult = mockMvc.perform(get("/tours/{id}/optimize", tourId)
                        .param("algorithm", "NEAREST_NEIGHBOR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TourDTO optimizedTour = objectMapper.readValue(optimizeResult.getResponse().getContentAsString(), TourDTO.class);

        assertThat(optimizedTour.getId()).isEqualTo(tourId);
        assertThat(optimizedTour.getDeliveries()).hasSize(3);

        List<Double> originalLatitudes = createdTour.getDeliveries().stream().map(DeliveryDTO::getLatitude).toList();
        List<Double> optimizedLatitudes = optimizedTour.getDeliveries().stream().map(DeliveryDTO::getLatitude).toList();

        assertThat(optimizedLatitudes).isNotEqualTo(originalLatitudes);
    }

    private DeliveryDTO delivery(double latitude, double longitude, double weightKg, double volumeM3) {
        Delivery deliveryEntity = new Delivery();
        deliveryEntity.getCustomer().setLatitude(latitude);
        deliveryEntity.getCustomer().setLongitude(longitude);
        deliveryEntity.setWeightKg(weightKg);
        deliveryEntity.setVolumeM3(volumeM3);

        Delivery savedDelivery = deliveryRepository.save(deliveryEntity);

        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(savedDelivery.getId());
        dto.setLatitude(savedDelivery.getCustomer().getLatitude());
        dto.setLongitude(savedDelivery.getCustomer().getLongitude());
        dto.setWeightKg(savedDelivery.getWeightKg());
        dto.setVolumeM3(savedDelivery.getVolumeM3());

        return dto;
    }
}
package com.kyojin.tawsila.optimizer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.model.AIOptimizationResponse;
import com.kyojin.tawsila.optimizer.TourOptimizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "tawsila.optimizer.type", havingValue = "AI")
public class AIOptimizer implements TourOptimizer {

    private final AzureOpenAiChatModel aiClient;
    private final ObjectMapper objectMapper;

    private record DeliveryNode(Long id, Double lat, Double lon, String status) {}

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        try {
            log.info("Requesting AI optimization for {} deliveries...", deliveries.size());

            List<DeliveryNode> nodes = deliveries.stream()
                    .map(d -> new DeliveryNode(d.getId(), d.getCustomer().getLatitude(), d.getCustomer().getLongitude(), d.getStatus().name()))
                    .toList();

            String deliveriesJson = objectMapper.writeValueAsString(nodes);

            PromptTemplate template = new PromptTemplate(new ClassPathResource("prompts/ai_optimizer_prompt.st"));
            Prompt prompt = template.create(Map.of(
                    "deliveries", deliveriesJson,
                    "warehouse_lat", warehouse.getLatitude(),
                    "warehouse_lon", warehouse.getLongitude()
            ));

            var response = aiClient.call(prompt);
            String rawOutput = response.getResult().getOutput().getText();

            String jsonOutput = sanitizeJson(rawOutput);
            AIOptimizationResponse result = objectMapper.readValue(jsonOutput, AIOptimizationResponse.class);

            return mapOrderedIdsToDeliveries(result.getOrderedIds(), deliveries);

        } catch (Exception e) {
            log.error("AI Optimization failed: {}. Returning original list.", e.getMessage());
            return deliveries;
        }
    }

    private List<Delivery> mapOrderedIdsToDeliveries(List<Long> orderedIds, List<Delivery> originalDeliveries) {
        if (orderedIds == null || orderedIds.isEmpty()) return originalDeliveries;

        Map<Long, Delivery> deliveryMap = originalDeliveries.stream()
                .collect(Collectors.toMap(Delivery::getId, Function.identity()));

        List<Delivery> optimizedRoute = new ArrayList<>();

        for (Long id : orderedIds) {
            if (deliveryMap.containsKey(id)) {
                optimizedRoute.add(deliveryMap.get(id));
            }
        }

        for (Delivery d : originalDeliveries) {
            if (!optimizedRoute.contains(d)) {
                optimizedRoute.add(d);
            }
        }

        return optimizedRoute;
    }

    private String sanitizeJson(String input) {
        if (input == null) return "{}";
        String cleaned = input.trim();
        if (cleaned.contains("```json")) {
            return cleaned.replace("```json", "").replace("```", "").trim();
        } else if (cleaned.contains("```")) {
            return cleaned.replace("```", "").trim();
        }
        return cleaned;
    }
}
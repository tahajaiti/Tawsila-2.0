package com.kyojin.tawsila.optimizer.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.model.AIOptimizationResponse;
import com.kyojin.tawsila.optimizer.TourOptimizer;
import com.kyojin.tawsila.repository.DeliveryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
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
public class AIOptimizer implements TourOptimizer {

    private final AzureOpenAiChatModel aiClient;
    private final DeliveryHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        try {
            log.info("Requesting AI optimization for {} deliveries...", deliveries.size());

            String historyJson = getDeliveryHistory();
            String deliveriesJson = objectMapper.writeValueAsString(deliveries);

            PromptTemplate template = new PromptTemplate(new ClassPathResource("prompts/ai_optimizer_prompt.st"));

            Prompt prompt = template.create(
                    Map.of(
                            "history", historyJson,
                            "deliveries", deliveriesJson,
                            "vehicle_capacity", vehicle.getMaxDeliveries(),
                            "warehouse_latitude", warehouse.getLatitude(),
                            "warehouse_longitude", warehouse.getLongitude()
                    )
            );

            var response = aiClient.call(prompt);
            String rawOutput = response.getResult().getOutput().getText();

            String jsonOutput = sanitizeJson(rawOutput);
            AIOptimizationResponse result = objectMapper.readValue(jsonOutput, AIOptimizationResponse.class);

            log.info("AI Optimization Metrics: {}", result.getKeyMetrics());

            return mapOrderedIdsToDeliveries(result.getOrderedIds(), deliveries);

        } catch (Exception e) {
            log.error("AI Optimization failed: {}. Falling back to original sequence.", e.getMessage(), e);
            return deliveries;
        }
    }

    /**
     * Maps the List of IDs returned by AI back to the actual Delivery objects.
     */
    private List<Delivery> mapOrderedIdsToDeliveries(List<Long> orderedIds, List<Delivery> originalDeliveries) {
        Map<Long, Delivery> deliveryMap = originalDeliveries.stream()
                .collect(Collectors.toMap(Delivery::getId, Function.identity()));

        List<Delivery> optimizedRoute = new ArrayList<>();

        for (Long id : orderedIds) {
            if (deliveryMap.containsKey(id)) {
                optimizedRoute.add(deliveryMap.get(id));
            } else {
                log.warn("AI returned an ID ({}) that was not in the original list.", id);
            }
        }

        if (optimizedRoute.size() < originalDeliveries.size()) {
            log.warn("⚠AI missed some deliveries. Appending missing ones.");
            for (Delivery d : originalDeliveries) {
                if (!optimizedRoute.contains(d)) {
                    optimizedRoute.add(d);
                }
            }
        }

        return optimizedRoute;
    }

    private String getDeliveryHistory() throws JsonProcessingException {
        var histories = historyRepository.findTop50ByOrderByDeliveryDateDesc();
        return objectMapper.writeValueAsString(histories);
    }

    private String sanitizeJson(String input) {
        if (input.contains("```json")) {
            return input.replace("```json", "").replace("```", "").trim();
        } else if (input.contains("```")) {
            return input.replace("```", "").trim();
        }
        return input;
    }
}
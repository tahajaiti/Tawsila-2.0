package com.kyojin.tawsila.optimizer.impl;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.optimizer.TourOptimizer;
import com.kyojin.tawsila.repository.DeliveryHistoryRepository;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AIOptimizer implements TourOptimizer {

    private final AzureOpenAiChatModel chatModel;
    private final DeliveryHistoryRepository deliveryHistoryRepository;

    @Autowired
    public AIOptimizer(AzureOpenAiChatModel chatModel, DeliveryHistoryRepository deliveryHistoryRepository) {
        this.chatModel = chatModel;
        this.deliveryHistoryRepository = deliveryHistoryRepository;
    }


    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        return List.of();
    }
}
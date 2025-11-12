package com.kyojin.tawsila.optimizer.impl;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.optimizer.TourOptimizer;

import java.util.List;

public class AIOptimizer implements TourOptimizer {

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        return List.of();
    }
}
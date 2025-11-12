package com.kyojin.tawsila.optimizer.impl;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.enums.VehicleType;
import com.kyojin.tawsila.optimizer.TourOptimizer;
import com.kyojin.tawsila.util.DistanceCalculator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(value = "tawsila.optimizer.type", havingValue = "NEAREST_NEIGHBOR")
public class NearestNeighborOptimizer implements TourOptimizer {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class State {
        double currentLat;
        double currentLon;
        double currentWeight;
        double currentVolume;
        int currentStops;

        public void update(Delivery delivery) {
            this.currentLat = delivery.getCustomer().getLatitude();
            this.currentLon = delivery.getCustomer().getLongitude();
            this.currentWeight += delivery.getWeightKg();
            this.currentVolume += delivery.getVolumeM3();
            this.currentStops++;
        }
    }

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        if (deliveries == null || deliveries.isEmpty()) return new ArrayList<>();

        VehicleType vType = vehicle.getType();

        List<Delivery> notVisited = new ArrayList<>();

        // we fill notVisited with a list of deliveries that can be handled by the vehicle
        // doing sanity checks
        for (Delivery del : deliveries) {
           if (vType.canHandle(del.getWeightKg(), del.getVolumeM3())) {
            notVisited.add(del);
           }
        }

        // edge cases
        if (notVisited.isEmpty()) return new ArrayList<>();
        if (notVisited.size() == 1) return notVisited;

        // the final optimized route
        List<Delivery> optimized = new ArrayList<>();

        // initial state
        State state = new State(
                warehouse.getLatitude(),
                warehouse.getLongitude(),
                0,
                0,
                0
        );

        // we loop until we have visited all deliveries, or reach the vehicle's max deliveries
        while (!notVisited.isEmpty() && state.getCurrentStops() < vehicle.getMaxDeliveries()) {
            Delivery nearest = findNearestDelivery(notVisited, state, vehicle);

            if (nearest == null) break; // no more deliveries that fit capacity

            optimized.add(nearest);
            notVisited.remove(nearest);
            state.update(nearest);
        }

        return optimized;

    }

    /**
     * Finds the nearest delivery from the current state that can fit in the vehicle.
     * @param deliveries list of deliveries to consider
     * @param state current state of the vehicle
     * @param vehicle the vehicle being used
     * @return the nearest delivery that fits, or null if none found
     */
    private Delivery findNearestDelivery(List<Delivery> deliveries, State state, Vehicle vehicle) {
        Delivery nearest = null;
        double minDistance = Double.MAX_VALUE; // define a large number minimum distance

        for (Delivery del : deliveries) {
            if (!canFit(del, vehicle, state)) continue;

            // calculate distance from current position to delivery
            double dist = DistanceCalculator.calculateDistance(
                    state.getCurrentLat(), state.getCurrentLon(),
                    del.getCustomer().getLatitude(), del.getCustomer().getLongitude()
            );

            // we check if it is closer than the current minimum
            // and update nearest if so
            if (dist < minDistance) {
                minDistance = dist;
                nearest = del;
            }
        }

        return nearest;
    }


    /**
     * Checks if a delivery can fit in the vehicle given the current state.
     * @param delivery the delivery to check
     * @param vehicle the vehicle being used
     * @param state the current state of the vehicle
     * @return true if the delivery can fit, false otherwise
     */
    private boolean canFit(Delivery delivery, Vehicle vehicle, State state) {
        // calculate new state if we add this delivery
        double newWeight = state.getCurrentWeight() + delivery.getWeightKg();
        double newVolume = state.getCurrentVolume() + delivery.getVolumeM3();
        int newStops = state.getCurrentStops() + 1;

        // we call the vehicle type handle method to check if it fits
        return vehicle.getType().canHandle(newWeight, newVolume, newStops);
    }
}

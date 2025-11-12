package com.kyojin.tawsila.optimizer.impl;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.enums.VehicleType;
import com.kyojin.tawsila.optimizer.TourOptimizer;
import com.kyojin.tawsila.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConditionalOnProperty(value = "tawsila.optimizer.type", havingValue = "CLARKE_WRIGHT")
public class ClarkeWrightOptimizer implements TourOptimizer {

    @RequiredArgsConstructor
    private static class Saving implements Comparable<Saving> {
        final Delivery from;
        final Delivery to;
        final double amount; // how much distance saved

        @Override
        public int compareTo(Saving other) {
            // we sort in descending order of savings
            int cmp = Double.compare(other.amount, this.amount);
            if (cmp == 0) return Long.compare(this.from.getId(), other.from.getId());
            return cmp;
        }
    }

    private static class SubTour {
        LinkedList<Delivery> deliveries = new LinkedList<>(); // use linked list because THEY ARE LINKED and fast
        double currentWeight = 0.0;
        double currentVolume = 0.0;

        SubTour(Delivery del) {
            this.deliveries.add(del);
            this.currentWeight = del.getWeightKg();
            this.currentVolume = del.getVolumeM3();
        }

        Delivery getFirst() { return deliveries.getFirst(); }
        Delivery getLast()  { return deliveries.getLast(); }
        int getSize()  { return deliveries.size(); }
    }

    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        if (deliveries == null || deliveries.isEmpty()) return new ArrayList<>();

        VehicleType vehicleType = vehicle.getType();

        List<Delivery> notVisited = findDeliveries(deliveries, vehicleType);

        // same method from before to filter deliveries that can be handled by the vehicle
        for (Delivery del : deliveries) {
            if (vehicleType.canHandle(del.getWeightKg(), del.getVolumeM3())) {
                notVisited.add(del);
            }
        }

        // edge cases
        if (notVisited.isEmpty()) return new ArrayList<>();
        if (notVisited.size() == 1) return notVisited;

        // calculate all possible savings
        List<Saving> savings = calculateSavings(warehouse, notVisited);

        // each delivery starts as its own subtour
        Map<Delivery, SubTour> tourMap = initSubTours(notVisited);

        // we merge subtours based on savings and vehicle constraints
        merge(savings, tourMap, vehicleType);

        return findBestTour(tourMap);
    }


    /**
     * Filters deliveries that can be handled by the vehicle type.
     * @param deliveries List of all deliveries.
     * @param vType Vehicle type.
     * @return List of eligible deliveries.
     */
    private List<Delivery> findDeliveries(List<Delivery> deliveries, VehicleType vType) {
        List<Delivery> eligible = new ArrayList<>();
        for (Delivery del : deliveries) {
            // constraint check
            if (vType.canHandle(del.getWeightKg(), del.getVolumeM3())) {
                eligible.add(del);
            }
        }
        return eligible;
    }


    /**
     * Calculates savings for all pairs of deliveries.
     * @param warehouse The warehouse (depot).
     * @param deliveries List of deliveries.
     * @return List of savings.
     */
    private List<Saving> calculateSavings(Warehouse warehouse, List<Delivery> deliveries) {
        List<Saving> savings = new ArrayList<>();
        for (int i = 0; i < deliveries.size(); i++) {
            for (int j = i + 1; j < deliveries.size(); j++) {
                Delivery del1 = deliveries.get(i);
                Delivery del2 = deliveries.get(j);

                // saving(i,j) = (dist(warehouse, i) + dist(warehouse, j)) - dist(i, j)
                double savingAmount = dist(warehouse, del1) + dist(warehouse, del2) - dist(del1, del2);

                if (savingAmount > 0) {
                    savings.add(new Saving(del1, del2, savingAmount));
                }
            }
        }
        // sort from high to low
        Collections.sort(savings);
        return savings;
    }

    /**
     * Initializes each delivery as its own subtour.
     * @param deliveries List of deliveries.
     * @return Map of delivery to its subtour.
     */
    private Map<Delivery, SubTour> initSubTours(List<Delivery> deliveries) {
        Map<Delivery, SubTour> tourMap = new HashMap<>();
        for (Delivery del : deliveries) {
            tourMap.put(del, new SubTour(del));
        }
        return tourMap;
    }


    /**
     * Merges subtours based on savings and vehicle constraints.
     * @param savings List of savings.
     * @param tourMap Map of delivery to its subtour.
     * @param vType Vehicle type.
     */
    private void merge(List<Saving> savings, Map<Delivery, SubTour> tourMap, VehicleType vType) {
        for (Saving saving : savings) {
            SubTour tour1 = tourMap.get(saving.from);
            SubTour tour2 = tourMap.get(saving.to);

            // sanity check if they are already merged
            if (tour1 == tour2) {
                continue;
            }

            // checks
            double combinedWeight = tour1.currentWeight + tour2.currentWeight;
            double combinedVolume = tour1.currentVolume + tour2.currentVolume;
            int combinedStops = tour1.getSize() + tour2.getSize();

            if (!vType.canHandle(combinedWeight, combinedVolume, combinedStops)) {
                continue;
            }

            tryMerge(saving, tour1, tour2, tourMap);
        }
    }

    /**
     * Attempts to merge two subtours based on the saving.
     * @param saving The saving object.
     * @param tour1 First subtour.
     * @param tour2 Second subtour.
     * @param tourMap Map of delivery to its subtour.
     * @return True if merged, false otherwise.
     */
    private boolean tryMerge(Saving saving, SubTour tour1, SubTour tour2, Map<Delivery, SubTour> tourMap) {
        Delivery del1 = saving.from;
        Delivery del2 = saving.to;

        // case 1 (end-start)
        if (tour1.getLast().equals(del1) && tour2.getFirst().equals(del2)) {
            mergeTours(tourMap, tour1, tour2); // merges tour2 into tour1
        }
        // case 2 (start-end)
        else if (tour1.getFirst().equals(del1) && tour2.getLast().equals(del2)) {
            mergeTours(tourMap, tour2, tour1);
        }
        // case 3 (end-end)
        else if (tour1.getLast().equals(del1) && tour2.getLast().equals(del2)) {
            Collections.reverse(tour2.deliveries);// reverse tour2
            mergeTours(tourMap, tour1, tour2);
        }
        // case 4 (start-start)
        else if (tour1.getFirst().equals(del1) && tour2.getFirst().equals(del2)) {
            Collections.reverse(tour1.deliveries);
            mergeTours(tourMap, tour1, tour2);
        } else {
            return false; // cannot merge
        }
        return true;
    }

    /**
     * Finds the best tour (with most stops) from the subtours.
     * @param tourMap Map of delivery to its subtour.
     * @return List of deliveries in the best tour.
     */
    private List<Delivery> findBestTour(Map<Delivery, SubTour> tourMap) {
        SubTour bestTour = null;
        int maxStops = 0;

        // we use a set to avoid duplicates
        Set<SubTour> finalTours = new HashSet<>(tourMap.values());

        for (SubTour tour : finalTours) {
            if (tour.getSize() > maxStops) {
                maxStops = tour.getSize();
                bestTour = tour;
            }
        }

        return (bestTour != null) ? bestTour.deliveries : new ArrayList<>();
    }

    /**
     * Calculates distance between Warehouse and a Delivery.
     */
    private double dist(Warehouse w, Delivery d) {
        return DistanceCalculator.calculateDistance(
                w.getLatitude(), w.getLongitude(),
                d.getLatitude(), d.getLongitude()
        );
    }

    /**
     * Calculates distance between two Deliveries.
     */
    private double dist(Delivery d1, Delivery d2) {
        return DistanceCalculator.calculateDistance(
                d1.getLatitude(), d1.getLongitude(),
                d2.getLatitude(), d2.getLongitude()
        );
    }

    /**
     * Merges tour2 into tour1 and updates the tourMap accordingly.
     */
    private void mergeTours(Map<Delivery, SubTour> tourMap, SubTour tour1, SubTour tour2) {
        // add all deliveries from tour2 to tour1
        tour1.deliveries.addAll(tour2.deliveries);

        // also add the volume and weight
        tour1.currentWeight += tour2.currentWeight;
        tour1.currentVolume += tour2.currentVolume;

        // update the map to point all deliveries in tour2 to tour1
        for (Delivery del: tour2.deliveries) {
            tourMap.put(del, tour1);
        }

    }
}

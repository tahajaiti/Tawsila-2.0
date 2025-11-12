package com.kyojin.tawsila.optimizer;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.enums.VehicleType;
import com.kyojin.tawsila.optimizer.impl.NearestNeighborOptimizer;
import com.kyojin.tawsila.util.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mockStatic;

public class NearestNeighborOptimizerTest {

    private TourOptimizer optimizer;
    private Warehouse warehouse;
    private Vehicle vehicle;


    @BeforeEach
    void setUp() {
        optimizer = new NearestNeighborOptimizer();

        warehouse = new Warehouse();
        warehouse.setLatitude(0.0);
        warehouse.setLongitude(0.0);

        vehicle = new Vehicle();
        vehicle.setType(VehicleType.VAN);
        vehicle.setMaxDeliveries(5);
    }

    @Test
    void testDeliveriesInNearestNeighborOrder() {
        Delivery d1 = delivery(1L, 1, 1, 10, 2);
        Delivery d2 = delivery(2L, 2, 2, 5, 1);
        Delivery d3 = delivery(3L, 3, 3, 8, 1);

        List<Delivery> deliveries = List.of(d1, d2, d3);


        try (MockedStatic<DistanceCalculator> mockStatic = mockStatic(DistanceCalculator.class)) {

            // distances from warehouse (0,0)
            mockStatic.when(() -> DistanceCalculator.calculateDistance(0, 0, 1, 1)).thenReturn(5.0);
            mockStatic.when(() -> DistanceCalculator.calculateDistance(0, 0, 2, 2)).thenReturn(10.0);
            mockStatic.when(() -> DistanceCalculator.calculateDistance(0, 0, 3, 3)).thenReturn(15.0);

            // distance between deliveries
            mockStatic.when(() -> DistanceCalculator.calculateDistance(1, 1, 2, 2)).thenReturn(3.0);
            mockStatic.when(() -> DistanceCalculator.calculateDistance(1, 1, 3, 3)).thenReturn(7.0);
            mockStatic.when(() -> DistanceCalculator.calculateDistance(2, 2, 3, 3)).thenReturn(4.0);

            List<Delivery> result = optimizer.calculateOptimalTour(warehouse, deliveries, vehicle);

            // expected order: d1 -> d2 -> d3
            assertEquals(3, result.size());
            assertEquals(1L, result.get(0).getId()); // d1 first
            assertEquals(2L, result.get(1).getId()); // then d2 (nearest to d1)
            assertEquals(3L, result.get(2).getId()); // finally d3
        }
    }

    @Test
    void testVehicleMaxDeliveriesReached() {
        vehicle.setMaxDeliveries(2);

        Delivery d1 = delivery(1L, 1, 1, 10, 2);
        Delivery d2 = delivery(2L, 2, 2, 5, 1);
        Delivery d3 = delivery(3L, 3, 3, 8, 1);

        List<Delivery> deliveries = List.of(d1, d2, d3);

        try (MockedStatic<DistanceCalculator> mockStatic = mockStatic(DistanceCalculator.class)) {
            mockStatic.when(() -> DistanceCalculator.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(1.0);

            List<Delivery> result = optimizer.calculateOptimalTour(warehouse, deliveries, vehicle);

            assertEquals(2, result.size());
        }
    }


    private Delivery delivery(Long id, double lat, double lon, double weight, double volume) {
        Delivery d = new Delivery();
        d.setId(id);
        d.getCustomer().setLatitude(lat);
        d.getCustomer().setLongitude(lon);
        d.setWeightKg(weight);
        d.setVolumeM3(volume);
        return d;
    }
}

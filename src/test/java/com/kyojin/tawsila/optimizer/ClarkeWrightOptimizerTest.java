package com.kyojin.tawsila.optimizer;

import com.kyojin.tawsila.entity.Delivery;
import com.kyojin.tawsila.entity.Vehicle;
import com.kyojin.tawsila.entity.Warehouse;
import com.kyojin.tawsila.enums.VehicleType;
import com.kyojin.tawsila.optimizer.impl.ClarkeWrightOptimizer;
import com.kyojin.tawsila.util.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class ClarkeWrightOptimizerTest {
    ClarkeWrightOptimizer optimizer;
    Warehouse warehouse;
    Vehicle vehicle;

    Delivery A, B, C, D;


    @BeforeEach
    void setUp() {
        optimizer = new ClarkeWrightOptimizer();

        warehouse = new Warehouse();
        warehouse.setLatitude(0.0);
        warehouse.setLongitude(0.0);

        vehicle = new Vehicle();
        vehicle.setType(VehicleType.TRUCK);
        vehicle.setMaxDeliveries(10);

        // create deliveries
        A = delivery(1L, 1, 1, 5, 2);
        B = delivery(2L, 2, 2, 5, 2);
        C = delivery(3L, 3, 3, 5, 2);
        D = delivery(4L, 4, 4, 5, 2);
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

    @Test
    void testClarkeWright4Clients() {
        try (MockedStatic<DistanceCalculator> mock = mockStatic(DistanceCalculator.class)) {
            // warehouse distances
            mock.when(() -> DistanceCalculator.calculateDistance(0, 0, 1, 1)).thenReturn(5.0);  // W->1
            mock.when(() -> DistanceCalculator.calculateDistance(0, 0, 2, 2)).thenReturn(15.0); // W->2
            mock.when(() -> DistanceCalculator.calculateDistance(0, 0, 3, 3)).thenReturn(10.0); // W->3
            mock.when(() -> DistanceCalculator.calculateDistance(0, 0, 4, 4)).thenReturn(20.0); // W->4

            // distances between deliveries
            mock.when(() -> DistanceCalculator.calculateDistance(1,1,2,2)).thenReturn(12.0); // 1-2
            mock.when(() -> DistanceCalculator.calculateDistance(1,1,3,3)).thenReturn(8.0);  // 1-3
            mock.when(() -> DistanceCalculator.calculateDistance(1,1,4,4)).thenReturn(18.0); // 1-4
            mock.when(() -> DistanceCalculator.calculateDistance(2,2,3,3)).thenReturn(7.0);  // 2-3
            mock.when(() -> DistanceCalculator.calculateDistance(2,2,4,4)).thenReturn(6.0);  // 2-4
            mock.when(() -> DistanceCalculator.calculateDistance(3,3,4,4)).thenReturn(11.0); // 3-4

            List<Delivery> deliveries = List.of(A, B, C, D);
            List<Delivery> tour = optimizer.calculateOptimalTour(warehouse, deliveries, vehicle);

            // final expected order based on example: W->1->2->3->4->W
            assertEquals(4, tour.size());
            assertEquals(A.getId(), tour.get(0).getId());
            assertEquals(B.getId(), tour.get(1).getId());
            assertEquals(D.getId(), tour.get(2).getId());
            assertEquals(C.getId(), tour.get(3).getId());
        }
    }

}

package com.kyojin.tawsila.config;

import com.kyojin.tawsila.entity.Warehouse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

@Configuration
public class WarehouseConfig {


    @Bean
    public Warehouse warehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setAddress("Darbida");
        warehouse.setLatitude(33.5731);
        warehouse.setLongitude(-7.5898);
        warehouse.setOpenTime(LocalTime.parse("06:00"));
        warehouse.setCloseTime(LocalTime.parse("22:00"));
        return warehouse;
    }
}

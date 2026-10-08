package com.example.kitchenpalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KitchenPalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KitchenPalApiApplication.class, args);
    }

}

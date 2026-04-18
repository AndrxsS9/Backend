package com.monitoring.backend.service;

import org.springframework.stereotype.Service;


@Service
public class InventoryService {

    public String checkStock(String productId) {
        // Simulating some processing time
        sleepRandom(10, 50);
        return "Stock available for " + productId;
    }

    public String reduceStock(String productId, Integer quantity) {
        sleepRandom(20, 100);
        return "Reduced stock of " + productId + " by " + quantity;
    }

    private void sleepRandom(int min, int max) {
        try {
            int time = min + (int)(Math.random() * ((max - min) + 1));
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

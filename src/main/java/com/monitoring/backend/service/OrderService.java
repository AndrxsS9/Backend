package com.monitoring.backend.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    public String createOrder(String customerId, String productId) {
        sleepRandom(30, 80);
        return "Order " + UUID.randomUUID().toString() + " created for customer " + customerId;
    }

    public String cancelOrder(String orderId) {
        sleepRandom(10, 40);
        return "Order " + orderId + " cancelled";
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

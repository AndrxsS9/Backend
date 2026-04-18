package com.monitoring.backend.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    private final Random random = new Random();

    public String processPayment(String orderId, Double amount) {
        sleepRandom(50, 150);
        
        // Simular fallas aleatorias: 10% de las llamadas a PaymentService fallan intencionalmente
        if (random.nextInt(100) < 10) {
            throw new RuntimeException("Payment Gateway Timeout for order " + orderId);
        }
        
        return "Payment of $" + amount + " successful for order " + orderId;
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

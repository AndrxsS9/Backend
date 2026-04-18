package com.monitoring.backend.controller;

import com.monitoring.backend.proxy.LoggingProxy;
import com.monitoring.backend.proxy.MicroserviceProxy;
import com.monitoring.backend.repository.LogRepository;
import com.monitoring.backend.service.InventoryService;
import com.monitoring.backend.service.OrderService;
import com.monitoring.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*") // Para conectar fácilmente con el frontend
public class ServiceController {

    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> orderProxy;
    private final MicroserviceProxy<Object> paymentProxy;

    public ServiceController(InventoryService inventoryService,
                             OrderService orderService,
                             PaymentService paymentService,
                             LogRepository logRepository) {
        this.inventoryProxy = new LoggingProxy<>("inventory", inventoryService, logRepository);
        this.orderProxy = new LoggingProxy<>("orders", orderService, logRepository);
        this.paymentProxy = new LoggingProxy<>("payments", paymentService, logRepository);
    }

    @PostMapping("/inventory/{operation}")
    public ResponseEntity<?> callInventory(@PathVariable String operation, @RequestBody(required = false) Object[] params) {
        return executeProxy(inventoryProxy, operation, params);
    }

    @PostMapping("/orders/{operation}")
    public ResponseEntity<?> callOrders(@PathVariable String operation, @RequestBody(required = false) Object[] params) {
        return executeProxy(orderProxy, operation, params);
    }

    @PostMapping("/payments/{operation}")
    public ResponseEntity<?> callPayments(@PathVariable String operation, @RequestBody(required = false) Object[] params) {
        return executeProxy(paymentProxy, operation, params);
    }

    private ResponseEntity<?> executeProxy(MicroserviceProxy<Object> proxy, String operation, Object[] params) {
        try {
            Object[] args = params != null ? params : new Object[0];
            Object result = proxy.execute(operation, args);
            return ResponseEntity.ok(Map.of("message", "Success", "result", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal Server Error"));
        }
    }
}

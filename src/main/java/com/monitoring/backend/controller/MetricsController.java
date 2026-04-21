package com.monitoring.backend.controller;

import com.monitoring.backend.model.LogEntry;
import com.monitoring.backend.model.PaginatedResponse;
import com.monitoring.backend.model.ServiceSummary;
import com.monitoring.backend.repository.LogRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    private final LogRepository logRepository;
    private final ServiceController serviceController;

    public MetricsController(LogRepository logRepository, ServiceController serviceController) {
        this.logRepository = logRepository;
        this.serviceController = serviceController;
    }

    @GetMapping("/summary")
    public ResponseEntity<List<ServiceSummary>> getSummary() {
        List<LogEntry> logs = logRepository.findAll();

        Map<String, List<LogEntry>> logsByService = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::getServiceId));

        List<ServiceSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<LogEntry>> entry : logsByService.entrySet()) {
            String serviceId = entry.getKey();
            List<LogEntry> serviceLogs = entry.getValue();

            long totalCalls = serviceLogs.size();
            long errorCalls = serviceLogs.stream().filter(l -> "ERROR".equals(l.getStatus())).count();
            double errorRate = totalCalls == 0 ? 0 : (double) errorCalls / totalCalls * 100.0;
            double avgTime = serviceLogs.stream().mapToLong(LogEntry::getDurationMs).average().orElse(0.0);

            summaries.add(new ServiceSummary(serviceId, totalCalls, errorCalls, errorRate, avgTime));
        }

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/logs")
    public ResponseEntity<PaginatedResponse<LogEntry>> getLogs(
            @RequestParam(required = false, name = "service") String serviceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<LogEntry> filteredLogs = logRepository.findFiltered(serviceId, status, from, to);

        int totalElements = filteredLogs.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = Math.min(page * size, totalElements);
        int end = Math.min((page + 1) * size, totalElements);
        List<LogEntry> pageContent = filteredLogs.subList(start, end);

        PaginatedResponse<LogEntry> response = PaginatedResponse.<LogEntry>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/simulate-load", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> simulateLoad() {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            int serviceChoice = random.nextInt(3);
            try {
                if (serviceChoice == 0) {
                    serviceController.callInventory("checkStock", new Object[]{"PROD-" + random.nextInt(100)});
                } else if (serviceChoice == 1) {
                    serviceController.callOrders("createOrder", new Object[]{"CUST-" + random.nextInt(100), "PROD-" + random.nextInt(100)});
                } else {
                    serviceController.callPayments("processPayment", new Object[]{"ORD-" + random.nextInt(100), random.nextDouble() * 100});
                }
            } catch (Exception e) {
                // Ignore, logged by proxy
            }
        }
        return ResponseEntity.ok(Map.of("message", "Simulated 50 requests successfully"));
    }
}

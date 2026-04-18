package com.monitoring.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSummary {
    private String serviceId;
    private long totalCalls;
    private long errorCalls;
    private double errorRate;
    private double averageResponseTimeMs;
}

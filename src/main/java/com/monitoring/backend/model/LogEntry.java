package com.monitoring.backend.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LogEntry {
    private String requestId;
    private String serviceId;
    private String operation;
    private long durationMs;
    private String status; // "SUCCESS" or "ERROR"
    private LocalDateTime timestamp;
    private String errorMessage;
    private String errorStackTrace;
}

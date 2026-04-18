package com.monitoring.backend.repository;

import com.monitoring.backend.model.LogEntry;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class LogRepository {

    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();

    public void save(LogEntry logEntry) {
        logs.add(logEntry);
    }

    public List<LogEntry> findAll() {
        return logs;
    }

    public List<LogEntry> findFiltered(String serviceId, String status, LocalDateTime from, LocalDateTime to) {
        return logs.stream()
                .filter(log -> serviceId == null || serviceId.isEmpty() || log.getServiceId().equalsIgnoreCase(serviceId))
                .filter(log -> status == null || status.isEmpty() || log.getStatus().equalsIgnoreCase(status))
                .filter(log -> from == null || !log.getTimestamp().isBefore(from))
                .filter(log -> to == null || !log.getTimestamp().isAfter(to))
                .sorted(Comparator.comparing(LogEntry::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public void clear() {
        logs.clear();
    }
}

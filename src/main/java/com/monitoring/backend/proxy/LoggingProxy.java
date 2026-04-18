package com.monitoring.backend.proxy;

import com.monitoring.backend.model.LogEntry;
import com.monitoring.backend.repository.LogRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoggingProxy<R, T> implements MicroserviceProxy<R> {

    private final String serviceId;
    private final T target;
    private final LogRepository logRepository;

    public LoggingProxy(String serviceId, T target, LogRepository logRepository) {
        this.serviceId = serviceId;
        this.target = target;
        this.logRepository = logRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public R execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();

        try {
            Method targetMethod = findMethod(operation, params);
            if (targetMethod == null) {
                throw new IllegalArgumentException("Operation " + operation + " not found in service " + serviceId);
            }

            Object result = targetMethod.invoke(target, params);

            long duration = System.currentTimeMillis() - startMillis;
            recordLog(requestId, operation, duration, "SUCCESS", startTime, null);
            
            return (R) result;

        } catch (InvocationTargetException e) {
            long duration = System.currentTimeMillis() - startMillis;
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            recordLog(requestId, operation, duration, "ERROR", startTime, cause);
            throw new RuntimeException("Error executing operation " + operation, cause);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startMillis;
            recordLog(requestId, operation, duration, "ERROR", startTime, e);
            throw new RuntimeException("Error executing operation " + operation, e);
        }
    }

    private void recordLog(String requestId, String operation, long durationMs, String status, LocalDateTime timestamp, Throwable error) {
        LogEntry logEntry = LogEntry.builder()
                .requestId(requestId)
                .serviceId(serviceId)
                .operation(operation)
                .durationMs(durationMs)
                .status(status)
                .timestamp(timestamp)
                .errorMessage(error != null ? error.getMessage() : null)
                .errorStackTrace(error != null ? getTruncatedStackTrace(error) : null)
                .build();
        logRepository.save(logEntry);
    }

    private Method findMethod(String methodName, Object[] params) {
        int paramCount = (params == null) ? 0 : params.length;
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        return null;
    }

    private String getTruncatedStackTrace(Throwable error) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (StackTraceElement element : error.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            count++;
            if (count > 5) break;
        }
        return sb.toString();
    }
}

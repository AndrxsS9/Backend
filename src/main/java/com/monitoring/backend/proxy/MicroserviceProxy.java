package com.monitoring.backend.proxy;

public interface MicroserviceProxy<T> {
    T execute(String operation, Object... params);
}

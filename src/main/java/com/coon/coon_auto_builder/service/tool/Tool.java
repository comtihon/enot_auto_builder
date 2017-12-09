package com.coon.coon_auto_builder.service.tool;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public abstract class Tool implements HealthIndicator {
    protected String message = "";
    boolean ready = false;
    String version;

    public String getMessage() {
        return message;
    }

    public boolean isReady() {
        return ready;
    }

    public String getVersion() {
        return version;
    }

    public abstract boolean check();

    public abstract boolean install();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " version='" + version + "'";
    }

    @Override
    public Health health() {
        if (ready)
            return Health.up().withDetail("version", version).build();
        return Health.down().withDetail("error", message).build();
    }
}

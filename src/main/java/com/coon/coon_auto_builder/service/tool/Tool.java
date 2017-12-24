package com.coon.coon_auto_builder.service.tool;

import lombok.Getter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public abstract class Tool implements HealthIndicator {
    @Getter
    protected String message = "";
    @Getter
    boolean ready = false;
    @Getter
    String version;

    public abstract boolean check();

    public abstract boolean install();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " version='" + version + "'";
    }

    @Override
    public Health health() {
        if (ready)
            return Health.up().withDetail("Version", version).build();
        return Health.down().withDetail("Error", message).build();
    }
}

package com.coon.coon_auto_builder.service.tool;

import lombok.Getter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

@Getter
public abstract class Tool implements HealthIndicator {
    protected String message = "";
    boolean ready = false;
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
            return Health.up().withDetail("version", version).build();
        return Health.down().withDetail("error", message).build();
    }
}

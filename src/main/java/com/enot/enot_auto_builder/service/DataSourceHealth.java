package com.enot.enot_auto_builder.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceHealth implements HealthIndicator {

    private HikariPool pool;

    @Autowired
    public DataSourceHealth(DataSource dataSource) {
        if (dataSource.getClass().equals(HikariDataSource.class))
            pool = (HikariPool) new DirectFieldAccessor(dataSource).getPropertyValue("pool");
    }


    @Override
    public Health health() {
        if (pool != null)
            return Health.up()
                    .withDetail("active", pool.getActiveConnections())
                    .withDetail("idle", pool.getIdleConnections())
                    .withDetail("total", pool.getTotalConnections())
                    .withDetail("waiting", pool.getThreadsAwaitingConnection())
                    .build();
        else
            return Health.down().build();
    }
}

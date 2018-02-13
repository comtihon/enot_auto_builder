package com.enot.enot_auto_builder.service;

public enum Metrics {
    CLONE_OK("gauge.package.clone.ok"),
    CLONE_FAIL("gauge.package.clone.fail"),
    BUILD_OK("gauge.package.build.ok"),
    BUILD_FAIL("gauge.package.build.fail"),
    LOAD_ALL("counter.package.load.all"),
    LOAD_OK("gauge.package.load.success"),
    LOAD_FAIL("gauge.package.load.fail");

    private final String metricName;

    Metrics(String metricName) {
        this.metricName = metricName;
    }

    @Override
    public String toString() {
        return metricName;
    }
}

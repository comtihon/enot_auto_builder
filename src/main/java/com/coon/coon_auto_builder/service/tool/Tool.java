package com.coon.coon_auto_builder.service.tool;

public abstract class Tool {
    protected String message = "";
    protected boolean ready = false;
    protected String version;

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
        return getClass().getSimpleName() + "version='" + version + "'";
    }
}

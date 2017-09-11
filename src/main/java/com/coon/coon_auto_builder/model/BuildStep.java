package com.coon.coon_auto_builder.model;

import com.coon.coon_auto_builder.domain.BuildResult;
import com.coon.coon_auto_builder.system.Status;

public class BuildStep {
    private String ref;
    private String fullName;
    private Status state;
    private String erlangVsn;
    private boolean result = true;
    private String message = "";
    private String url;

    public BuildStep withRef(String ref) {
        this.ref = ref;
        return this;
    }

    public BuildStep withName(String name) {
        this.fullName = name;
        return this;
    }

    public BuildStep withStatus(Status state) {
        this.state = state;
        return this;
    }

    public BuildStep withErlangVsn(String erlangVsn) {
        this.erlangVsn = erlangVsn;
        return this;
    }

    public BuildStep withResult(boolean result) {
        this.result = result;
        return this;
    }

    public BuildStep withMessage(String message) {
        this.message = message;
        return this;
    }

    public BuildStep withUrl(String url) {
        this.url = url;
        return this;
    }

    public BuildResult toResult() {
        return new BuildResult(fullName, ref, state, erlangVsn, url, result, message);
    }
}

package com.coon.coon_auto_builder.model;

import com.coon.coon_auto_builder.system.Status;

public class BuildResult {
    final private String ref;
    final private String fullName;
    final private Status state;
    final private String erlangVsn;
    final private boolean result;
    final private String message;

    public BuildResult(String fullName, String ref, Status state, String erlangVsn, boolean result, String message) {
        this.fullName = fullName;
        this.ref = ref;
        this.state = state;
        this.erlangVsn = erlangVsn;
        this.result = result;
        this.message = message;
    }
}

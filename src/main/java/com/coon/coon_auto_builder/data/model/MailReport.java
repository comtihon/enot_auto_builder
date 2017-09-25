package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.system.Status;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MailReport {

    private String to;
    private String packageName;
    private Status status;
    private String ref;
    private String subject;
    private Date buildDate;

    private boolean successs;

    private Map<String, BuildBO> results;

    MailReport(String to, String packageName, Status status,
               boolean successs, String ref, @Nullable RepositoryBO repository) {
        this.to = to;
        this.packageName = packageName;
        this.status = status;
        this.successs = successs;
        this.ref = ref;
        if (repository != null) {
            this.results = repository.getBuilds();
        } else {
            this.results = new HashMap<>();
        }
        this.buildDate = new Date();
        if (successs) {
            subject = "Coon build for " + packageName + " " + ref + " succeed";
        } else
            subject = "Coon build for " + packageName + " " + ref + " failed";
    }

    public String getPackageName() {
        return packageName;
    }

    public Status getStatus() {
        return status;
    }

    public String getRef() {
        return ref;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public boolean isSuccesss() {
        return successs;
    }

    public Map<String, BuildBO> getResults() {
        return results;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }
}

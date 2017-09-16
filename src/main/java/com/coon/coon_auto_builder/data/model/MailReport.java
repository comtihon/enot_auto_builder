package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.system.Status;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MailReport {

    private String to;
    private String packageName;
    private Status status;
    private String ref;

    private boolean successs;

    private Map<String, PackageBuilder> body;

    MailReport(String to, String packageName, Status status, boolean successs,
               Map<String, PackageBuilder> builders, String ref) {
        this.to = to;
        this.packageName = packageName;
        this.status = status;
        this.successs = successs;
        this.ref = ref;
        this.body = builders;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        if (successs) {
            return "Coon build for " + packageName + " " + ref + " succeed";
        } else
            return "Coon build for " + packageName + " " + ref + " failed";
    }

    public Context getContext() {
        final Context ctx = new Context();
        ctx.setVariable("subject", getSubject());
        ctx.setVariable("buildDate", new Date());
        ctx.setVariable("logs", body);
        ctx.setVariable("name", packageName);
        ctx.setVariable("successs", successs);
        ctx.setVariable("status", status);
        ctx.setVariable("ref", ref);
        return ctx;
    }
}

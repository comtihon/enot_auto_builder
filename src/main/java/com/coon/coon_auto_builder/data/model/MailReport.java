package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.system.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MailReport {

    private String to;
    private String packageName;
    private Status status;

    private boolean successs;

    private List<String> body;

    MailReport(String to, String packageName, Status status, boolean successs, Map<String, PackageBuilder> builders) {
        this.to = to;
        this.packageName = packageName;
        this.status = status;
        this.successs = successs;
        formBody(builders);
    }

    public String getTo() {
        return to;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isSuccesss() {
        return successs;
    }

    public String getBody() {
        return body.toString(); //TODO use list in timeleaf without toString
    }

    private void formBody(Map<String, PackageBuilder> builders) {
        body = new ArrayList<>();
        body.add("Status is " + status + "\n");
        StringBuilder builder;
        for (Map.Entry<String, PackageBuilder> entry : builders.entrySet()) {
            builder = new StringBuilder();
            builder.append(entry.getKey()).append("\n").append(entry.getValue().getFailMessage()).append("\n");
            body.add(builder.toString());
        }
    }
}

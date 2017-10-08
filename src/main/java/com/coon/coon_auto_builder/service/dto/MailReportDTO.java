package com.coon.coon_auto_builder.service.dto;

import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.service.Status;

import java.util.List;
import java.util.Map;

public class MailReportDTO {

    private String to;
    private String packageName;
    private Status status;
    private String ref;

    private boolean successs;

    private Map<String, BuildDTO> results;

    public MailReportDTO(String email, String packageName, String ref) {
        this.to = email;
        this.packageName = packageName;
        this.status = status;
        this.successs = successs;
        this.ref = ref;
    }

    public void addBuilds(String erlang, List<BuildDTO> builds) {
        for (BuildDTO build : builds)
            this.results.put(erlang, build);
    }

    public void calculateSuccess() {
        successs = true;
        for (BuildDTO build : results.values())
            if (!build.isResult()) {
                successs = false;
                break;
            }
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

    public boolean isSuccesss() {
        return successs;
    }

    public Map<String, BuildDTO> getResults() {
        return results;
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
}

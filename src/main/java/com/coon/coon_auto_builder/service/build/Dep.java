package com.coon.coon_auto_builder.service.build;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

import java.util.Map;

@Data
@AllArgsConstructor
public class Dep {
    private String url;
    private String name;
    private String tag;
    private String branch;
    @Wither
    private String erlVsn;

    public Dep(Map<String, String> data) {
        url = data.get("url");
        name = data.get("name");
        tag = data.get("tag");
        branch = data.get("branch");
    }

    public boolean isTagged() {
        return tag != null;
    }
}

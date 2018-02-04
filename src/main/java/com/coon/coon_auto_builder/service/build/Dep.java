package com.coon.coon_auto_builder.service.build;

import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.coon.coon_auto_builder.tool.UrlHelper.removeGitEnding;
import static com.coon.coon_auto_builder.tool.UrlHelper.removeProtocol;

@Data
@Builder
@AllArgsConstructor
public class Dep {
    private String url;
    private String name;
    private String tag;
    private String branch;
    private Set<String> erlVsn;

    public Dep(Map<String, String> data) {
        url = removeProtocol(removeGitEnding(data.get("url")));
        name = data.get("name");
        tag = data.get("tag");
        branch = data.get("branch");
        erlVsn = new HashSet<>();
    }

    public boolean isTagged() {
        return tag != null;
    }

    public Dep withErlVsn(String vsn) {
        this.erlVsn.add(vsn);
        return this;
    }

    public String getFirstErlVsn() {
        return erlVsn.stream().findFirst().orElse(null);
    }

    public RepositoryDTO toRepositoryDTO() {
        List<PackageVersionDTO> versions = erlVsn.stream()
                .map(vsn -> new PackageVersionDTO(tag, vsn))
                .collect(Collectors.toList());
        return RepositoryDTO.builder()
                .fullName(name)
                .cloneUrl(url)
                .versions(versions)
                .notifyEmail(false)
                .build();
    }

    public static void addDep(Map<String, Dep> deps, Dep newDep) {
        Dep got = deps.get(newDep.getUrl());
        if (got == null) {
            deps.put(newDep.getUrl(), newDep);
        } else {
            got.getErlVsn().addAll(newDep.getErlVsn());
        }
    }

    public static void mergeDeps(Map<String, Dep> acc1, Map<String, Dep> acc2) {
        acc2.forEach(
                (e, m) ->
                        acc1.merge(e, m,
                                (v1, v2) ->
                                {
                                    v1.getErlVsn().addAll(v2.getErlVsn());
                                    return v1;
                                }));
    }
}

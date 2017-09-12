package com.coon.coon_auto_builder.domain;

import com.coon.coon_auto_builder.system.Status;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Entity
@Table(name = "build_result")
public class BuildResult {
    @Column(name = "ref", length = 100, nullable = false)
    private String ref;
    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName; // TODO split to name and namespace?
    @Column(name = "state", length = 100, nullable = false)
    private Status state;
    @Column(name = "erlang_vsn", length = 100)
    private String erlangVsn;
    @Column(name = "result", length = 100, nullable = false)
    private boolean result;
    @Column(name = "message", length = 100)  //TODO message lenght can be more than 100
    private String message;
    @Column(name = "url", length = 100, nullable = false)
    private String url;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private ZonedDateTime createdDate = ZonedDateTime.now();

    public BuildResult() {

    }

    public BuildResult(String fullName, String ref, Status state, String erlangVsn,
                       String url, boolean result, String message) {
        this.fullName = fullName;
        this.ref = ref;
        this.state = state;
        this.erlangVsn = erlangVsn;
        this.result = result;
        this.message = message;
        this.url = url;
    }
}

package com.coon.coon_auto_builder.domain;

import com.coon.coon_auto_builder.system.Status;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "build_result")
public class BuildResult {
    @Column(name = "ref", length = 100, nullable = false)
    final private String ref;
    @Column(name = "full_name", length = 100, nullable = false)
    final private String fullName; // TODO split to name and namespace?
    @Column(name = "state", length = 100, nullable = false)
    final private Status state;
    @Column(name = "erlang_vsn", length = 100)
    final private String erlangVsn;
    @Column(name = "result", length = 100, nullable = false)
    final private boolean result;
    @Column(name = "message", length = 100)
    final private String message;
    @Column(name = "url", length = 100, nullable = false)
    final private String url;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(name = "creation_time", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentZonedDateTime")
    @CreatedDate
    private ZonedDateTime creationTime;

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

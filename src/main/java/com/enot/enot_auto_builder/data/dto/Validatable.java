package com.enot.enot_auto_builder.data.dto;

import com.enot.enot_auto_builder.data.dao.RepositoryDAOService;
import com.enot.enot_auto_builder.data.entity.Build;
import com.enot.enot_auto_builder.data.entity.Repository;

import javax.annotation.Nullable;

public interface Validatable {
    void onConflict(Repository found, RepositoryDAOService service) throws Exception;

    RepositoryDTO onRebuild(@Nullable Build found) throws Exception;

    void basicValidation(String secret) throws Exception;

    @Nullable
    String getBuildId();

    @Nullable
    String getFullName();

    @Nullable
    String getCloneUrl();
}

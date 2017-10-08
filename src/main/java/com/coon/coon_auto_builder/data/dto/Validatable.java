package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.Repository;
import org.jetbrains.annotations.Nullable;

public interface Validatable {
    void onConflict(Repository found, RepositoryDAOService service) throws Exception;

    RepositoryDTO onRebuild(@Nullable Build found) throws Exception;

    void basicValidation(String secret) throws Exception;

    @Nullable String getBuildId();

    @Nullable String getFullName();

    @Nullable String getCloneUrl();
}

package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class FraudDetector {

    /**
     * Allow:
     * 1. Request from github if signature presents and correct
     * 2. Rebuild existing build request
     * 3. Build existing repository
     * 4. New build (not from github) if doesn't have namespace/name clash with
     * existing repository.
     * @throws Exception
     */
    public void tryDetect(@Nullable String signature, RepositoryDTO repo) throws Exception {

    }

}

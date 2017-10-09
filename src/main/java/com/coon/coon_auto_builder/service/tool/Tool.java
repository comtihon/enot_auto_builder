package com.coon.coon_auto_builder.service.tool;

import org.jetbrains.annotations.Nullable;

public interface Tool {
    public boolean check();

    public boolean install();

    @Nullable
    public String version();
}

package com.coon.coon_auto_builder.service.tool;

import com.coon.coon_auto_builder.config.ToolsConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.coon.coon_auto_builder.tool.CmdHelper.runCmd;

public class Coon implements Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsConfiguration.class);

    private String coonVersion;

    public Coon() {
    }

    @Override
    public boolean check() {
        try {
            coonVersion = runCmd("coon -v");
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Calling coon error " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean install() {
        //TODO
        return false;
    }

    @Override
    @Nullable
    public String version() {
        return coonVersion;
    }
}

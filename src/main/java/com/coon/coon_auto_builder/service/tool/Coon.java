package com.coon.coon_auto_builder.service.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.coon.coon_auto_builder.tool.CmdHelper.runCmd;

@Component
public class Coon extends Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(Coon.class);


    public Coon() {
    }

    @Override
    public boolean check() {
        try {
            version = runCmd("coon -v").trim();
            ready = true;
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Calling coon error " + e.getMessage());
            message = e.getMessage();
            return false;
        }
    }

    @Override
    public boolean install() {
        //TODO
        return false;
    }
}

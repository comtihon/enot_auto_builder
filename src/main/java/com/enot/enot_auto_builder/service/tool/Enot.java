package com.enot.enot_auto_builder.service.tool;

import com.enot.enot_auto_builder.service.build.BuildException;
import com.enot.enot_auto_builder.tool.CmdHelper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.enot.enot_auto_builder.tool.CmdHelper.runCmd;

@Component
@NoArgsConstructor
@Slf4j
public class Enot extends Tool {

    public void build(Path buildPath, String erlangExecutable) throws BuildException, IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("enot", "package");
        pb.directory(buildPath.toFile());
        Map<String, String> env = pb.environment();
        String path = env.get("PATH");
        env.put("PATH", Paths.get(erlangExecutable, "bin").toString() + ":" + path);
        Process process = pb.start();
        if (process.waitFor() != 0) {
            throw new BuildException(CmdHelper.getProcessError(process));
        }
    }

    @Override
    public boolean check() {
        try {
            version = runCmd("enot -v").trim();
            ready = true;
            return true;
        } catch (IOException | InterruptedException e) {
            log.warn("Calling enot error " + e.getMessage());
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

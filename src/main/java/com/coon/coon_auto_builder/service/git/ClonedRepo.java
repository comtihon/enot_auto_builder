package com.coon.coon_auto_builder.service.git;

import com.coon.coon_auto_builder.service.build.Dep;
import com.coon.coon_auto_builder.tool.FileHelper;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ClonedRepo {
    private final String email;
    private final Path cloned;

    /**
     * Repo belongs to coon if there is coonfig.json in the root.
     *
     * @return true if found
     */
    public boolean isCoon() {
        Optional<File> erts = Arrays.stream(
                Objects.requireNonNull(cloned.toFile().listFiles()))
                .filter(f -> f.getName().equals("coonfig.json"))
                .findFirst();
        return erts.isPresent();
    }

    public List<Dep> getDeps() {
        try {
            return ((List<Map<String, String>>) getConfig().get("deps")).stream()
                    .map(Dep::new)
                    .collect(Collectors.toList());
        } catch (IOException | NullPointerException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Return coonfig.json
     *
     * @return kv config
     */
    public Map<String, Object> getConfig() throws IOException {
        return FileHelper.readConfig(cloned);
    }

    @Override
    public String toString() {
        return "ClonedRepo{" +
                "cloned=" + cloned +
                '}';
    }
}

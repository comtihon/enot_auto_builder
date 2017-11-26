package com.coon.coon_auto_builder.tool;

import com.metadave.etp.ETP;
import com.metadave.etp.rep.ETPTuple;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.file.Path;

public class ErlangHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErlangHelper.class);

    /**
     * Get application name from Erlang configuration file (.app or .app.src)
     * Convert atom to string if needed.
     *
     * @param path path to a file
     * @return application name
     */
    public static String getApplicationName(Path path) {
        try {
            String content = FileUtils.readFileToString(path.toFile(), Charset.forName("UTF-8"));
            return getName(content);
        } catch (Exception e) {
            LOGGER.warn("Can't parse erlang configuration {}, Err {}" + path, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    static String getName(String appConfig) throws ETP.ParseException {
        ETPTuple tuple = (ETPTuple) ETP.parse(appConfig);
        return tuple.getValue(1).toString().replaceAll("\'", "");
    }
}

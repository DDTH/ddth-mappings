package com.github.ddth.mappings.test.cql;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.datastax.driver.core.Session;

public class CqlTestUtils {

    public static void loadAndRunCqlScript(Session session, String scriptInClasspath)
            throws IOException {
        try (InputStream is = CqlTestUtils.class.getResourceAsStream(scriptInClasspath)) {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            String CQL = "";
            for (String line : lines) {
                if (line.startsWith("--") || line.startsWith("#")) {
                    // ignore comment lines
                    continue;
                }
                CQL += line;
                if (line.endsWith(";")) {
                    try {
                        session.execute(CQL);
                    } catch (Exception e) {
                    }
                    CQL = "";
                }
            }
        }
    }

}

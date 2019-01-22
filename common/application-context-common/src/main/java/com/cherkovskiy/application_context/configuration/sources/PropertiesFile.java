package com.cherkovskiy.application_context.configuration.sources;

import com.cherkovskiy.application_context.configuration.utils.CommonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PropertiesFile extends MapPropertySource {

    public PropertiesFile(String name, String filename) {
        super(name, CommonUtils.readFile(() -> {
            try {
                return Files.newInputStream(Paths.get(filename));
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }));
    }
}

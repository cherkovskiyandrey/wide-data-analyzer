package com.cherkovskiy.application_context.configuration.sources;

import com.cherkovskiy.application_context.configuration.sources.MapPropertySource;
import com.cherkovskiy.application_context.configuration.utils.CommonUtils;

import java.io.InputStream;

public class PropertiesFileAsResource extends MapPropertySource {

    public PropertiesFileAsResource(String name, ClassLoader classLoader, String filename) {
        super(name, CommonUtils.readFile(() -> {
            final InputStream resource = classLoader.getResourceAsStream(filename);
            if(resource == null) {
                throw new IllegalArgumentException(filename);
            }
            return resource;
        }));
    }
}

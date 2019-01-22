package com.cherkovskiy.application_context.configuration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

public class CommonUtils {

    public static Map<String, Object> readFile(Supplier<InputStream> streamSupplier) {
        try (InputStream inputStream = streamSupplier.get()) {
            final Map<String, Object> result = new HashMap<>();
            final Properties properties = new Properties();

            properties.load(inputStream);
            properties.forEach((k, v) -> result.put((String) k, v));

            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

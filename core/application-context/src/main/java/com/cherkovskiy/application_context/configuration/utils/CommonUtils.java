package com.cherkovskiy.application_context.configuration.utils;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUtils {

    public static String lazyJoin(String delimiter, String... elements) {
        return Stream.of(elements).filter(s -> !Strings.isNullOrEmpty(s)).collect(Collectors.joining(delimiter));
    }

    public static Object getDefaultValueFor(Class<?> cls) {
        if (!cls.isPrimitive()) {
            return null;
        }
        if (boolean.class.equals(cls)) {
            return false;
        }
        if (byte.class.equals(cls)) {
            return (byte) 0;
        }
        if (char.class.equals(cls)) {
            return "";
        }
        if (short.class.equals(cls)) {
            return (short) 0;
        }
        if (int.class.equals(cls)) {
            return 0;
        }
        if (long.class.equals(cls)) {
            return 0L;
        }
        if (float.class.equals(cls)) {
            return 0.;
        }

        return 0.D;
    }
}

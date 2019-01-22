package com.cherkovskiy.application_context.configuration.sources.impl;

import com.cherkovskiy.application_context.configuration.sources.AbstractPropertySource;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class SystemEnvironmentPropertySource extends AbstractPropertySource {

    public SystemEnvironmentPropertySource(String name) {
        super(name);
    }

    /**
     * Return {@code true} if a property with the given name or any underscore/uppercase variant
     * thereof exists in this property source.
     */
    @Override
    public boolean containsProperty(@Nonnull String name) {
        return (getProperty(name) != null);
    }

    /**
     * This implementation returns {@code true} if a property with the given name or
     * any underscore/uppercase variant thereof exists in this property source.
     */
    @Override
    public Object getProperty(@Nonnull String name) {
        String actualName = resolvePropertyName(name);
        return System.getenv(actualName);
    }

    @Nonnull
    @Override
    public Set<String> getAllPropertiesKeys() {
        return System.getenv().keySet();
    }

    private static String resolvePropertyName(String name) {
        Objects.nonNull(name);
        String resolvedName = checkPropertyName(name);
        if (resolvedName != null) {
            return resolvedName;
        }
        String uppercasedName = name.toUpperCase();
        if (!name.equals(uppercasedName)) {
            resolvedName = checkPropertyName(uppercasedName);
            if (resolvedName != null) {
                return resolvedName;
            }
        }
        return name;
    }

    private static String checkPropertyName(String name) {
        // Check name as-is
        if (containsKey(name)) {
            return name;
        }
        // Check name with just dots replaced
        String noDotName = name.replace('.', '_');
        if (!name.equals(noDotName) && containsKey(noDotName)) {
            return noDotName;
        }
        // Check name with just hyphens replaced
        String noHyphenName = name.replace('-', '_');
        if (!name.equals(noHyphenName) && containsKey(noHyphenName)) {
            return noHyphenName;
        }
        // Check name with dots and hyphens replaced
        String noDotNoHyphenName = noDotName.replace('-', '_');
        if (!noDotName.equals(noDotNoHyphenName) && containsKey(noDotNoHyphenName)) {
            return noDotNoHyphenName;
        }
        // Give up
        return null;
    }

    private static boolean containsKey(String name) {
        return System.getenv().containsKey(name);
    }
}
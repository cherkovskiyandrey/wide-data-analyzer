package com.cherkovskiy.application_context.configuration.environments;

import com.cherkovskiy.application_context.configuration.sources.AbstractPropertySource;
import com.cherkovskiy.application_context.configuration.sources.impl.SystemEnvironmentPropertySource;

import javax.annotation.Nonnull;
import java.util.Set;

public class StandardConfiguration extends AbstractConfiguration {

    /**
     * System CONFIGURATION property source name: {@value}
     */
    public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";

    /**
     * JVM system properties property source name: {@value}
     */
    public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";

    public StandardConfiguration() {
        propertySources.addLast(new AbstractPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME) {
            @Override
            public Object getProperty(@Nonnull String name) {
                return System.getProperty(name);
            }

            @Nonnull
            @Override
            public Set<String> getAllPropertiesKeys() {
                return System.getProperties().stringPropertyNames();
            }
        });
        propertySources.addLast(new SystemEnvironmentPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME));
    }
}
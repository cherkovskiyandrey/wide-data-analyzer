package com.cherkovskiy.application_context.configuration.environments;

import com.cherkovskiy.application_context.configuration.resources.MutableResourcesImpl;
import com.cherkovskiy.application_context.configuration.sources.AbstractPropertySource;
import com.cherkovskiy.application_context.configuration.sources.impl.SystemEnvironmentPropertySource;

import javax.annotation.Nonnull;
import java.util.Set;

public abstract class StandardConfiguration extends ConfigurationImpl {

    /**
     * System CONFIGURATION property source name: {@value}
     */
    public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";

    /**
     * JVM system properties property source name: {@value}
     */
    public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";

    public static ConfigurationImpl create() {
        ConfigurationImpl configuration = new ConfigurationImpl();
        configuration.getGlobalPropertySources().addLast(new AbstractPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME) {
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
        configuration.getGlobalPropertySources().addLast(new SystemEnvironmentPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME));
        return configuration;
    }

    public static ConfigurationImpl createLocalConfiguration(@Nonnull ConfigurationImpl parent) {
        return new ConfigurationImpl(
                parent.getGlobalPropertySources(),
                parent.getGlobalConverterServices(),
                new MutableResourcesImpl<>(),
                new MutableResourcesImpl<>()
        );
    }
}
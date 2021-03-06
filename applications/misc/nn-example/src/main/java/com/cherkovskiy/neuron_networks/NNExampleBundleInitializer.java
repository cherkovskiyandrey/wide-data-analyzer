package com.cherkovskiy.neuron_networks;

import com.cherkovskiy.application_context.api.BundleContext;
import com.cherkovskiy.application_context.api.BundleLifecycle;
import com.cherkovskiy.application_context.api.BundleVersion;
import com.cherkovskiy.application_context.configuration.sources.MapPropertySource;
import com.cherkovskiy.application_context.configuration.sources.PropertiesFile;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.stream.StreamSupport;


public class NNExampleBundleInitializer implements BundleLifecycle {

    /**
     * example how to add bundle custom properties or overload current
     *
     * @param bundleNameVersion
     */
    @Override
    public void beforeInit(@Nonnull BundleVersion bundleNameVersion, @Nonnull BundleContext bundleContext) {
        //Set global properties.
        boolean hasLogDir = StreamSupport.stream(bundleContext.getConfigurableConfiguration().getGlobalPropertySources().spliterator(), false)
                .anyMatch(propertiesSource -> propertiesSource.containsProperty("global.log_dir"));

        if (!hasLogDir) {
            bundleContext.getConfigurableConfiguration().getGlobalPropertySources().addFirst(new MapPropertySource("Overloaded", ImmutableMap.of(
                    "global.log_dir",
                    new File(".").getAbsolutePath()
            )));
        }

        //or: set local properties
        bundleContext.getConfigurableConfiguration().getPropertySources().addLast(
                new PropertiesFile("NNProperties", "neuralNetwork.properties")
        );
    }
    //TODO: ConfigurationContextProxy::refresh will be invoked
}

package com.cherkovskiy.neuron_networks;

import com.cherkovskiy.application_context.api.BundleLifecycle;
import com.cherkovskiy.application_context.api.BundleVersion;

import javax.annotation.Nonnull;


//TODO: реализовать пусть лучше будет
public class NNExampleBundleInitializer implements BundleLifecycle {

    @Override
    public void beforeInit(@Nonnull BundleVersion bundleNameVersion) {

        //TODO: example of rewriting of properties on start
//        //---------------------------------
//        final ImmutableMap.Builder<String, Object> overridedProperties = new ImmutableMap.Builder<>();
//        if(Objects.isNull(configurationExample.getLogDir().getValue())) {
//            overridedProperties.put(configurationExample.getLogDir().getPath(), new File(".").getAbsolutePath());
//        }
//        final ConfigurableConfiguration configuration = (ConfigurableConfiguration) ApplicationContextHolder.currentContext().getConfigurationContext().source();
//        configuration.getPropertySources().addFirst(new MapPropertySource("Overloaded", overridedProperties.build()));
//        ApplicationContextHolder.currentContext().getConfigurationContext().refresh();
//        //---------------------------------
    }
}

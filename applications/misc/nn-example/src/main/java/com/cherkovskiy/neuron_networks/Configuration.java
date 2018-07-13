package com.cherkovskiy.neuron_networks;

import com.cherkovskiy.application_context.api.configuration.ConfigurationItem;
import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationValue;

import java.io.File;


public interface Configuration {

    @ConfigurationValue(value = "global.log_dir", defaultValue = "log")
    ConfigurationItem<File> getLogDir();

}

package com.cherkovskiy.neuron_networks;

import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationProperties;
import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationValue;
import com.cherkovskiy.application_context.configuration.FileConverter;

import java.io.File;


@ConfigurationProperties
public class Configuration {

    @ConfigurationValue(value = "global.log_dir", defaultValue = "log", converter = FileConverter.class)
    private File logDir;


    public File getLogDir() {
        return logDir;
    }
}

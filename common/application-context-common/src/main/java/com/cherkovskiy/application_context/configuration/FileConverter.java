package com.cherkovskiy.application_context.configuration;

import com.cherkovskiy.application_context.api.configuration.convertors.Converter;

import java.io.File;

public class FileConverter implements Converter<String, File> {
    @Override
    public File convert(String s) {
        return new File(s);
    }
}

package com.cherkovskiy.application_context.configuration.mappers;

import com.cherkovskiy.application_context.configuration.ConfigurationInternal;


public interface ObjectMapper {

    <T> T readValue(Class<T> token, ConfigurationInternal configuration);
}

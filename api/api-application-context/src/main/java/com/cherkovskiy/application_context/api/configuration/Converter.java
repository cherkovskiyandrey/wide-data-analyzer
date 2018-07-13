package com.cherkovskiy.application_context.api.configuration;

@FunctionalInterface
public interface Converter<From, To> {

    To convert(From from) throws Exception;
}

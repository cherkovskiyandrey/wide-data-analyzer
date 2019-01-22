package com.cherkovskiy.application_context.api.configuration.convertors;

@FunctionalInterface
public interface Converter<From, To> {

    To convert(From from) throws Exception;
}

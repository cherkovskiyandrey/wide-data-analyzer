package com.cherkovskiy.application_context.api.configuration.convertors;

import com.cherkovskiy.application_context.api.configuration.resources.NamedResource;

public interface ConverterService extends NamedResource {

    <From, To> boolean canConvert(Class<From> from, Class<To> toCls);

    <From, To> To convert(From fromCls, Class<To> toCls) throws IllegalStateException;

}

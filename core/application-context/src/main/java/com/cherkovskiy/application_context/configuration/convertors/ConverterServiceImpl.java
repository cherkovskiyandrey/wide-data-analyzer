package com.cherkovskiy.application_context.configuration.convertors;

import com.cherkovskiy.application_context.api.configuration.convertors.Converter;
import com.cherkovskiy.application_context.api.configuration.convertors.ConverterService;
import com.google.common.collect.ImmutableTable;

import static java.lang.String.format;

public class ConverterServiceImpl implements ConverterService {
    private final String name;
    private final ImmutableTable<Class, Class, Converter> converters;

    public ConverterServiceImpl(String name, ImmutableTable<Class, Class, Converter> converters) {
        this.name = name;
        this.converters = converters;
    }

    @Override
    public <From, To> boolean canConvert(Class<From> fromCls, Class<To> toCls) {
        return toCls.isAssignableFrom(fromCls) ||
                converters.get(fromCls, toCls) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <From, To> To convert(From from, Class<To> toCls) throws IllegalStateException {
        final Class<From> fromCls = (Class<From>) from.getClass();
        if (toCls.isAssignableFrom(fromCls)) {
            return (To) from;
        }

        final Converter<From, To> converter = converters.get(fromCls, toCls);
        if (converter == null) {
            throw new IllegalStateException(format("Can`t convert from %s to %s", fromCls.getSimpleName(), toCls.getSimpleName()));
        }

        try {
            return converter.convert(from);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }
}

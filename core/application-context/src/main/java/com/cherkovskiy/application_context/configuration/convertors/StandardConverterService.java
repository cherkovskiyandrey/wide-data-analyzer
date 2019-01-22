package com.cherkovskiy.application_context.configuration.convertors;

import com.cherkovskiy.application_context.api.configuration.convertors.Converter;
import com.google.common.collect.ImmutableTable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;


public final class StandardConverterService extends ConverterServiceImpl {

    private static <To> Converter<String, To> stringPrepareHelper(Converter<String, To> func) {
        return str -> func.convert(str.trim());
    }

    private final static String NAME = StandardConverterService.class.getName();

    /**
     * Row - from type, Column - to type.
     */
    private final static ImmutableTable<Class, Class, Converter> CONVERTERS = new ImmutableTable.Builder<Class, Class, Converter>()

            //bool
            .put(String.class, boolean.class, stringPrepareHelper(Boolean::parseBoolean))
            .put(String.class, Boolean.class, stringPrepareHelper(Boolean::parseBoolean))

            //byte
            .put(String.class, byte.class, stringPrepareHelper(Byte::parseByte))
            .put(String.class, Byte.class, stringPrepareHelper(Byte::parseByte))

            //short
            .put(String.class, short.class, stringPrepareHelper(Short::parseShort))
            .put(String.class, Short.class, stringPrepareHelper(Short::parseShort))

            //int
            .put(String.class, int.class, stringPrepareHelper(Integer::parseInt))
            .put(String.class, Integer.class, stringPrepareHelper(Integer::parseInt))

            //long
            .put(String.class, long.class, stringPrepareHelper(Long::parseLong))
            .put(String.class, Long.class, stringPrepareHelper(Long::parseLong))

            //float
            .put(String.class, float.class, stringPrepareHelper(Float::parseFloat))
            .put(String.class, Float.class, stringPrepareHelper(Float::parseFloat))

            //double
            .put(String.class, double.class, stringPrepareHelper(Double::parseDouble))
            .put(String.class, Double.class, stringPrepareHelper(Double::parseDouble))


            .put(String.class, BigInteger.class, stringPrepareHelper(BigInteger::new))
            .put(String.class, BigDecimal.class, stringPrepareHelper(BigDecimal::new))

            //InetAddress
            .put(String.class, InetAddress.class, stringPrepareHelper(InetAddress::getByName))

            //TODO: expand if necessary for basic types

            .build();

    public StandardConverterService() {
        super(NAME, CONVERTERS);
    }
}

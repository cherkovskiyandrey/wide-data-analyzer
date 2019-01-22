package com.cherkovskiy.application_context.configuration.mappers.simple.restrictions;

import com.cherkovskiy.application_context.configuration.mappers.simple.ObjectElement;
import com.google.common.collect.ImmutableTable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

public class RestrictProvider<T> {

    private final static ImmutableTable<Class<? extends Annotation>, Class<?>, RestrictFunction> SUPPORTED_RESTRICTIONS =
            new ImmutableTable.Builder<Class<? extends Annotation>, Class<?>, RestrictFunction>()

                    //---------------------------------Min----------------------------
                    //Byte
                    .put(Min.class, Byte.class, RestrictFunction.<Min, Byte>wrap((a, i) -> Long.valueOf(Math.max(i, a.value())).byteValue()))

                    //Short
                    .put(Min.class, Short.class, RestrictFunction.<Min, Short>wrap((a, i) -> Long.valueOf(Math.max(i, a.value())).shortValue()))

                    //Integer
                    .put(Min.class, Integer.class, RestrictFunction.<Min, Integer>wrap((a, i) -> Math.toIntExact(Math.max(i, a.value()))))

                    //Long
                    .put(Min.class, Long.class, RestrictFunction.<Min, Long>wrap((a, i) -> Math.max(i, a.value())))

                    //BigDecimal & BigInteger
                    .put(Min.class, BigDecimal.class, RestrictFunction.<Min, BigDecimal>wrap((a, i) -> i.max(BigDecimal.valueOf(a.value()))))
                    .put(Min.class, BigInteger.class, RestrictFunction.<Min, BigInteger>wrap((a, i) -> i.max(BigInteger.valueOf(a.value()))))

                    //---------------------------------Max----------------------------
                    //Byte
                    .put(Max.class, Byte.class, RestrictFunction.<Max, Byte>wrap((a, i) -> Long.valueOf(Math.min(i, a.value())).byteValue()))

                    //Short
                    .put(Max.class, Short.class, RestrictFunction.<Max, Short>wrap((a, i) -> Long.valueOf(Math.min(i, a.value())).shortValue()))

                    //Integer
                    .put(Max.class, Integer.class, RestrictFunction.<Max, Integer>wrap((a, i) -> Math.toIntExact(Math.min(i, a.value()))))

                    //Long
                    .put(Max.class, Long.class, RestrictFunction.<Max, Long>wrap((a, i) -> Math.min(i, a.value())))

                    //BigDecimal & BigInteger
                    .put(Max.class, BigDecimal.class, RestrictFunction.<Max, BigDecimal>wrap((a, i) -> i.min(BigDecimal.valueOf(a.value()))))
                    .put(Max.class, BigInteger.class, RestrictFunction.<Max, BigInteger>wrap((a, i) -> i.min(BigInteger.valueOf(a.value()))))

                    .build();

    private final Function<T, T> handlerChain;

    @SuppressWarnings("unchecked")
    public RestrictProvider(ObjectElement<?> objectElement, Class<T> classToken) {
        Function<T, T> handlerChain = Function.identity();
        for (Map.Entry<Class<? extends Annotation>, RestrictFunction> entry : SUPPORTED_RESTRICTIONS.column(classToken).entrySet()) {
            final Annotation annotation = objectElement.getAnnotation(entry.getKey());
            if (annotation != null) {
                handlerChain = handlerChain.andThen(o -> classToken.cast(((RestrictFunction<Annotation, T>) (entry.getValue())).apply(annotation, o)));
            }
        }
        this.handlerChain = handlerChain;
    }

    public T applyAllTo(T fieldValue) {
        return handlerChain.apply(fieldValue);
    }
}

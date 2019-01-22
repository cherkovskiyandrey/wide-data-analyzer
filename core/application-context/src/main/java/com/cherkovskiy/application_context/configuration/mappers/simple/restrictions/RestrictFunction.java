package com.cherkovskiy.application_context.configuration.mappers.simple.restrictions;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface RestrictFunction<A extends Annotation, U> {

    U apply(A annotation, U object);

    static <A extends Annotation, U> RestrictFunction<A, U> wrap(RestrictFunction<A, U> function) {
        return function;
    }
}

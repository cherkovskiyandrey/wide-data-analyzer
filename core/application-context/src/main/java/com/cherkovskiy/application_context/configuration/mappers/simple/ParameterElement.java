package com.cherkovskiy.application_context.configuration.mappers.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

class ParameterElement<T> implements ObjectElement<T> {
    private final Map.Entry<Parameter, Object> parameter;

    ParameterElement(Map.Entry<Parameter, Object> parameter) {
        this.parameter = parameter;
    }

    @Override
    public <U extends Annotation> U getAnnotation(Class<U> annotationType) {
        return parameter.getKey().getAnnotation(annotationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) parameter.getKey().getType();
    }

    @Override
    public Optional<ParameterizedType> getAsParameterizedType() {
        return parameter.getKey().getParameterizedType() instanceof ParameterizedType ?
                Optional.of((ParameterizedType) parameter.getKey().getParameterizedType()) :
                Optional.empty();
    }

    @Override
    public void setValue(T value) {
        parameter.setValue(value);
    }
}

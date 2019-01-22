package com.cherkovskiy.application_context.configuration.mappers.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

class FieldElement<T> implements ObjectElement<T> {
    private final Map.Entry<Field, Object> field;

    FieldElement(Map.Entry<Field, Object> field) {
        this.field = field;
    }

    @Override
    public <U extends Annotation> U getAnnotation(Class<U> annotationType) {
        return field.getKey().getAnnotation(annotationType);
    }

    @Override
    public Optional<ParameterizedType> getAsParameterizedType() {
        return field.getKey().getGenericType() instanceof ParameterizedType ?
                Optional.of((ParameterizedType) field.getKey().getGenericType()) :
                Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) field.getKey().getType();
    }

    @Override
    public void setValue(T value) {
        field.setValue(value);
    }
}

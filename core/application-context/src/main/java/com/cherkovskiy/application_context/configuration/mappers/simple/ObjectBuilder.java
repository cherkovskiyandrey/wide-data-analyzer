package com.cherkovskiy.application_context.configuration.mappers.simple;

import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationValue;
import com.cherkovskiy.application_context.configuration.utils.CommonUtils;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

import static java.lang.String.format;

class ObjectBuilder<T> implements Iterable<ObjectElement<?>> {
    private final Class<T> token;
    private final Constructor<T> constructor;
    private final Map<Parameter, Object> constructorArguments;
    private final Map<Field, Object> fieldsValues;

    private ObjectBuilder(Class<T> token) {
        this.token = token;
        this.constructor = lookUpSuitableConstructor(token);
        this.constructorArguments = constructor == null ? Collections.emptyMap() : Arrays.stream(constructor.getParameters()).collect(
                Maps::newLinkedHashMap,
                (map, param) -> map.put(param, CommonUtils.getDefaultValueFor(param.getType())),
                Map::putAll
        );
        this.fieldsValues = Arrays.stream(token.getDeclaredFields()).collect(
                Maps::newLinkedHashMap,
                (map, field) -> map.put(field, null),
                Map::putAll
        );
    }

    private static <T> Constructor<T> lookUpSuitableConstructor(Class<T> token) {
        @SuppressWarnings("unchecked") final Constructor<T>[] constructors = (Constructor<T>[]) token.getDeclaredConstructors();
        return Arrays.stream(constructors)
                .filter(c -> c.getParameters().length > 0)
                .max(Comparator.comparingLong(c ->
                        Arrays.stream(c.getParameters())
                                .filter(param -> param.isAnnotationPresent(ConfigurationValue.class))
                                .count()
                ))
                .orElse(null);
    }

    static <T> ObjectBuilder<T> of(Class<T> token) {
        return new ObjectBuilder<>(token);
    }

    @Override
    public Iterator<ObjectElement<?>> iterator() {
        final Iterator<Map.Entry<Parameter, Object>> constructorArgumentsItr = constructorArguments.entrySet().iterator();
        final Iterator<Map.Entry<Field, Object>> fieldsValuesItr = fieldsValues.entrySet().iterator();

        return new Iterator<ObjectElement<?>>() {
            @Override
            public boolean hasNext() {
                return constructorArgumentsItr.hasNext() || fieldsValuesItr.hasNext();
            }

            @Override
            public ObjectElement<?> next() {
                if (constructorArgumentsItr.hasNext()) {
                    return new ParameterElement<>(constructorArgumentsItr.next());
                }
                if (fieldsValuesItr.hasNext()) {
                    return new FieldElement<>(fieldsValuesItr.next());
                }
                throw new NoSuchElementException();
            }
        };
    }

    T build() {
        final T resultObject;
        try {
            resultObject = constructor != null ?
                    constructor.newInstance(constructorArguments.values().toArray()) :
                    token.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(format("Could not create object with class %s.", token.getSimpleName()), e);
        }

        fieldsValues.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .forEach(entry -> {
                    try (AccessHolder accessHolder = new AccessHolder(entry.getKey())) {
                        entry.getKey().set(resultObject, entry.getValue());
                    } catch (Exception e) {
                        throw new IllegalStateException(format("Could not init field %s for class %s", entry.getKey().getName(), token.getSimpleName()), e);
                    }
                });

        return resultObject;
    }

    T buildIfNotEmpty() {
        if (constructorArguments.values().stream().anyMatch(Objects::nonNull) ||
                fieldsValues.values().stream().anyMatch(Objects::nonNull)) {
            return build();
        }
        return null;
    }
}

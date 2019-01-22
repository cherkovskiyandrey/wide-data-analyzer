package com.cherkovskiy.application_context.configuration.mappers.simple;

import com.cherkovskiy.application_context.api.configuration.Configuration;
import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationProperties;
import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationValue;
import com.cherkovskiy.application_context.api.configuration.annotations.NestedConfigurationProperties;
import com.cherkovskiy.application_context.api.configuration.convertors.Converter;
import com.cherkovskiy.application_context.api.configuration.convertors.EmptyConverter;
import com.cherkovskiy.application_context.configuration.ConfigurationInternal;
import com.cherkovskiy.application_context.configuration.mappers.ObjectMapper;
import com.cherkovskiy.application_context.configuration.mappers.simple.restrictions.RestrictProvider;
import com.cherkovskiy.application_context.configuration.utils.CollectionBuilder;
import com.cherkovskiy.application_context.configuration.utils.CommonUtils;
import com.google.common.base.Strings;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;


public class SimpleObjectMapper implements ObjectMapper {

    @Override
    public <T> T readValue(Class<T> token, ConfigurationInternal configuration) {
        Objects.requireNonNull(token);
        Objects.requireNonNull(configuration);

        final ConfigurationProperties configurationProperties = token.getAnnotation(ConfigurationProperties.class);
        if (configurationProperties == null) {
            throw new IllegalStateException(format("Class %s does not have ConfigurationProperties annotation.", token.getSimpleName()));
        }

        final String prefix = configurationProperties.value();
        return readValueWithPrefix(token, prefix, configuration).build();
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectBuilder<T> readValueWithPrefix(Class<T> token, String prefix, ConfigurationInternal configuration) {
        final ObjectBuilder<T> objectBuilder = ObjectBuilder.of(token);

        for (ObjectElement<?> objectElement : objectBuilder) {
            final ConfigurationValue configurationValue = objectElement.getAnnotation(ConfigurationValue.class);

            if (configurationValue != null) {
                if (isArray(objectElement)) {
                    final String fieldKey = CommonUtils.lazyJoin(".", prefix, configurationValue.value());
                    throw new IllegalStateException(format("Configuration framework does not support arrays: %s -> %s", fieldKey, objectElement.getType().getName()));

                } else if (isCollection(objectElement)) {
                    initCollection(objectElement, prefix, configurationValue, configuration);

                } else {
                    initSimpleValue(objectElement, prefix, configurationValue, configuration);
                }
            }
        }
        return objectBuilder;
    }

    private boolean isArray(ObjectElement<?> objectElement) {
        return objectElement.getType().isArray();
    }

    private boolean isCollection(ObjectElement<?> objectElement) {
        return objectElement.getAsParameterizedType().isPresent() && Collection.class.isAssignableFrom(objectElement.getType());
    }

    private <T> Converter<Object, T> createConverterInstance(Class<? extends Converter<Object, T>> specConvCls) throws IllegalAccessException, InstantiationException {
        return specConvCls.newInstance();
    }

    private <T> T convertAs(String fieldKey, Class<T> fieldType, Object rawValue, Class<? extends Converter<Object, T>> specConvCls) {
        T result = null;
        if (rawValue != null) {
            try {
                final Converter<Object, T> converter = createConverterInstance(specConvCls);
                result = converter.convert(rawValue);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            if (!result.getClass().equals(fieldType)) {
                throw new IllegalStateException(format("Incompatible types for property %s: filed/param type is %s, but converter return type %s",
                        fieldKey, fieldType, result.getClass()));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> void initSimpleValue(ObjectElement<T> objectElement, String prefix, ConfigurationValue configurationValue, ConfigurationInternal configuration) {
        final String fieldKey = CommonUtils.lazyJoin(".", prefix, configurationValue.value());
        final boolean isRequired = configurationValue.required();
        final String defaultValue = configurationValue.defaultValue();
        final Class<T> fieldType = objectElement.getType();
        final boolean isEnclosed = fieldType.getAnnotation(NestedConfigurationProperties.class) != null;
        final Class<? extends Converter<Object, T>> specConvCls = (Class<? extends Converter<Object, T>>) configurationValue.converter();

        if (isEnclosed && !EmptyConverter.class.equals(specConvCls)) {
            throw new IllegalStateException(format("Property: %s. Custom converter can`t be set for nested configurable classes.", fieldKey));
        }

        final T fieldValue;
        if (isRequired) {
            if (isEnclosed) {
                fieldValue = readValueWithPrefix(fieldType, fieldKey, configuration).buildIfNotEmpty();
                if (fieldValue == null) {
                    throw new IllegalStateException(format("Could not find mandatory property %s", fieldKey));
                }
            } else {
                if (EmptyConverter.class.equals(specConvCls)) {
                    fieldValue = configuration.getRequiredProperty(fieldKey, fieldType);
                } else {
                    final Object rawValue = configuration.getRawRequiredProperty(fieldKey);
                    fieldValue = convertAs(fieldKey, fieldType, rawValue, specConvCls);
                }
            }
        } else {
            if (isEnclosed) {
                fieldValue = readValueWithPrefix(fieldType, fieldKey, configuration).buildIfNotEmpty();
            } else {
                if (EmptyConverter.class.equals(specConvCls)) {
                    fieldValue = configuration.getPropertyWithRawDefault(fieldKey, fieldType, Strings.isNullOrEmpty(defaultValue) ? null : defaultValue);
                } else {
                    final Object rawValue = configuration.getRawProperty(fieldKey, Strings.isNullOrEmpty(defaultValue) ? null : defaultValue);
                    fieldValue = convertAs(fieldKey, fieldType, rawValue, specConvCls);
                }
            }
        }

        if (fieldValue != null) {
            objectElement.setValue(applyRestrictions(objectElement, fieldValue));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T applyRestrictions(ObjectElement<?> objectElement, T fieldValue) {
        final RestrictProvider<T> restrictProvider = new RestrictProvider<>(objectElement, (Class<T>) fieldValue.getClass());
        return restrictProvider.applyAllTo(fieldValue);
    }


    @SuppressWarnings("unchecked")
    private <T extends Collection<U>, U> void initCollection(ObjectElement<?> objectElementArg, String prefix, ConfigurationValue configurationValue, ConfigurationInternal configuration) {
        final ObjectElement<T> objectElement = (ObjectElement<T>) objectElementArg;
        final T collection = CollectionBuilder.createBy(objectElement.getType());
        final boolean isRequired = configurationValue.required();
        final Class<U> collectionElementType = (Class<U>) objectElement.getAsParameterizedType().get().getActualTypeArguments()[0];
        final String fieldKeyBase = CommonUtils.lazyJoin(".", prefix, configurationValue.value());
        final boolean isEnclosed = collectionElementType.getAnnotation(NestedConfigurationProperties.class) != null;
        final Set<String> allIndexes = configuration.getIndexesByPrefix(fieldKeyBase);
        final Class<? extends Converter<Object, U>> specConvCls = (Class<? extends Converter<Object, U>>) configurationValue.converter();

        if (isEnclosed && !EmptyConverter.class.equals(specConvCls)) {
            throw new IllegalStateException(format("Collection property: %s. Custom converter can`t be set for nested configurable classes.", fieldKeyBase));
        }

        if (isEnclosed) {
            allIndexes.forEach(idx -> {
                final String fieldKey = fieldKeyBase + "[" + idx + "]";
                Optional.ofNullable(readValueWithPrefix(collectionElementType, fieldKey, configuration).buildIfNotEmpty())
                        .map(u -> applyRestrictions(objectElement, u))
                        .ifPresent(collection::add);
            });

        } else if (!EmptyConverter.class.equals(specConvCls)) {
            allIndexes.forEach(idx -> {
                final String fieldKey = fieldKeyBase + "[" + idx + "]";
                Optional.ofNullable(configuration.getRawProperty(fieldKey, null))
                        .map(rawValue -> convertAs(fieldKey, collectionElementType, rawValue, specConvCls))
                        .map(u -> applyRestrictions(objectElement, u))
                        .ifPresent(collection::add);
            });

        } else {
            allIndexes.forEach(idx -> {
                final String fieldKey = fieldKeyBase + "[" + idx + "]";
                Optional.ofNullable(configuration.getProperty(fieldKey, collectionElementType))
                        .map(u -> applyRestrictions(objectElement, u))
                        .ifPresent(collection::add);
            });
        }

        if (collection.isEmpty() && isRequired) {
            throw new IllegalStateException(format("Could not find mandatory properties list %s", fieldKeyBase));
        }
        objectElement.setValue(collection);
    }
}

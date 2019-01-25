package com.cherkovskiy.application_context.configuration;

import com.cherkovskiy.application_context.api.configuration.convertors.ConverterService;
import com.cherkovskiy.application_context.api.configuration.sources.PropertiesSource;
import com.cherkovskiy.application_context.configuration.mappers.ObjectMapper;
import com.cherkovskiy.application_context.configuration.sources.AbstractPropertySource;
import com.cherkovskiy.application_context.configuration.utils.CommonUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;


public class PropertySourcesPropertyResolver implements ConfigurationInternal, ObjectMappingConfiguration {
    private final static Pattern pattern = Pattern.compile("^\\[([^\\[\\]]+)].*$");
    private final Iterable<PropertiesSource<?>> globalPropertySources;
    private final Iterable<ConverterService> globalConverterService;
    private final Iterable<PropertiesSource<?>> propertySources;
    private final Iterable<ConverterService> converterService;
    private final ObjectMapper objectMapper;


    /**
     * Create a new resolver against the given property sources.
     *
     * @param globalPropertySources  the set of {@link AbstractPropertySource} objects to use
     * @param globalConverterService
     * @param objectMapper
     */
    public PropertySourcesPropertyResolver(@Nonnull Iterable<PropertiesSource<?>> globalPropertySources,
                                           @Nonnull Iterable<ConverterService> globalConverterService,
                                           @Nonnull Iterable<PropertiesSource<?>> propertySources,
                                           @Nonnull Iterable<ConverterService> converterService,
                                           @Nonnull ObjectMapper objectMapper) {
        this.globalPropertySources = globalPropertySources;
        this.globalConverterService = globalConverterService;
        this.propertySources = propertySources;
        this.converterService = converterService;
        this.objectMapper = objectMapper;
    }


    @Override
    public boolean containsProperty(@Nonnull String key) {
        for (PropertiesSource<?> propertySource : this.globalPropertySources) {
            if (propertySource.containsProperty(key)) {
                return true;
            }
        }
        return false;
    }

    public String getProperty(@Nonnull String key) {
        return getProperty(key, String.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    @Override
    public String getProperty(@Nonnull String key, @Nonnull String defaultValue) {
        return containsProperty(key) ? getProperty(key) : defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetValueType) {
        return propertySourcesAsStream()
                .filter(p -> p.containsProperty(key))
                .map(p -> p.getProperty(key))
                .map(o -> tryConvert(o, targetValueType))
                .findFirst()
                .orElse((T) CommonUtils.getDefaultValueFor(targetValueType))
                ;
    }

    private <From, To> To tryConvert(From from, Class<To> targetValueType) {
        return converterServicesAsStream()
                .filter(conv -> conv.canConvert(from.getClass(), targetValueType))
                .map(conv -> conv.convert(from, targetValueType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(format("Can`t convert from %s to %s", from.getClass().getSimpleName(), targetValueType.getSimpleName())))
                ;
    }


    @SuppressWarnings("ConstantConditions")
    @Nonnull
    @Override
    public <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType, @Nonnull T defaultValue) {
        return containsProperty(key) ? getProperty(key, targetType) : defaultValue;
    }

    @Nonnull
    @Override
    public String getRequiredProperty(@Nonnull String key) throws IllegalStateException {
        final String prop = getProperty(key);
        if (prop == null) {
            throw new IllegalStateException(key);
        }
        return prop;
    }

    @Nonnull
    @Override
    public <T> T getRequiredProperty(@Nonnull String key, @Nonnull Class<T> targetType) throws IllegalStateException {
        final T prop = getProperty(key, targetType);
        if (prop == null) {
            throw new IllegalStateException(format("Could not find mandatory property %s", key));
        }
        return prop;
    }

    @Override
    public Set<String> getIndexesByPrefix(String fieldKeyBase) {
        return propertySourcesAsStream()
                .map(PropertiesSource::getAllPropertiesKeys)
                .map(l -> getIndexesByPrefixHelper(l, fieldKeyBase))
                .filter(l -> !l.isEmpty())
                .findFirst()
                .orElse(Collections.emptySet());
    }

    private static Set<String> getIndexesByPrefixHelper(Set<String> source, String fieldKeyBase) {
        return source.stream()
                .filter(el -> el.startsWith(fieldKeyBase))
                .map(el -> el.substring(fieldKeyBase.length()))
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.replaceFirst("$1"))
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getPropertyWithRawDefault(String key, Class<T> targetType, Object defaultValue) {
        return propertySourcesAsStream()
                .filter(p -> p.containsProperty(key))
                .map(p -> p.getProperty(key))
                .map(o -> tryConvert(o, targetType))
                .findFirst()
                .orElseGet(() -> defaultValue != null ? tryConvert(defaultValue, targetType) : null);
    }

    @Override
    public Object getRawProperty(String key, String defaultValue) {
        return propertySourcesAsStream()
                .filter(p -> p.containsProperty(key))
                .map(p -> p.getProperty(key))
                .findFirst()
                .orElse(defaultValue);
    }

    @Override
    public Object getRawRequiredProperty(String key) throws IllegalStateException {
        return propertySourcesAsStream()
                .filter(p -> p.containsProperty(key))
                .map(p -> p.getProperty(key))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(format("Could not find mandatory property %s", key)))
                ;
    }

    //Local properties have high priority
    private Stream<PropertiesSource<?>> propertySourcesAsStream() {
        return Stream.concat(
                StreamSupport.stream(propertySources.spliterator(), false),
                StreamSupport.stream(globalPropertySources.spliterator(), false)
        );
    }

    //Local converters have high priority
    private Stream<ConverterService> converterServicesAsStream() {
        return Stream.concat(
                StreamSupport.stream(converterService.spliterator(), false),
                StreamSupport.stream(globalConverterService.spliterator(), false)
        );
    }

    @Override
    public <T> T resolvePropertyClass(Class<T> token) throws IllegalStateException {
        return objectMapper.readValue(token, this);
    }

}
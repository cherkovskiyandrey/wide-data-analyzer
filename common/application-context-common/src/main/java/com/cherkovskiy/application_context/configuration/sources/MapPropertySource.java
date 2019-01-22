package com.cherkovskiy.application_context.configuration.sources;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class MapPropertySource extends AbstractPropertySource<Map<String, Object>> {

    public MapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getProperty(@Nonnull String name) {
        return this.source.get(name);
    }

    @Nonnull
    @Override
    public Set<String> getAllPropertiesKeys() {
        return this.source.keySet();
    }

    @Override
    public boolean containsProperty(@Nonnull String name) {
        return this.source.containsKey(name);
    }
}
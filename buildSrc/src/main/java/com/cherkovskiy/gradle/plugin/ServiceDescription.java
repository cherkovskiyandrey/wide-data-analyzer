package com.cherkovskiy.gradle.plugin;

import java.util.List;
import java.util.stream.Collectors;

//TODO: parameters from Service annotation
public class ServiceDescription {
    private final Class<?> service;
    private final List<Class<?>> implInterfaces;

    public ServiceDescription(Class<?> service, List<Class<?>> implInterfaces) {
        this.service = service;
        this.implInterfaces = implInterfaces;
    }

    public List<String> serviceToInterface() {
        return implInterfaces.stream().map(i -> String.format("%s->%s", i.getName(), service.getName())).collect(Collectors.toList());
    }
}

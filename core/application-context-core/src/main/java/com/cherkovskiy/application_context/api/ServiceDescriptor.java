package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.annotations.Service;

import java.util.Map;

public interface ServiceDescriptor {

    enum AccessType {
        PUBLIC,
        PRIVATE,
    }

    String getServiceClass();

    String getServiceName();

    Service.LifecycleType getLifecycleType();

    Service.InitType getInitType();

    Map<String, AccessType> getInterfaces();
}

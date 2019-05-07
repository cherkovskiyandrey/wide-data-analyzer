package com.cherkovskiy.application_context.api.bundles;

import com.cherkovskiy.application_context.api.annotations.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface ServiceDescriptor {

    enum AccessType {
        PUBLIC,
        PRIVATE,
    }

    @Nonnull
    String getServiceClass();

    @Nullable
    String getServiceName();

    @Nonnull
    Service.LifecycleType getLifecycleType();

    @Nonnull
    Service.InitType getInitType();

    @Nonnull
    Map<String, AccessType> getInterfaces();
}

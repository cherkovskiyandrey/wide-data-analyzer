package com.cherkovskiy.application_context.api;

import javax.annotation.Nullable;
import java.util.Set;

public interface Bundle {

    /**
     * Load bundle
     */
    void load();

    /**
     * Unload
     */
    void unload();

    /**
     * @return true for remote bundles and false for local
     */
    boolean isRemote();

    /**
     * @return implemented services in this bundle if it is local bundle and null for remote
     */
    @Nullable
    Set<ServiceDescriptor> getServices();

}

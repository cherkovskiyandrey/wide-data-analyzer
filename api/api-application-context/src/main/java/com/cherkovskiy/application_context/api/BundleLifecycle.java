package com.cherkovskiy.application_context.api;

import javax.annotation.Nonnull;

public interface BundleLifecycle {

    /**
     * Run before any services from bundle will be created.
     *
     * @param bundleNameVersion
     */
    default void beforeInit(@Nonnull BundleVersion bundleNameVersion) {
    }

    /**
     * Run after all services in bundle are created.
     *
     * @param bundleNameVersion
     */
    default void afterInit(@Nonnull BundleVersion bundleNameVersion) {
    }

    /**
     * Run before bundle is destroy and unload.
     *
     * @param bundleNameVersion
     */
    default void beforeDestroy(@Nonnull BundleVersion bundleNameVersion) {
    }
}

package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.annotations.Service;

public interface ServiceLifecycle {

    /**
     * Invoke after constructor.
     * Implement extra beforeInit logic here.
     * And run any handler loops in other threads.
     *
     * @throws Exception
     */
    default void postConstruct() throws Exception {
    }


    /**
     * Invoke before destroy object (only for {@link Service.LifecycleType#SINGLETON})
     * Release any resources, stop any threads.
     *
     * @throws Exception
     */
    default void preDestroy() throws Exception {
    }


    /**
     * Is invoked if any depended services changed/reloaded or refreshed.
     * For example: bundle with depended services is reloaded.
     *
     * @throws Exception
     */
    default void refresh() throws Exception {
    }
}

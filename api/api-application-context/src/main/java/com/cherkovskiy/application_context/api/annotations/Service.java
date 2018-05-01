package com.cherkovskiy.application_context.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark class as Service.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

    /**
     * The same as {@link #name()}
     *
     * @return
     */
    String value() default "";

    /**
     * Name of service.
     *
     * @return
     */
    String name() default "";

    /**
     * LifecycleType of service.
     * Default: {@link LifecycleType#SINGLETON}
     *
     * @return
     */
    LifecycleType lifecycleType() default LifecycleType.SINGLETON;

    /**
     * LifecycleType of initialization.
     * Default: {@link InitType#LAZY}
     *
     * @return
     */
    InitType initType() default InitType.EAGER;

    enum LifecycleType {
        SINGLETON,
        PROTOTYPE
    }

    enum InitType {
        LAZY,
        EAGER
    }
}

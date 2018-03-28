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
     * Type of service.
     * Default: {@link Type#SINGLETON}
     *
     * @return
     */
    Type type() default Type.SINGLETON;

    /**
     * Type of initialization.
     * Default: {@link InitType#LAZY}
     *
     * @return
     */
    InitType initType() default InitType.EAGER;

    enum Type {
        SINGLETON,
        PROTOTYPE
    }

    enum InitType {
        LAZY,
        EAGER
    }
}

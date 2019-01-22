package com.cherkovskiy.application_context.api.configuration.annotations;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

    /**
     * The name prefix of the properties that are valid to bind to this object.
     *
     * @return the name prefix of the properties to bind
     */
    String value() default "";
}

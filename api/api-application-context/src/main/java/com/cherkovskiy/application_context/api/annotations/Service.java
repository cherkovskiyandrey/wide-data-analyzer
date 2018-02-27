package com.cherkovskiy.application_context.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

    String value() default "";

    Type type() default Type.SINGLETON;

    InitType initType() default InitType.LAZY;


    enum Type {
        SINGLETON,
        PROTOTYPE
    }

    enum InitType {
        LAZY,
        EAGER
    }
}

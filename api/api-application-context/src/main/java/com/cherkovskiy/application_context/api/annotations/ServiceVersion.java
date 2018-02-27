package com.cherkovskiy.application_context.api.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceVersion {

    int getMajor() default -1;

    int getMinor() default -1;

    String getSnapshot() default "";
}

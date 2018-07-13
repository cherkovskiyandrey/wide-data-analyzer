package com.cherkovskiy.application_context.api.configuration.annotations;

import com.cherkovskiy.application_context.api.configuration.Converter;
import com.cherkovskiy.application_context.api.configuration.EmptyConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationValue {

    String value();

    /**
     * Does not work for collections.
     *
     * @return
     */
    String defaultValue() default "";

    boolean required() default false;

    Class<? extends Converter<?, ?>> converter() default EmptyConverter.class;

}

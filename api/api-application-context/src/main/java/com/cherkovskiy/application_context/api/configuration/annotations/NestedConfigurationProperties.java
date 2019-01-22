package com.cherkovskiy.application_context.api.configuration.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NestedConfigurationProperties {
}

package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum ConfigurationTypes {

    //From java library plugin
    API("api", false),
    API_ELEMENTS("apiElements", false),
    IMPLEMENTATION("implementation", false),
    COMPILE_ONLY("compileOnly", true),
    RUNTIME_ONLY("runtimeOnly", false),

    //Inner from java library
    COMPILE_CLASSPATH("compileClasspath", true),
    RUNTIME_CLASSPATH("runtimeClasspath", true),

    //From java plugin
    COMPILE("compile", true),
    ANNOTATION_PROCESSOR("annotationProcessor", true),
    RUNTIME("runtime", true),

    // Custom configurations
    STUFF_ALL_API("___all_api___", true),


    //Not interested dependencies
    UNKNOWN("unknown", false);

    private final static ImmutableMap<String, ConfigurationTypes> STR_TO_TYPE = new ImmutableMap.Builder<String, ConfigurationTypes>()
            .putAll(Arrays.stream(values()).collect(toMap(t -> t.gradleStr, Function.identity(), (l, r) -> l)))
            .build();

    private final String gradleStr;
    private final boolean couldBeResolved;

    ConfigurationTypes(String gradleStr, boolean couldBeResolved) {
        this.gradleStr = gradleStr;
        this.couldBeResolved = couldBeResolved;
    }

    public static ConfigurationTypes of(String gradleStr) {
        return STR_TO_TYPE.getOrDefault(gradleStr, UNKNOWN);
    }

    public String getGradleString() {
        return gradleStr;
    }

    public boolean couldBeResolved() {
        return couldBeResolved;
    }
}

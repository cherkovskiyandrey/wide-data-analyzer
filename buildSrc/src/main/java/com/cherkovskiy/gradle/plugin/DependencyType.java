package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

enum DependencyType {

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

    //Not interested dependencies
    UNKNOWN("unknown", false);

    private final static ImmutableMap<String, DependencyType> STR_TO_TYPE = new ImmutableMap.Builder<String, DependencyType>()
            .putAll(Arrays.stream(values()).collect(toMap(t -> t.gradleStr, Function.identity(), (l, r) -> l)))
            .build();

    private final String gradleStr;
    private final boolean couldBeResolved;

    DependencyType(String gradleStr, boolean couldBeResolved) {
        this.gradleStr = gradleStr;
        this.couldBeResolved = couldBeResolved;
    }

    static DependencyType of(String gradleStr) {
        return STR_TO_TYPE.getOrDefault(gradleStr, UNKNOWN);
    }

    String getGradleString() {
        return gradleStr;
    }

    boolean couldBeResolved() {
        return couldBeResolved;
    }
}

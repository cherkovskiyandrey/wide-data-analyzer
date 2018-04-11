package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SubProjectTypes {
    API("api"),
    APPLICATION("application"),
    BUNDLE("bundle"),
    COMMON("common"),
    CORE("core"),
    PLUGIN("plugin");

    public static final String CORE_PROJECT_GROUP = "com.cherkovskiy"; //todo: rename to com.wide_data_analyzer

    public static final ImmutableMap<String, SubProjectTypes> STR_TO_TYPE = new ImmutableMap.Builder<String, SubProjectTypes>()
            .putAll(Arrays.stream(values()).collect(Collectors.toMap(SubProjectTypes::asString, Function.identity(), (l, r) -> l)))
            .build();

    private final String type;

    SubProjectTypes(String subGroupName) {
        this.type = subGroupName;
    }

    public static SubProjectTypes fromString(String s) {
        return STR_TO_TYPE.get(s);
    }

    public String asString() {
        return type;
    }

    @Override
    public String toString() {
        return "SubProjectTypes{" +
                "type='" + type + '\'' +
                '}';
    }
}

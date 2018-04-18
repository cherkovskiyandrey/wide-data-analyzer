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

    public static final ImmutableMap<String, SubProjectTypes> SUB_GROUP_NAME_TO_TYPE = new ImmutableMap.Builder<String, SubProjectTypes>()
            .putAll(Arrays.stream(values()).collect(Collectors.toMap(SubProjectTypes::getSubGroupName, Function.identity(), (l, r) -> l)))
            .build();

    private final String subGroupName;

    SubProjectTypes(String subGroupName) {
        this.subGroupName = subGroupName;
    }

    public static SubProjectTypes ofSubGroupName(String s) {
        return SUB_GROUP_NAME_TO_TYPE.get(s);
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    @Override
    public String toString() {
        return "SubProjectTypes{" +
                "subGroupName='" + subGroupName + '\'' +
                '}';
    }
}

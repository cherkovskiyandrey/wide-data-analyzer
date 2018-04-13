package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum SubProjectTypes {
    API("api", ":api:.*|:api$"),
    APPLICATION("application", ":applications:.*|:applications$"),
    BUNDLE("bundle", ":bundles:.*|:bundles$"),
    COMMON("common", ":common:.*|:common"),
    CORE("core", ":core:.*|:core"),
    PLUGIN("plugin", ":plugins:.*|:plugins$");

    public static final String CORE_PROJECT_GROUP = "com.cherkovskiy"; //todo: rename to com.wide_data_analyzer

    public static final ImmutableMap<String, SubProjectTypes> SUB_GROUP_NAME_TO_TYPE = new ImmutableMap.Builder<String, SubProjectTypes>()
            .putAll(Arrays.stream(values()).collect(Collectors.toMap(SubProjectTypes::getSubGroupName, Function.identity(), (l, r) -> l)))
            .build();

    public static final ImmutableMap<String, SubProjectTypes> PATH_TEMPLATE_TO_TYPE = new ImmutableMap.Builder<String, SubProjectTypes>()
            .putAll(Arrays.stream(values()).collect(Collectors.toMap(SubProjectTypes::getPathTemplate, Function.identity(), (l, r) -> l)))
            .build();

    private final String subGroupName;
    private final Pattern pathTemplate;

    SubProjectTypes(String subGroupName, String pathTemplate) {
        this.subGroupName = subGroupName;
        this.pathTemplate = Pattern.compile(pathTemplate);
    }

    public static SubProjectTypes ofSubGroupName(String s) {
        return SUB_GROUP_NAME_TO_TYPE.get(s);
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public String getPathTemplate() {
        return pathTemplate.pattern();
    }

    public boolean isSupportedPath(String projectPath) {
        return pathTemplate.matcher(projectPath).matches();
    }

    @Override
    public String toString() {
        return "SubProjectTypes{" +
                "subGroupName='" + subGroupName + '\'' +
                ", pathTemplate=" + pathTemplate.pattern() +
                '}';
    }
}

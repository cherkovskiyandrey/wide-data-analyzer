package com.cherkovskiy.gradle.plugin;

public enum DependencyGroup {
    API_EXPORT("WDA-Bundle-Api-Export-Dependencies", "embedded/api/"),
    API_IMPORT("WDA-Bundle-Api-Import-Dependencies", "embedded/api/"),
    COMMON("WDA-Bundle-Common-Dependencies", "embedded/libs/common/"),
    IMPL_INTERNAL("WDA-Bundle-Impl-Internal-Dependencies", "embedded/libs/wda/"),
    IMPL_EXTERNAL("WDA-Bundle-Impl-External-Dependencies", "embedded/libs/"),;

    private final String attributeName;
    private final String path;

    DependencyGroup(String attributeName, String path) {
        this.attributeName = attributeName;
        this.path = path;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getPath() {
        return path;
    }
}

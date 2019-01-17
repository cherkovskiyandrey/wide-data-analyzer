package com.cherkovskiy.application_context;

public enum StarterDependencyGroup {
    API("WDA-Starter-Api-Dependencies", ApplicationDirectories.API),
    COMMON("WDA-Starter-Common-Dependencies", ApplicationDirectories.LIB_COMMON),
    EXTERNAL_3RD_PARTY("WDA-Starter-3rdParty-Dependencies", ApplicationDirectories.LIB),
    INTERNAL("WDA-Starter-Internal-Dependencies", ApplicationDirectories.LIB_INTERNAL);

    private final String attributeName;
    private final ApplicationDirectories path;

    StarterDependencyGroup(String attributeName, ApplicationDirectories path) {
        this.attributeName = attributeName;
        this.path = path;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public ApplicationDirectories getPath() {
        return path;
    }
}

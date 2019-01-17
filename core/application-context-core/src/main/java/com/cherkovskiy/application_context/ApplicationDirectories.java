package com.cherkovskiy.application_context;

public enum ApplicationDirectories {
    API("api_core/"),
    BIN("bin/"),
    BUNDLES("bundles_core/"),
    APP("bundles_core/app/"),
    PLUGINS("plugins/"),

    LIB("lib/"),
    LIB_COMMON("lib/common/"),
    LIB_INTERNAL("lib/wda/");

    private final String path;

    ApplicationDirectories(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

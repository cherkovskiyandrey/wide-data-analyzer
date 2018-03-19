package com.cherkovskiy.gradle.plugin;

import org.gradle.api.artifacts.ResolvedModuleVersion;

import java.io.File;

class DependencyHolder {
    private final ResolvedModuleVersion module;
    private final File file;
    private final DependencyHolder parent;

    DependencyHolder(ResolvedModuleVersion module, File file, DependencyHolder parent) {
        this.module = module;
        this.file = file;
        this.parent = parent;
    }

    File getFile() {
        return file;
    }

    String getGroup() {
        return module.getId().getGroup();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append(module.getId().getGroup()).append(":");
        result.append(module.getId().getName()).append(":");
        result.append(module.getId().getVersion());

        if (parent != null) {
            result.append(" <- ").append(parent.toString());
        }

        return result.toString();
    }
}

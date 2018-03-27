package com.cherkovskiy.gradle.plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//Don't override equals and hashcode - DependencyScanner assume these methods are default
class DependencyHolder {

    private final String group;
    private final String name;
    private final String version;
    private final List<File> files;
    private final DependencyType type;
    private final DependencyHolder parent;
    private final DependencyHolder root;

    private DependencyHolder(Builder builder) {
        this.group = builder.group;
        this.name = builder.name;
        this.version = builder.version;
        this.files = builder.file;
        this.type = builder.type;
        this.parent = builder.parent;
        this.root = parent != null ? parent.getRoot() : this;
    }


    static Builder builder() {
        return new Builder();
    }

    @Nonnull
    String getGroup() {
        return group;
    }

    @Nonnull
    String getName() {
        return name;
    }

    @Nonnull
    String getVersion() {
        return version;
    }

    @Nonnull
    DependencyType getType() {
        return type;
    }

    List<File> getArtifacts() {
        return files;
    }

    Optional<DependencyHolder> getParent() {
        return Optional.ofNullable(parent);
    }

    boolean isTransitive() {
        return parent != null;
    }

    /**
     * @return root dependency of tree or itself if it is root
     */
    @Nonnull
    DependencyHolder getRoot() {
        return root;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append(group).append(":");
        result.append(name).append(":");
        result.append(version);

        if (parent != null) {
            result.append(" <- ").append(parent.toString());
        }

        return result.toString();
    }

    static class Builder {
        private String group;
        private String name;
        private String version;
        private List<File> file;
        private DependencyType type;
        private DependencyHolder parent;

        Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        Builder setName(String name) {
            this.name = name;
            return this;
        }

        Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        Builder setFile(List<File> file) {
            this.file = file;
            return this;
        }

        Builder setType(DependencyType type) {
            this.type = type;
            return this;
        }

        Builder setParent(DependencyHolder parent) {
            this.parent = parent;
            return this;
        }

        Builder copyOf(DependencyHolder orig) {
            this.group = orig.group;
            this.name = orig.name;
            this.version = orig.version;
            this.file = orig.files;
            this.type = orig.type;
            this.parent = orig.parent;
            return this;
        }

        DependencyHolder build() {
            Objects.requireNonNull(group);
            Objects.requireNonNull(name);
            Objects.requireNonNull(version);
            Objects.requireNonNull(type);

            return new DependencyHolder(this);
        }
    }
}

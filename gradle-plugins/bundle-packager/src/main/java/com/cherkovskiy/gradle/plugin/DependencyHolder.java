package com.cherkovskiy.gradle.plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//Don't override equals and hashcode - DependencyScanner assume these methods are default
public class DependencyHolder {

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


    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public String getGroup() {
        return group;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    @Nonnull
    public DependencyType getType() {
        return type;
    }

    public List<File> getArtifacts() {
        return files;
    }

    public Optional<DependencyHolder> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isTransitive() {
        return parent != null;
    }

    /**
     * @return root dependency of tree or itself if it is root
     */
    @Nonnull
    public DependencyHolder getRoot() {
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

    public static class Builder {
        private String group;
        private String name;
        private String version;
        private List<File> file;
        private DependencyType type;
        private DependencyHolder parent;

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setFile(List<File> file) {
            this.file = file;
            return this;
        }

        public Builder setType(DependencyType type) {
            this.type = type;
            return this;
        }

        public Builder setParent(DependencyHolder parent) {
            this.parent = parent;
            return this;
        }

        public Builder copyOf(DependencyHolder orig) {
            this.group = orig.group;
            this.name = orig.name;
            this.version = orig.version;
            this.file = orig.files;
            this.type = orig.type;
            this.parent = orig.parent;
            return this;
        }

        public DependencyHolder build() {
            Objects.requireNonNull(group);
            Objects.requireNonNull(name);
            Objects.requireNonNull(version);
            Objects.requireNonNull(type);

            return new DependencyHolder(this);
        }
    }
}

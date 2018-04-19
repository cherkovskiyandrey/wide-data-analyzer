package com.cherkovskiy.gradle.plugin;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ManifestArtifact implements Artifact {
    public static final String GROUP_SEPARATOR = ";";
    private final static Pattern MAVEN_PATTERN = Pattern.compile("^([^:]+):([^:]+):([^:]+):([^:]*)");

    private final String group;
    private final String name;
    private final String version;
    private final String fileName;

    public ManifestArtifact(@Nonnull String group, @Nonnull String name, @Nonnull String version, @Nonnull String fileName) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.fileName = fileName;
    }

    @Override
    @Nonnull
    public String getGroup() {
        return group;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public String getVersion() {
        return version;
    }
    
    @Nonnull
    public String getFileName() {
        return fileName;
    }

    @Nonnull
    public static String toManifestString(Artifact artifact) {
        final StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append(artifact.getGroup()).append(":");
        stringBuilder.append(artifact.getName()).append(":");
        stringBuilder.append(artifact.getVersion()).append(":");
        stringBuilder.append(artifact.getVersion());

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManifestArtifact that = (ManifestArtifact) o;
        return Objects.equals(group, that.group) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name, version);
    }

    public static Set<? extends Artifact> fromManifestString(String manifestString) {
        return Arrays.stream(manifestString.split(GROUP_SEPARATOR))
                .map(str -> {
                    Matcher matcher = MAVEN_PATTERN.matcher(str);

                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("Invalid format: " + manifestString);
                    }

                    return new ManifestArtifact(matcher.replaceFirst("$1"),
                            matcher.replaceFirst("$2"),
                            matcher.replaceFirst("$3"),
                            matcher.replaceFirst("$4")
                    );
                })
                .collect(Collectors.toSet());
    }
}

package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.bundles.Dependency;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ManifestArtifact implements Dependency {
    public static final String GROUP_SEPARATOR = ";";
    private final static Pattern MAVEN_PATTERN = Pattern.compile("^([^:]+):([^:]+):([^:]+):([^:]*)");

    private final String group;
    private final String name;
    private final String version;
    private final String fileName;

    public ManifestArtifact(@Nonnull String group, @Nonnull String name, @Nonnull String version, @Nullable String fileName) {
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

    @Override
    @Nullable
    public String getFileName() {
        return fileName;
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

    @Nonnull
    public static String toManifestString(ResolvedDependency artifact) {
        final StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append(artifact.getGroup()).append(":");
        stringBuilder.append(artifact.getName()).append(":");
        stringBuilder.append(artifact.getVersion()).append(":");
        stringBuilder.append(artifact.getFile().getName());

        return stringBuilder.toString();
    }

    public static Set<ManifestArtifact> fromManifestString(String manifestString) {

        return Arrays.stream(manifestString.split(GROUP_SEPARATOR))
                .filter(StringUtils::isNotBlank)
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

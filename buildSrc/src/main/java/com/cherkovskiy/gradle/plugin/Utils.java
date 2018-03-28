package com.cherkovskiy.gradle.plugin;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Utils {

    @SuppressWarnings("StatementWithEmptyBody")
    public static String lookUpRootGroupName(@Nonnull Project project) {
        for (; project.getParent() != null; project = project.getParent()) {
        }
        return (String) project.getGroup();
    }

    public static Optional<String> subProjectAgainst(String group, String rootGroupName) {
        if (StringUtils.startsWith(group, rootGroupName)) {
            String[] prjGroup = StringUtils.split(group.substring(rootGroupName.length()), '.');
            return prjGroup.length > 0 ? Optional.of(prjGroup[0]) : Optional.empty();
        }
        return Optional.empty();
    }
}

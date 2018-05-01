package com.cherkovskiy.gradle.plugin.api;

import javax.annotation.Nonnull;
import java.io.File;

public interface ResolvedDependency extends Dependency {

    @Nonnull
    File getFile();

}

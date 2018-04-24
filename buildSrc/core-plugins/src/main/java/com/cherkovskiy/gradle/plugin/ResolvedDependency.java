package com.cherkovskiy.gradle.plugin;

import javax.annotation.Nonnull;
import java.io.File;

public interface ResolvedDependency extends Dependency {

    @Nonnull
    File getFile();

}

package com.cherkovskiy.application_context.api.bundles;

import com.cherkovskiy.application_context.api.bundles.Dependency;

import javax.annotation.Nonnull;
import java.io.File;

public interface ResolvedDependency extends Dependency {

    @Nonnull
    File getFile();

}

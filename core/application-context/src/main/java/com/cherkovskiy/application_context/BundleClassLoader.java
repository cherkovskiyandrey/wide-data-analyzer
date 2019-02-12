package com.cherkovskiy.application_context;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

public class BundleClassLoader extends URLClassLoader {
    private final static URL[] emptyUrls = new URL[0];

    public BundleClassLoader(ClassLoader classLoader) {
        super(emptyUrls, classLoader);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    public void addDependencies(@Nonnull Collection<File> dependencies) {
//todo
    }
}

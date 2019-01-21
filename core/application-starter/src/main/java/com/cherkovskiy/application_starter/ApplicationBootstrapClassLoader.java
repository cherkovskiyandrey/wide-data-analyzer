package com.cherkovskiy.application_starter;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Use vanilla java only.
 */
class ApplicationBootstrapClassLoader extends URLClassLoader {

    ApplicationBootstrapClassLoader(URL[] resources, ClassLoader parent) {
        super(resources, parent);
    }
}

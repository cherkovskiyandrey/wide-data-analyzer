package com.cherkovskiy.application_starter;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Vanilla java only.
 */
class ApplicationContextClassLoader extends URLClassLoader {
    private final AtomicReference<Class<?>> starterRef = new AtomicReference<>();

    ApplicationContextClassLoader(URL[] resources, ApplicationBootstrapClassLoader parent) {
        super(resources, parent);
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        if (Starter.class.getName().equalsIgnoreCase(s)) {
            try {
                starterRef.getAndUpdate(current -> {
                    if (current == null) {
                        //load from this class loader
                        final Class<?> cls;
                        try {
                            cls = findClass(s);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        resolveClass(cls);
                        return cls;
                    }
                    return current;
                });
            } catch (RuntimeException e) {
                if (e.getCause() instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) e.getCause();
                }
            }
        }
        return super.loadClass(s);
    }
}

package com.cherkovskiy.application_starter;

import java.net.URL;
import java.util.List;

/**
 * Created from system class loader.
 * Vanilla java only.
 * It has to be ready to be wrapped into ApplicationRootClassLoader interface
 */
class ApplicationRootClassLoaderSkeleton extends ClassLoader {
    public ApplicationRootClassLoaderSkeleton(List<URL> bootstrapClasses, ClassLoader parentClassLoader) {
        throw new UnsupportedOperationException("TODO");
    }


    //todo: можно унаслеоваться от URLClassLoader и
    // перегрузить C:/Program Files/Java/jdk1.8.0_131/jre/lib/rt.jar!/java/net/URLClassLoader.class:341
    //java.lang.ClassLoader.findLoadedClass
    //java.lang.ClassLoader.findClass

    //TODO: ну как быть с кэшем? - defineClass видимо засовывает в Metaspace класс и тот может расти бесконечно?
    // хранить у себя в кэше классы на уровне хипа не гуд совсем! - проверить будет ли при defineClass с таким же именем перетераться он в кэше?
}

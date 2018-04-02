package com.cherkovskiy.comprehensive_serializer;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Deserializer {

    public static <T extends Serializable> T deserializeFrom(InputStream from, Class<T> token)
            throws IOException, ClassNotFoundException {

        return deserializeFrom(from, token, Thread.currentThread().getContextClassLoader());
    }


    public static <T extends Serializable> T deserializeFrom(InputStream from, Class<T> token, ClassLoader parentLoader)
            throws IOException, ClassNotFoundException {

        return deserializeFrom(from, token, parentLoader, true);
    }

    public static <T extends Serializable> T deserializeFrom(InputStream from, Class<T> token, ClassLoader parentLoader, boolean isExternalFirst)
            throws IOException, ClassNotFoundException {

        return deserializeFrom(from, token, parentLoader, isExternalFirst, Collections.emptySet());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserializeFrom(InputStream from, Class<T> token, ClassLoader parentLoader, boolean isExternalFirst, Set<String> externalExcludeMask)
            throws IOException, ClassNotFoundException {

        final ClassPresenter classPresenter = Parser.parse(from);

        //externalExcludeMask = Sets.newHashSet(externalExcludeMask);
        externalExcludeMask = new HashSet<>(externalExcludeMask);
        externalExcludeMask.add(token.getName());

        final PresentedClassLoader presentedClassLoader = new PresentedClassLoader(parentLoader, classPresenter, isExternalFirst, externalExcludeMask);
        final Class<?> rootClass = presentedClassLoader.loadRootClass();

        if (!token.isAssignableFrom(rootClass)) {
            throw new ClassCastException(String.format("Class from stream is %s is not compatible with token: %s", rootClass.getName(), token.getName()));
        }

        try (final ObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(presentedClassLoader, classPresenter.getObjectAsStream())) {
            return (T) objectInputStream.readObject();
        }
    }
}

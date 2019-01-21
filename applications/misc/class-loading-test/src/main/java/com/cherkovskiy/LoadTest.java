package com.cherkovskiy;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LoadTest {


    /**
     *  Убеждаемся что интерфейс класса/другие используемые в классе классы будут грузится через тот же класс лоадер
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        String nameOfA = A.class.getName();
        byte[] codeOfA = readCode(A.class);

        String nameOfB = B.class.getName();
        byte[] codeOfB = readCode(B.class);

        ClassLoader parent = new ClassLoader() {
            private final ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
            private final Map<String, Class<?>> loadedClasses = new HashMap<>();

            @Override
            public Class<?> loadClass(String className) throws ClassNotFoundException {

                if (
                        className.equalsIgnoreCase(nameOfA) ||
                                className.equalsIgnoreCase(nameOfB)
                        ) {
                    byte[] code = className.equalsIgnoreCase(nameOfA) ? codeOfA : codeOfB;
                    final Class<?> cls = defineClass(className, code, 0, code.length);
                    resolveClass(cls);
                    loadedClasses.put(className, cls);
                    return cls;
                }

                final Class<?> cls = parentLoader.loadClass(className);
                resolveClass(cls);
                return cls;
            }
        };


        Class<?> cls = parent.loadClass(nameOfA);

        Object o = cls.newInstance();

        System.out.println("---");
    }

    private static byte[] readCode(Class<?> cls) throws IOException {
        final String pathToClass = cls.getName().replace(".", "/") + ".class";
        try (InputStream inputStream = cls.getClassLoader().getResourceAsStream(pathToClass)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

}

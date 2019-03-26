package com.cherkovskiy.code_gen;

import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;

//child first
public class BootstrapFilteredClassLoader extends ClassLoader {
    public static final Path BASE_CLASSES_PATH = Paths.get("C:\\Andrey\\WORKSPACE\\wide-data-analyzer\\applications\\misc\\class-loading-test\\build\\classes\\java\\main");
    private final ConcurrentMap<String, byte[]> knownClasses = Maps.newConcurrentMap();

    public BootstrapFilteredClassLoader() {
        super(ClassLoader.getSystemClassLoader());
    }

    @Override
    protected Class<?> loadClass(String className, boolean shouldResolve) throws ClassNotFoundException {
        synchronized (this.getClassLoadingLock(className)) {
            Class var4 = findLoadedClass(className);
            if (var4 == null) {
                byte[] classCode = knownClasses.get(className);
                if (classCode != null) {
                    var4 = defineClass(className, classCode, 0, classCode.length);
                    if (shouldResolve) {
                        resolveClass(var4);
                    }
                    return var4;
                }
            }
            return super.loadClass(className, shouldResolve); //is lock reenterable?
        }
    }

    public void addClass(String className, byte[] binaryClass) {
        knownClasses.putIfAbsent(className, binaryClass);
    }

    public void addClass(String className) throws IOException {
        File path = Paths.get(BASE_CLASSES_PATH.toString(), className.replace('.', '/').concat(".class")).toFile();
        knownClasses.putIfAbsent(className, FileUtils.readFileToByteArray(path));
    }

    public boolean contain(@Nonnull String className) {
        return knownClasses.containsKey(className);
    }
}

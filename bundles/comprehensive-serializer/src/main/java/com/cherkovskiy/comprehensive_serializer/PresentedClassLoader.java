package com.cherkovskiy.comprehensive_serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PresentedClassLoader extends ClassLoader {
    private final ClassPresenter classPresenter;
    private final boolean isExternalFirst;
    private final Set<Pattern> externalExcludePatterns;
//    private final ConcurrentMap<String, ReentrantLock> lockByClass = Maps.newConcurrentMap();
//    private final Map<String, Class<?>> loadedClasses = Maps.newHashMap();

    private final ConcurrentMap<String, ReentrantLock> lockByClass = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();

    PresentedClassLoader(ClassLoader parentLoader, ClassPresenter classPresenter, boolean isExternalFirst, Set<String> externalExcludeMask) {
        super(parentLoader);
        this.classPresenter = classPresenter;
        this.isExternalFirst = isExternalFirst;
        this.externalExcludePatterns = toPatterns(externalExcludeMask);
    }

    public Class<?> loadRootClass() throws ClassNotFoundException {
        return loadClass(classPresenter.getRootObjectName());
    }


    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        final ReentrantLock lock = getLockByName(className);
        lock.lock();
        try {
            //try load from parent first
            if (!isExternalFirst) {
                Class<?> clsFromParent = null;
                try {
                    clsFromParent = super.loadClass(className);
                } catch (ClassNotFoundException e) {
                }

                if (clsFromParent != null) {
                    return clsFromParent;
                }

                if (isMatchToPattern(className, externalExcludePatterns)) {
                    throw new ClassNotFoundException(String.format("Class %s could not be loaded. There is not in parent class loader and there is in the exclude list.", className));
                }
            }

            //try load from ClassPresenter
            if (classPresenter.contain(className) && !isMatchToPattern(className, externalExcludePatterns)) {
                if (loadedClasses.containsKey(className)) {
                    return loadedClasses.get(className);
                }
                final byte[] rawClassData = classPresenter.getClassByName(className);
                final Class<?> cls = defineClass(className, rawClassData, 0, rawClassData.length);
                resolveClass(cls);
                loadedClasses.put(className, cls);

                return cls;
            }

            return super.loadClass(className);

        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock getLockByName(String className) {
        return lockByClass.computeIfAbsent(className, c -> new ReentrantLock());
    }

    private static Set<Pattern> toPatterns(Set<String> excludedMask) {
        return excludedMask.stream().map(Pattern::compile).collect(Collectors.toSet());
    }

    private static boolean isMatchToPattern(String name, Set<Pattern> excludedMask) {
        return excludedMask.stream().map(p -> p.matcher(name)).anyMatch(Matcher::matches);
    }
}

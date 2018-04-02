package com.cherkovskiy.comprehensive_serializer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.bcel.classfile.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//        //TODO: bucket сериализации
//        //-----------------------------------------------
//        final TestInterface testInterface = new TestInterfaceImpl.InnerContainerLevelOne.Inner(null, null);
//
//        final SerializedBucket serializedBucket = new SerializedBucket();
//        Serializer.serializeTo(api, serializedBucket);
//        Serializer.serializeTo(testInterface, serializedBucket);
//
//        try (final OutputStream outputStream = Files.newOutputStream(Paths.get("full_bucket.eser"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//            serializedBucket.writeTo(outputStream);
//        }
//
//        // add content
//        try (final InputStream inputStream = Files.newInputStream(Paths.get("full_bucket.eser"))) {
//            serializedBucket = new SerializedBucket(inputStream);
//        }
//        Serializer.serializeTo(api, serializedBucket);
//        Serializer.serializeTo(testInterface, serializedBucket);
//
//        try (final OutputStream outputStream = Files.newOutputStream(Paths.get("full_bucket.eser"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//            serializedBucket.writeTo(outputStream);
//        }
//
//        //или по отдельности:
//        try (final OutputStream outputStream = Files.newOutputStream(Paths.get("classes.eser"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//            serializedBucket.writeClassesTo(outputStream);
//            serializedBucket.writeClassesTo(outputStream, excludeFilterMask);
//        }
//        try (final OutputStream outputStream = Files.newOutputStream(Paths.get("content.eser"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//            serializedBucket.writeContetnTo(outputStream);
//        }
//
//        //-----------------------------------------------
//        //TODO: если мы общаемся по rmi - то мы имеем общую базу классов которые мы переслали уже и следующий раз отправлемя только те что не отправляли ранее
//        // На каждой стороне у нас есть актуальный список тех классов что есть на другой стороне.
//        // Пусть нам пришёл некий класс в метод rmi - мы смотрим есть ли он уже в этом списке, если есть - отправляем, если нет
//        // то Serializer.serializeTo(testInterface, serializedBucket, Collections.emptySet(), allExistingClasses);
//        // тем самым получаем пр сериализации только те классы, которых нет ещё на другой стороне и отправляем сначала только новые классы, если такие есть
//        // получаем классы, ДОБАВЛЯЕМ их в наш специальный класс лоадер: PresentedClassLoader, после получаем уже контент и десериализуем класс
//        //TODO: нужно так же решить проблему с версией разных классов
public class Serializer {

    //TODO: log4j
    private static volatile boolean isDebugMode = false;

    public static void setDebugMode(boolean isDebugMode) {
        Serializer.isDebugMode = isDebugMode;
    }

    //Поддерживаем:
    // - обычные классы
    // - вложенные статические (не тянет класс куда вложен, если нет на него никаких зависимостей)
    // - анонимные
    // - лямбды
    // - массивы как корневой объект не поддерживается, нужно его вложить в объект
    // - обязательно должны быть сериализуемые
    // - классы из методов не обязаны быть сериализуемыми
    // - нет поддержки динамической загрузки через Class.forName, такие классы невозможно отследить и засериализовать их байт код
    //Структура файла: (big endian)
    // Заголовок:
    // 4 байта - кол-во классов в заголовке
    // 2 байта - размер название основного класса в UTF8 - B
    // B байт - название основого класса в UTF8
    // 4 байт - размер байткода основного класса (идёт сразу же после заголовка)
    // 2 байта - размер название 1 вспомогательного класса в UTF8 - A1
    // A1 байт - название 1 вспомогательного класса в UTF8
    // 4 байт - размер байткода 1 вспомогательного класса (идёт сразу же за основным класом)
    //....

    // в конце идёт засериализованного основоного класса (идёт в самом конце)
    public static <T extends Serializable> void serializeTo(T object, OutputStream outputStream, Set<String> includedMask, Set<String> excludedMask)
            throws IOException, ClassNotFoundException {

        Objects.requireNonNull(object, "Object must be not null.");
        Objects.requireNonNull(outputStream, "OutputStream must be not null.");

        final String className = object.getClass().getName();
        final HeaderBuilder header = new HeaderBuilder();
        final Set<Pattern> includePatterns = toPatterns(includedMask);
        final Set<Pattern> excludePatterns = toPatterns(excludedMask);

        final InterceptedObjectWriter interceptedObjectWriter = InterceptedObjectWriter.write(object);

        if (isStandardClass(object.getClass()) || isMatchToPattern(className, excludePatterns)) {
            header.setCoreClass(className, 0, interceptedObjectWriter.getContent().length);
            outputStream.write(header.build());
            outputStream.write(interceptedObjectWriter.getContent());
            return;
        }

        final LinkedHashMap<String, byte[]> clsNameToContent = Maps.newLinkedHashMap();
        walkClass(object.getClass(), object.getClass().getClassLoader(), clsNameToContent, includePatterns, excludePatterns);

        for (Class<?> runtimeClasses : interceptedObjectWriter.getRuntimeClasses()) {
            walkClass(runtimeClasses, object.getClass().getClassLoader(), clsNameToContent, includePatterns, excludePatterns);
        }

        if (isDebugMode) {
            System.out.println("Classes to serializeTo: ");
            clsNameToContent.keySet().forEach(c -> System.out.println(" - " + c));
        }

        boolean isCoreSet = true;
        for (Map.Entry<String, byte[]> iterator : clsNameToContent.entrySet()) {
            if (isCoreSet) {
                header.setCoreClass(iterator.getKey(), iterator.getValue().length, interceptedObjectWriter.getContent().length);
                isCoreSet = false;
            } else {
                header.addAuxClass(iterator.getKey(), iterator.getValue().length);
            }
        }

        outputStream.write(header.build());
        for (byte[] code : clsNameToContent.values()) {
            outputStream.write(code);
        }

        outputStream.write(interceptedObjectWriter.getContent());
    }

    public static <T extends Serializable> void serializeTo(T object, OutputStream to) throws IOException, ClassNotFoundException {
        serializeTo(object, to, Collections.emptySet(), Collections.emptySet());
    }

    private static boolean isStandardClass(Class<?> className) {
        return className.isArray() ||
                className.isPrimitive() ||
                className.getName().startsWith("java.") ||
                className.getName().startsWith("javax.");
    }

    private static void walkClass(Class<?> cls, ClassLoader classLoader, Map<String, byte[]> clsNameToContent, Set<Pattern> includeMask, Set<Pattern> excludedMask)
            throws IOException, ClassNotFoundException {

        if (cls == null ||
                isStandardClass(cls) ||
                clsNameToContent.containsKey(cls.getName()) ||
                isMatchToPattern(cls.getName(), excludedMask) ||
                (!includeMask.isEmpty() && !isMatchToPattern(cls.getName(), includeMask))) {
            return;
        }

        if (isDebugMode) {
            System.out.println("walkClass: " + cls.getName());
        }

        final String pathToClass = cls.getName().replace(".", "/") + ".class";

        //1. Load code.
        try (InputStream inputStream = classLoader.getResourceAsStream(pathToClass)) {
            final byte[] code = IOUtils.toByteArray(inputStream);
            clsNameToContent.put(cls.getName(), code);
        }

        //3. Parse class file
        final JavaClass javaClass;
        try (InputStream inputStream = classLoader.getResourceAsStream(pathToClass)) {
            final ClassParser classParser = new ClassParser(inputStream, "");
            javaClass = classParser.parse();
        }

        //2. Collect all runtime classes need to work current cls.
        final Set<String> allRuntimeClasses = Sets.newHashSet();

        //2.1. Super class.
        allRuntimeClasses.add(javaClass.getSuperClass().getClassName());

        //2.2. All Interfaces.
        Arrays.stream(javaClass.getAllInterfaces())
                .map(JavaClass::getClassName)
                .forEach(allRuntimeClasses::add);

        //TODO seems to be unnecessary
        //2.3. All fields signature.
        //walkFields(javaClass, allRuntimeClasses);

        //TODO seems to be unnecessary
        //2.4. All methods signature (whit exceptions) + annotations.
        //walkMethods(javaClass, allRuntimeClasses);

        //2.5 All types from all methods
        final MethodScanner methodScanner = new MethodScanner(javaClass, isDebugMode);
        Arrays.stream(javaClass.getMethods())
                .map(Method::getCode)
                .map(Optional::ofNullable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(code -> code.accept(methodScanner));

        allRuntimeClasses.addAll(methodScanner.getScannedClasses());


        //4. Recursive walk
        for (String className : allRuntimeClasses) {
            walkClass(Class.forName(className, true, classLoader), classLoader, clsNameToContent, includeMask, excludedMask);
        }
    }

    private static void walkMethods(JavaClass javaClass, Set<String> allRuntimeClasses) {
        for (Method method : javaClass.getMethods()) {
            if (StringUtils.isNotBlank(method.getSignature())) {
                final String returnType = Utility.methodSignatureReturnType(method.getSignature(), false);
                allRuntimeClasses.add(returnType);

                if (isDebugMode) {
                    System.out.println("Signature: " + method.getSignature() + " return type: " + returnType);
                }

                final List<String> argsTypes = Arrays.asList(Utility.methodSignatureArgumentTypes(method.getSignature()));
                allRuntimeClasses.addAll(argsTypes);

                if (isDebugMode) {
                    System.out.println("Signature: " + method.getSignature() + " arguments type: " + argsTypes.stream().collect(Collectors.joining("; ")));
                }
            }

            if (StringUtils.isNotBlank(method.getGenericSignature())) {
                final Set<String> returnTypes = extractAllTypes(Utility.methodSignatureReturnType(method.getGenericSignature(), false));
                allRuntimeClasses.addAll(returnTypes);

                if (isDebugMode) {
                    System.out.println("Signature: " + method.getGenericSignature() + " generic return types: " + returnTypes.stream().collect(Collectors.joining("; ")));
                }

                //TODO: try to this method: Type.getArgumentTypes(getSignature(cpg)); there - com/cherkovskiy/Serializer.java:205
                //TODO: не работает для <T:Ljava/time/LocalTime;:Ljava/lang/Runnable;>(TT;)TT; -> public <T extends LocalTime & Runnable> T getLocalTime(T t) throws Exception {
                final Set<String> argsTypes = Arrays.stream(Utility.methodSignatureArgumentTypes(method.getGenericSignature(), false))
                        .flatMap(s -> extractAllTypes(s).stream())
                        .collect(Collectors.toSet());

                if (isDebugMode) {
                    System.out.println("Signature: " + method.getGenericSignature() + " generic arguments type: " + argsTypes.stream().collect(Collectors.joining("; ")));
                }
            }

            if (method.getExceptionTable() != null) {
                final List<String> exceptions = Arrays.asList(method.getExceptionTable().getExceptionNames());
                allRuntimeClasses.addAll(exceptions);

                if (isDebugMode) {
                    System.out.println("Signature: " + method.getSignature() + " exceptions: " + exceptions.stream().collect(Collectors.joining("; ")));
                }
            }
        }
    }

    private static void walkFields(JavaClass javaClass, Set<String> allRuntimeClasses) {
        for (Field field : javaClass.getFields()) {
            if (StringUtils.isNotBlank(field.getType().getSignature())) {
                final String fieldType = Utility.signatureToString(field.getType().getSignature(), false);
                allRuntimeClasses.add(fieldType);

                if (isDebugMode) {
                    System.out.println("Field: " + fieldType);
                }
            }

            if (StringUtils.isNotBlank(field.getGenericSignature())) {
                final Set<String> genericFieldTypes = extractAllTypes(Utility.signatureToString(field.getGenericSignature(), false));
                allRuntimeClasses.addAll(genericFieldTypes);

                if (isDebugMode) {
                    System.out.println("Generic field: " + field.getGenericSignature() + " => " + genericFieldTypes.stream().collect(Collectors.joining("; ")));
                }
            }
        }
    }

    private static Set<String> extractAllTypes(String signature) {
        final Set<String> types = StringUtils.isBlank(signature) ? Collections.emptySet() :
                Arrays.stream(signature.split("[<&>\\s?]"))
                        .filter(s -> Stream.of("extends", "super")
                                .noneMatch(p -> p.equalsIgnoreCase(s)))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());

        if (isDebugMode) {
            System.out.println("Custom extractor: input: " + signature + " corresponding types: " + types.stream().collect(Collectors.joining("; ")));
        }

        return types;
    }

    private static Set<Pattern> toPatterns(Set<String> excludedMask) {
        return excludedMask.stream().map(Pattern::compile).collect(Collectors.toSet());
    }

    private static boolean isMatchToPattern(String name, Set<Pattern> excludedMask) {
        return excludedMask.stream().map(p -> p.matcher(name)).anyMatch(Matcher::matches);
    }

//    public static <T extends Serializable> void serializeTo(T object, SerializedBucket serializedBucket) {
//        //todo
//    }
}

package com.cherkovskiy.code_gen;

import com.google.common.collect.*;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;

public abstract class Utils {

    @Nonnull
    public static JavaClass patchPool(@Nonnull ImmutableMap<String, String> replacementClasses, @Nonnull JavaClass javaClass) throws IOException {
        Multimap<String, Integer> utf8Strings = ArrayListMultimap.create();
        System.out.println(format("Patch file %s:", javaClass.getClassName()));

        replacementClasses = replacementClasses.entrySet().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()),
                        Pair.of(entry.getKey().replace('.', '/'), entry.getValue().replace('.', '/'))))
                .collect(ImmutableMap.toImmutableMap(Pair::getLeft, Pair::getRight));

        String fileName = replacementClasses.entrySet().stream()
                .filter(entry -> javaClass.getFileName().contains(entry.getKey()))
                .findFirst()
                .map(entry -> javaClass.getFileName().replace(entry.getKey(), entry.getValue()))
                .orElse(javaClass.getFileName());

        for (int i = 0; i < javaClass.getConstantPool().getConstantPool().length; i++) {
            final int index = i;
            Constant constant = javaClass.getConstantPool().getConstantPool()[i];
            if (constant != null && constant.getTag() == 1) {
                String originString = ((ConstantUtf8) constant).getBytes();

                replacementClasses.forEach((lookupClass, replaceClass) -> {
                    String changedString = originString.replace(lookupClass, replaceClass);
                    if (!changedString.equalsIgnoreCase(originString)) {
                        utf8Strings.put(changedString, index);
                        System.out.println(format("Replacement: %d: %s  ->  %s", index, originString, changedString));
                    }
                });
            }
        }

        ClassGen classGen = new ClassGen(javaClass);
        for (Map.Entry<String, Collection<Integer>> entry : utf8Strings.asMap().entrySet()) {
            for (Integer index : entry.getValue()) {
                classGen.getConstantPool().setConstant(index, new ConstantUtf8(entry.getKey()));
            }
        }

        byte[] binaryClass = serialize(classGen.getJavaClass());
        Repository.getRepository().removeClass(javaClass);
        JavaClass patchedClass = readClassFromBytes(binaryClass);
        patchedClass.setFileName(fileName);

        return patchedClass;
    }

    @Nonnull
    private static JavaClass readClassFromBytes(@Nonnull byte[] binaryClass) throws IOException {
        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(binaryClass))) {
            final ClassParser classParser = new ClassParser(inputStream, "");
            return classParser.parse();
        }
    }

    @Nonnull
    public static byte[] serialize(@Nonnull JavaClass javaClass) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                javaClass.dump(dataOutputStream);  //write to byte array changed file
            }
            outputStream.flush();
            return outputStream.toByteArray();
        }
    }

    public static void removeInheritedMethods(@Nonnull ClassGen classGen, @Nonnull JavaClass originJavaClass) {
        ImmutableSet<Method> patchedMethods = Arrays.stream(classGen.getMethods()).collect(ImmutableSet.toImmutableSet());
        ImmutableSet<Method> originalMethods = Arrays.stream(originJavaClass.getMethods()).collect(ImmutableSet.toImmutableSet());

        for (Method commonMethod : Sets.intersection(originalMethods, patchedMethods)) {
            classGen.removeMethod(commonMethod);
        }
    }
}

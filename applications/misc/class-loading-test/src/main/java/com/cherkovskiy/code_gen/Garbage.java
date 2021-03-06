package com.cherkovskiy.code_gen;

import com.cherkovskiy.code_gen.api.A;
import com.cherkovskiy.code_gen.api.B;
import com.cherkovskiy.code_gen.api.L;
import com.cherkovskiy.code_gen.impl.AImpl;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

public class Garbage {
    /**
     * Для реализации новой концепции необходимо проверь генераци:
     * - интерфейс A в A_v2_gen с добавление нового метода и сделать его наследником от A
     * <p>
     * - интерфейс A_gen_v2 в B_v2_gen с добавление перегруженного метода принимающего A_v2_gen и сделать его наследником от A_gen_v2
     * <p>
     * - генерация прокси класса A_to_A_v2_gen_converter_gen имплементирующего A_v2_gen:
     * class A_to_A_v2_gen_converter_gen implement A_v2_gen {                  <<<<----- byte buddy: @Pipe or @SuperCall?
     * private final A a;
     * A_to_A_v2_gen_converter_gen(A a) {this.a = a;}
     * void f1() {return a.f1();}
     * D f2(B c) {throw new IllegalStateException("Attempt to invoke unprovided method.
     * Bundle which provide implementation doesn't know about this method.
     * It is connected with not-default new interface method during hot bundle redeploy.");}
     * --или если f2 default то просто:
     * D f2(B c) {return a.f2(c);}
     * }
     * <p>
     * <p>
     * - изменение класса имплементации BImpl:
     * * имя на генерённое: BImpl_v2_gen ? - повлечёт к изменению всех классов наследников и которые его использую - не стоит
     * * наследование на генерённый интерфейс: A_v2_gen
     * * изменение сигнатуры метода принимающего A на A_v2_gen
     * * изменение тела метода явно использующее тип A на A_v2_gen
     * * генерация перегруженного метода принимающего A и имеющее тело: L f1(A a) { return f1(new A_to_A_v2_gen_converter_gen(a)); }
     */
    @Nonnull
    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

//        DynamicType.Unloaded<?> dynamicTypesContainer;


        //интерфейс A в A_v2_gen с добавление нового метода и сделать его наследником от A
        //--------------------
        DynamicType.Unloaded<?> dynamicTypeA = new ByteBuddy()
                .makeInterface(A.class) //already in ApplicationRootClassLoader
                .name(A.class.getName() + "_v2_gen")

                //Define new method according to new version of A class
                .defineMethod("f2", void.class, Visibility.PUBLIC)
                .withParameter(
                        InstrumentedType.Default.of("com.cherkovskiy.code_gen.new_api.B", null, Modifier.PUBLIC | Modifier.INTERFACE),
                        "c"
                )
                .withoutCode()

                //Define cycle dependency to generated code
                .defineMethod("f3", void.class, Visibility.PUBLIC)
                .withParameter(
                        InstrumentedType.Default.of(B.class.getName() + "_v2_gen", null, Modifier.PUBLIC | Modifier.INTERFACE),
                        "b"
                )
                .withoutCode()
                .make();

        //dynamicTypesContainer = dynamicTypeA;

        Class<?> A_v2_genClass = dynamicTypeA.load(OnFlyGeneratorTest.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        System.out.println(A_v2_genClass);
        //--------------------


        //интерфейс A_gen_v2 в B_v2_gen с добавление перегруженного метода принимающего A_v2_gen и сделать его наследником от A_gen_v2
        //--------------------
        DynamicType.Unloaded<?> dynamicTypeB = new ByteBuddy()
                .makeInterface(B.class) //already in ApplicationRootClassLoader
                .name(B.class.getName() + "_v2_gen")

                //Define overloaded method according to new version of A class
                .defineMethod("f1", L.class, Visibility.PUBLIC)
                .withParameter(
                        dynamicTypeA.getTypeDescription(),
                        "a"
                )


//                //TODO: dynamicTypeB.load(OnFlyGeneratorTest.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded().getMethods()[0].getParameters()[0].getAnnotations()
//                // не добавил аннотацию!
//                .annotateParameter(
//                        L.class.getAnnotations() //это отработало нормально
////                        AnnotationDescription.Builder.ofType(
////                                InstrumentedType.Default.of("javax.annotation.Nonnull", null, Modifier.PUBLIC | Opcodes.ACC_ANNOTATION)
////                        ).build()
//                )
                .withoutCode()

                .require(dynamicTypeA) //It works fine. In both directions.

                .make();

        Class<?> B_v2_genClass = dynamicTypeB.load(OnFlyGeneratorTest.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        System.out.println(B_v2_genClass);

        //dynamicTypesContainer.include(dynamicTypeB);

        //Doesn't work :(
//        Map<TypeDescription, Class<?>> allClasses =
//        dynamicTypesContainer.load(OnFlyGeneratorTest.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
//                .getLoadedAuxiliaryTypes();
//
//        System.out.println(allClasses);
        //--------------------


        //генерация прокси класса A_to_A_v2_gen_converter_gen имплементирующего A_v2_gen:
        //--------------------
        //todo
        //--------------------


        //TODO: GENERIC! + ANNOTATION!
        // изменение сигнатуры метода принимающего A на A_v2_gen
        // изменение тела метода явно использующее тип A на A_v2_gen
        // наследование на генерённый интерфейс: B_v2_gen
        //--------------------
        byte[] bImplAsByteArray;
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "com/cherkovskiy/code_gen/new_impl/BImpl.class")) {



            final ClassParser classParser = new ClassParser(inputStream, "");
            JavaClass javaClass = classParser.parse(); //TODO: тут можно закрывать стрим

            Multimap<String, Integer> utf8Strings = replaceTypes(
                    ImmutableMap.of(
                            "com/cherkovskiy/code_gen/new_api/A", A.class.getName().replace('.', '/') + "_v2_gen",

                            //We have to replace in this test case only because class has been compiled with invoking A.f3(com/cherkovskiy/code_gen/new_api/A_gen_v2)
                            "com/cherkovskiy/code_gen/new_api/A_gen_v2", B.class.getName().replace('.', '/') + "_v2_gen"
                    ),
                    javaClass
            );

            System.out.println("====================================================");
            utf8Strings.forEach((k, v) -> System.out.println(format("%d -> %s", v, k)));

            System.out.println();

//            //------------- поменять ---------
            ClassGen classGen = new ClassGen(javaClass);

            for (Map.Entry<String, Collection<Integer>> entry : utf8Strings.asMap().entrySet()) {
                for (Integer index : entry.getValue()) {
                    classGen.getConstantPool().setConstant(index, new ConstantUtf8(entry.getKey()));
                }
            }

            classGen.removeInterface("com.cherkovskiy.code_gen.new_api.A_gen_v2");
            classGen.addInterface(B.class.getName() + "_v2_gen");

            //TODO: так не помогает - пул не перетерается и переиспользуется - попробовать выгрузить в буфер сначала а потом снова считать
            javaClass = classGen.getJavaClass();
            classGen = new ClassGen(javaClass);

            //todo
            //---------- пробуем создать метод копию имеющегося с изменённым именем и типами входных и выходных значений
            Method originMethod = Arrays.stream(javaClass.getMethods())
                    .filter(m -> m.getSignature() != null && m.getSignature().contains(A.class.getName().replace('.', '/') + "_v2_gen") ||
                            m.getGenericSignature() != null && m.getGenericSignature().contains(A.class.getName().replace('.', '/') + "_v2_gen"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Can't find method")) //todo
                    ;


            //TODO: не видет нихера значения в новом пуле
            Type[] argumentTypes = Type.getArgumentTypes(originMethod.getSignature().replace(A.class.getName().replace('.', '/') + "_v2_gen", "com/cherkovskiy/code_gen/new/A"));


            //TODO: та как беру из оригинального объекта даже подменять НЕ нужно, т.к. тип ещё старый
            //- не получилось, т.к. тип уже подменился в пуле и он на него ссылается
            MethodGen methodGen = new MethodGen(
                    originMethod.getAccessFlags() | Const.ACC_ABSTRACT, //make it abstract just for test
                    originMethod.getReturnType(), //TODO: Type.getReturnType(originMethod.getReturnType().getSignature()), //todo: change signature from A_v2_gen to A
                    argumentTypes, //TODO: originMethod.getArgumentTypes(), //todo: change signature from A_v2_gen to A by means of Type.getArgumentTypes(
                    null, //new String[0], //todo: String[] arg_names, не ясно пока как
                    originMethod.getName(),
                    javaClass.getClassName(),
                    null, //TODO: code, null for abstract methods
                    classGen.getConstantPool()
            );
            classGen.addMethod(methodGen.getMethod());
            //-----------------

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                    classGen.getJavaClass().dump(dataOutputStream);  //write to byte array changed file
                }
                outputStream.flush();
                System.out.println(outputStream.toByteArray());
                bImplAsByteArray = outputStream.toByteArray();
            }

        }
        //--------------------

        // генерация перегруженного метода принимающего A и имеющее тело: L f1(A a) { return f1(new A_to_A_v2_gen_converter_gen(a)); }
        //--------------------
        DynamicType.Unloaded<?> dynamicTypeBImpl = new ByteBuddy()
                .redefine(
                        InstrumentedType.Default.of("com.cherkovskiy.code_gen.new_impl.BImpl",
                                TypeDescription.Generic.Builder.rawType(Object.class).build(),
                                Modifier.PUBLIC),
                        ClassFileLocator.Simple.of("com.cherkovskiy.code_gen.new_impl.BImpl", bImplAsByteArray)
                )
                .name("com.cherkovskiy.code_gen.new_impl.BImpl_GEN") //TODO: just to check but unnecessary
                //todo
                .implement(dynamicTypeB.getTypeDescription()) //todo: какого-то хера перетерает - тут возвращаем
                .require(dynamicTypeA)
                .require(dynamicTypeB)
                .make()
                .include(dynamicTypeA, dynamicTypeB);
        //--- Check resulted class ----
        try (InputStream inputStream = new ByteArrayInputStream(dynamicTypeBImpl.getBytes())) {
            final ClassParser classParser = new ClassParser(inputStream, "com/cherkovskiy/code_gen/new_impl/BImpl_GEN.class");
            final JavaClass javaClass = classParser.parse();

            System.out.println(javaClass.getClassName());
        }

        //---------- генерация AImpl_GEN для проверки вызова метода
        //--------------------
        byte[] aImplAsByteArray;
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "com/cherkovskiy/code_gen/new_impl/AImpl.class")) {

            final ClassParser classParser = new ClassParser(inputStream, "");
            final JavaClass javaClass = classParser.parse();

            Multimap<String, Integer> utf8Strings = replaceTypes(
                    ImmutableMap.of(
                            "com/cherkovskiy/code_gen/new_api/A_gen_v2", B.class.getName().replace('.', '/') + "_v2_gen"
                    ),
                    javaClass
            );

            System.out.println("====================================================");
            utf8Strings.forEach((k, v) -> System.out.println(format("%d -> %s", v, k)));

            System.out.println();

//            //------------- поменять ---------
            //-----------
            //-----------
            final ClassGen classGen = new ClassGen(javaClass);

            for (Map.Entry<String, Collection<Integer>> entry : utf8Strings.asMap().entrySet()) {
                for (Integer index : entry.getValue()) {
                    classGen.getConstantPool().setConstant(index, new ConstantUtf8(entry.getKey()));
                }
            }

            classGen.removeInterface("com.cherkovskiy.code_gen.new_api.A");
            classGen.addInterface(A.class.getName() + "_v2_gen");
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                    classGen.getJavaClass().dump(dataOutputStream);  //write to byte array changed file
                }
                outputStream.flush();
                System.out.println(outputStream.toByteArray());
                aImplAsByteArray = outputStream.toByteArray();
            }
        }
        DynamicType.Unloaded<?> dynamicTypeAImpl = new ByteBuddy()
                .redefine(
                        InstrumentedType.Default.of("com.cherkovskiy.code_gen.new_impl.AImpl",
                                TypeDescription.Generic.Builder.rawType(Object.class).build(),
                                Modifier.PUBLIC),
                        ClassFileLocator.Simple.of("com.cherkovskiy.code_gen.new_impl.AImpl", aImplAsByteArray)
                )
                .name("com.cherkovskiy.code_gen.new_impl.AImpl_GEN") //TODO: just to check but unnecessary
                //todo
                .implement(dynamicTypeA.getTypeDescription()) //todo: какого-то хера перетерает - тут возвращаем
                .require(dynamicTypeA)
                .require(dynamicTypeB)
                .make()
                .include(dynamicTypeA, dynamicTypeB);

        dynamicTypeBImpl.include(dynamicTypeAImpl);
        //---------------------------------------------------------

        //-----------------------------
        InjectionClassLoader injectionClassLoader = new ByteArrayClassLoader(OnFlyGeneratorTest.class.getClassLoader(), false, Maps.newHashMap());
        DynamicType.Loaded<?> loadedClasses = dynamicTypeBImpl.load(injectionClassLoader);
        Class<?> BImpl_genClass = loadedClasses.getLoaded();
        System.out.println(BImpl_genClass);
        //--------------------

        // проверка BImpl_GEN.f1(new AImpl())
        // BImpl_GEN.f1(new AImpl_GEN())
        //--------------------
        Object bImplGenObject = BImpl_genClass.newInstance();
        BImpl_genClass.getMethod("f1", A.class).invoke(bImplGenObject, new AImpl());

        Class<?> aGenClass = loadedClasses.getLoadedAuxiliaryTypes().get(dynamicTypeA.getTypeDescription());
        Class<?> aImplGenClass = dynamicTypeAImpl.load(injectionClassLoader).getLoaded();
        Object aImplGenObject = aImplGenClass.newInstance();

        //Caused by: java.lang.IllegalAccessError: tried to access class com.cherkovskiy.code_gen.new_impl.BImpl$1
        // from class com.cherkovskiy.code_gen.new_impl.BImpl_GEN
        // Но в боевых условиях мы не будем переименовывать класс имплементации
        // Но если вдруг нужно - то обход всех классов и замена имени решит проблему
        BImpl_genClass.getMethod("f1", aGenClass).invoke(bImplGenObject, aImplGenObject);
        //--------------------

    }

    private static Multimap<String, Integer> replaceTypes(ImmutableMap<String, String> types, JavaClass javaClass) {
        Multimap<String, Integer> utf8Strings = ArrayListMultimap.create();
        for (int i = 0; i < javaClass.getConstantPool().getConstantPool().length; i++) {
            final int index = i;
            Constant constant = javaClass.getConstantPool().getConstantPool()[i];
            if (constant != null && constant.getTag() == 1) {
                String originString = ((ConstantUtf8) constant).getBytes();

                types.forEach((lookupString, replaceString) -> {
                    String changedString = originString.replaceAll(lookupString, replaceString);
                    if (!changedString.equalsIgnoreCase(originString)) {
                        utf8Strings.put(changedString, index);
                    }
                    System.out.println(format("%d -> %s", index, originString));
                });
            }
        }
        return utf8Strings;
    }
}

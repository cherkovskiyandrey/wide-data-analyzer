package com.cherkovskiy.code_gen;

import com.cherkovskiy.code_gen.api.A;
import com.cherkovskiy.code_gen.api.B;
import com.cherkovskiy.code_gen.impl.AImpl;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;


//TODO: выкинуть всё в спеку

/**
 * Для реализации новой концепции необходимо проверь генераци:
 * - интерфейс A из нового пакета в A_v2_gen и сделать его наследником от A и избавиться от всех наследников (если они явно не перегружены) и от всех оригинальных методов
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
 * * наследование на генерённый интерфейс: B_v2_gen
 * * изменение сигнатуры метода принимающего A на A_v2_gen
 * * изменение тела метода явно использующее тип A на A_v2_gen
 * * генерация перегруженного метода принимающего A и имеющее тело: L f1(A a) { return f1(new A_to_A_v2_gen_converter_gen(a)); }
 */
public class OnFlyGenerator implements Runnable {

    private final static String NEW_API_PACKAGE = "com.cherkovskiy.code_gen.new_api";
    private final static String TARGET_API_PACKAGE = "com.cherkovskiy.code_gen.api";
    private final static String NEW_IMPL_PACKAGE = "com.cherkovskiy.code_gen.new_impl";
    private final static String TARGET_IMPL_PACKAGE = "com.cherkovskiy.code_gen.impl";

    private void checkClassLoader() throws IOException, ClassNotFoundException {
        BootstrapFilteredClassLoader bootstrapFilteredClassLoader = (BootstrapFilteredClassLoader) Thread.currentThread().getContextClassLoader();
        bootstrapFilteredClassLoader.addClass(NEW_API_PACKAGE.concat(".A"));

        System.out.println(A.class.getClassLoader());
        Class<?> aFromNewApi = Class.forName(NEW_API_PACKAGE.concat(".A"));//bootstrapFilteredClassLoader.loadClass(NEW_API_PACKAGE.concat(".A"));
        System.out.println(aFromNewApi.getClassLoader());


        //BootstrapFilteredClassLoader.BASE_CLASSES_PATH
        //bootstrapFilteredClassLoader.addClass("<class name here>", new byte[0]);
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setContextClassLoader(OnFlyGenerator.class.getClassLoader());
            BootstrapFilteredClassLoader bootstrapFilteredClassLoader = (BootstrapFilteredClassLoader) Thread.currentThread().getContextClassLoader();
            //checkClassLoader();

            loadA(bootstrapFilteredClassLoader);
            loadB(bootstrapFilteredClassLoader);

            //TODO: сгененрировать equals и hashcode и проксировать их так же (clone ?)
            createAGenConvClass(bootstrapFilteredClassLoader);
            loadNewAImpl(bootstrapFilteredClassLoader);

            //TODO: генерённый метод n аргументов
            loadBImpl(bootstrapFilteredClassLoader);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNewAImpl(@Nonnull BootstrapFilteredClassLoader bootstrapFilteredClassLoader) throws Exception {
        JavaClass javaClass = Repository.lookupClass(NEW_IMPL_PACKAGE.concat(".AImpl"));
        javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".A"), TARGET_API_PACKAGE.concat(".A_generated_v2")), javaClass);

        //leave class in new package because there is the same class name in impl package
        //-------
        javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".B"), TARGET_API_PACKAGE.concat(".B")), javaClass);
        javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".C"), TARGET_API_PACKAGE.concat(".C")), javaClass);
        //-------

        ClassGen classGen = new ClassGen(javaClass);
        byte[] binaryClass = Utils.serialize(classGen.getJavaClass());
        bootstrapFilteredClassLoader.addClass(NEW_IMPL_PACKAGE.concat(".AImpl"), binaryClass);
        Repository.addClass(classGen.getJavaClass());

        testNewAImpl();
    }

    private void testNewAImpl() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        Class<?> newAGen = Class.forName(NEW_IMPL_PACKAGE.concat(".AImpl"));
        System.out.println(newAGen);

        // move new classes in worked packages
        //----
        preloadApi(".C");
        preloadImpl(".CImpl");
        //----

        Object newAGenInstance = newAGen.newInstance();
        newAGen.getMethod("f1").invoke(newAGenInstance);

        Class<?> cClass = Class.forName(TARGET_API_PACKAGE.concat(".C"));
        Class<?> cImplClass = Class.forName(TARGET_IMPL_PACKAGE.concat(".CImpl"));
        Object cImplObject = cImplClass.newInstance();
        newAGen.getMethod("f2", cClass).invoke(newAGenInstance, cImplObject);
    }

    /**
     * * - изменение класса имплементации BImpl:
     * * имя на генерённое: BImpl_v2_gen ? - повлечёт к изменению всех классов наследников и которые его использую - не стоит
     * * наследование на генерённый интерфейс: B_v2_gen
     * * изменение сигнатуры метода принимающего A на A_v2_gen
     * * изменение тела метода явно использующее тип A на A_v2_gen
     * * генерация перегруженного метода принимающего A и имеющее тело: L f1(A a) { return f1(new A_to_A_v2_gen_converter_gen(a)); }
     */
    private void loadBImpl(@Nonnull BootstrapFilteredClassLoader bootstrapFilteredClassLoader) throws Exception {
        JavaClass javaClassBImpl = Repository.lookupClass(NEW_IMPL_PACKAGE.concat(".BImpl"));

        //use target interfaces and moved classes
        // our class outside of target package
        //------------------------
        javaClassBImpl = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".L"),
                TARGET_API_PACKAGE.concat(".L")), javaClassBImpl); //create overloaded methods
        javaClassBImpl = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".C"),
                TARGET_API_PACKAGE.concat(".C")), javaClassBImpl); //create overloaded methods
        javaClassBImpl = Utils.patchPool(ImmutableMap.of(NEW_IMPL_PACKAGE.concat(".CImpl"),
                TARGET_IMPL_PACKAGE.concat(".CImpl")), javaClassBImpl); //create overloaded methods
        //------------------------

        //just to look for method which receives A
        //---------------
        javaClassBImpl = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".A"),
                TARGET_API_PACKAGE.concat(".A")), javaClassBImpl); //create overloaded methods
        javaClassBImpl = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".B"),
                TARGET_API_PACKAGE.concat(".B")), javaClassBImpl); //create overloaded methods
        Method aRecvMethod = Arrays.stream(javaClassBImpl.getMethods())
                .filter(m -> Arrays.stream(m.getArgumentTypes())
                        .anyMatch(t -> t.normalizeForStackOrLocal().toString().contains(TARGET_API_PACKAGE.concat(".A"))))
                .findAny()
                .orElse(null);
        //---------------

        javaClassBImpl = Utils.patchPool(ImmutableMap.of(TARGET_API_PACKAGE.concat(".B"),
                TARGET_API_PACKAGE.concat(".B_generated_v2")), javaClassBImpl); //create overloaded methods
        javaClassBImpl = Utils.patchPool(ImmutableMap.of(TARGET_API_PACKAGE.concat(".A"),
                TARGET_API_PACKAGE.concat(".A_generated_v2")), javaClassBImpl); //create overloaded methods


        ClassGen classGenBImpl = new ClassGen(javaClassBImpl);
        Arrays.stream(classGenBImpl.getInterfaceNames()).forEach(classGenBImpl::removeInterface);
        classGenBImpl.addInterface(TARGET_API_PACKAGE.concat(".B_generated_v2"));

        //toto: перегруженный метод приимающий A и вызываеющий прокси
        System.out.println(aRecvMethod);
        addGeneratedMethod(aRecvMethod, classGenBImpl);

        byte[] binaryClassBImpl = Utils.serialize(classGenBImpl.getJavaClass());
        bootstrapFilteredClassLoader.addClass(NEW_IMPL_PACKAGE.concat(".BImpl"), binaryClassBImpl);
        Repository.addClass(classGenBImpl.getJavaClass());


        JavaClass javaClassBImpl$1 = Repository.lookupClass(NEW_IMPL_PACKAGE.concat(".BImpl$1"));
        javaClassBImpl$1 = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".A"), TARGET_API_PACKAGE.concat(".A_generated_v2")), javaClassBImpl$1);

        //move class in new package because there is the same class name in impl package
        //-------
        javaClassBImpl$1 = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".B"), TARGET_API_PACKAGE.concat(".B_generated_v2")), javaClassBImpl$1);
        javaClassBImpl$1 = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE.concat(".C"), TARGET_API_PACKAGE.concat(".C")), javaClassBImpl$1);
        //-------

        ClassGen classGenBImpl$1 = new ClassGen(javaClassBImpl$1);
        byte[] binaryClass = Utils.serialize(classGenBImpl$1.getJavaClass());
        bootstrapFilteredClassLoader.addClass(NEW_IMPL_PACKAGE.concat(".BImpl$1"), binaryClass);
        Repository.addClass(classGenBImpl$1.getJavaClass());

        testLoadingNewBImpl();
    }

    private void testLoadingNewBImpl() throws Exception {
        Class<?> bImplClass = Class.forName(NEW_IMPL_PACKAGE.concat(".BImpl"));
        System.out.println(bImplClass);

        // move new classes in worked packages
        //----
        preloadApi(".C");
        preloadImpl(".CImpl");
        //----

        Object bImplObj = bImplClass.newInstance();
        bImplClass.getMethod("f1", A.class).invoke(bImplObj, new AImpl());
    }

    /**
     * Code(max_stack = 4, max_locals = 2, code_length = 13)
     0:    aload_0
     1:    new		<com.cherkovskiy.code_gen.new_api.covalent_return_types.A_proxy_gen_v2> (7)
     4:    dup
     5:    aload_1
     6:    invokespecial	com.cherkovskiy.code_gen.new_api.covalent_return_types.A_proxy_gen_v2.<init> (Lcom/cherkovskiy/code_gen/new_api/covalent_return_types/A;)V (8)
     9:    invokevirtual	com.cherkovskiy.code_gen.new_api.covalent_return_types.B_gen_v2_impl.simpleArg (Lcom/cherkovskiy/code_gen/new_api/covalent_return_types/A_gen_v2;)V (9)
     12:   return

     Attribute(s) =
     LineNumber(0, 21), LineNumber(12, 22)
     LocalVariable(start_pc = 0, length = 13, index = 0:com.cherkovskiy.code_gen.new_api.covalent_return_types.B_gen_v2_impl this)
     LocalVariable(start_pc = 0, length = 13, index = 1:com.cherkovskiy.code_gen.new_api.covalent_return_types.A a)
     * @param aRecvMethod
     * @param classGenBImpl
     */
    private void addGeneratedMethod(@Nonnull Method aRecvMethod, @Nonnull ClassGen classGenBImpl) {
        String converter = NEW_IMPL_PACKAGE.concat(".A_to_A_v2_gen_converter");
        String fromInterface = TARGET_API_PACKAGE.concat(".A");
        String toInterface = TARGET_API_PACKAGE.concat(".A_generated_v2");
        InstructionList instructionList = new InstructionList();
        InstructionFactory instructionFactory = new InstructionFactory(classGenBImpl);

        instructionList.append(instructionFactory.createPrintln("INVOKE FROM STUB METHOD:"));//for debug
        instructionList.append(new ALOAD(0));
        instructionList.append(instructionFactory.createNew(converter));
        instructionList.append(new DUP());
        instructionList.append(new ALOAD(1));
        instructionList.append(instructionFactory.createInvoke(converter,
                "<init>",
                Type.VOID,
                new Type[]{ObjectType.getInstance(fromInterface)},
                Const.INVOKESPECIAL)
        );
        instructionList.append(instructionFactory.createInvoke(classGenBImpl.getClassName(), //method proxy to
                aRecvMethod.getName(),
                aRecvMethod.getReturnType(),
                new Type[]{ObjectType.getInstance(toInterface)}, //todo: n argument have to be supported
                Const.INVOKEVIRTUAL)
        );
        instructionList.append(new ARETURN()); //TODO: only for void methods

        MethodGen overrideMethod = new MethodGen(
                aRecvMethod.getModifiers() & ~Modifier.ABSTRACT,
                aRecvMethod.getReturnType(),
                aRecvMethod.getArgumentTypes(),
                null,
                aRecvMethod.getName(),
                classGenBImpl.getClassName(),
                instructionList,
                classGenBImpl.getConstantPool()
        );

        if (aRecvMethod.getExceptionTable() != null) {
            Arrays.stream(aRecvMethod.getExceptionTable().getExceptionNames()).forEach(overrideMethod::addException);
        }

        overrideMethod.setMaxStack();
        overrideMethod.getMaxLocals();
        classGenBImpl.addMethod(overrideMethod.getMethod());
        instructionList.dispose();
    }


    //интерфейс A из нового пакета в A_v2_gen и сделать его наследником от A и избавиться от всех наследников
    //(если они явно не перегружены) и от всех оригинальных методов
    //так же необходимо унаследоваться вверх по графу от всех изменённых интерфейсов
    private void loadA(BootstrapFilteredClassLoader bootstrapFilteredClassLoader) throws Exception {
        JavaClass javaClass = Repository.lookupClass(NEW_API_PACKAGE.concat(".A"));
        javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE, TARGET_API_PACKAGE), javaClass);// our class outside of target package
        javaClass = Utils.patchPool(ImmutableMap.of(TARGET_API_PACKAGE.concat(".B"),
                TARGET_API_PACKAGE.concat(".B_generated_v2")), javaClass); //create overloaded methods
        ClassGen classGen = new ClassGen(javaClass);
        classGen.setClassName(TARGET_API_PACKAGE.concat(".A_generated_v2"));
        Arrays.stream(classGen.getInterfaceNames()).forEach(classGen::removeInterface);
        classGen.addInterface(A.class.getName());
        Utils.removeInheritedMethods(classGen, Repository.lookupClass(NEW_API_PACKAGE.concat(".A")));
        byte[] binaryClass = Utils.serialize(classGen.getJavaClass());
        bootstrapFilteredClassLoader.addClass(TARGET_API_PACKAGE.concat(".A_generated_v2"), binaryClass);
        Repository.addClass(classGen.getJavaClass());
        //testLoadingNewA(); //preloadApi B - bad idea

        //TODO: так же необходимо унаследоваться вверх по графу от всех изменённых интерфейсов - возможно стоит сделать в конце ?
    }


    private void testLoadingNewA() throws ClassNotFoundException, IOException {
        BootstrapFilteredClassLoader bootstrapFilteredClassLoader = (BootstrapFilteredClassLoader) Thread.currentThread().getContextClassLoader();

        //class B has to be moved to target package
        JavaClass javaClass = Repository.lookupClass(NEW_API_PACKAGE.concat(".B"));
        javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE, TARGET_API_PACKAGE), javaClass);// our class outside of target package
        byte[] binaryClass = Utils.serialize(javaClass);
        bootstrapFilteredClassLoader.addClass(TARGET_API_PACKAGE.concat(".B"), binaryClass);

        Class<?> newAGen = Class.forName(TARGET_API_PACKAGE.concat(".A_generated_v2"));
        System.out.println(newAGen);
    }


    //интерфейс A_gen_v2 в B_v2_gen с добавление перегруженного метода принимающего A_v2_gen и сделать его наследником от A_gen_v2
    //если метод не дефолтный то просто перегруженный метод добавляем
    //если метод A_gen_v2.f1 дефолтный - старый код по старому default - новый по-новому default
    private void loadB(BootstrapFilteredClassLoader bootstrapFilteredClassLoader) throws Exception {
        JavaClass javaClass = Repository.lookupClass(NEW_API_PACKAGE.concat(".B"));
        javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE, TARGET_API_PACKAGE), javaClass);// our class outside of target package
        javaClass = Utils.patchPool(ImmutableMap.of(TARGET_API_PACKAGE.concat(".A"),
                TARGET_API_PACKAGE.concat(".A_generated_v2")), javaClass); //create overloaded methods
        ClassGen classGen = new ClassGen(javaClass);
        classGen.setClassName(TARGET_API_PACKAGE.concat(".B_generated_v2"));
        Arrays.stream(classGen.getInterfaceNames()).forEach(classGen::removeInterface);
        classGen.addInterface(B.class.getName());
        Utils.removeInheritedMethods(classGen, Repository.lookupClass(NEW_API_PACKAGE.concat(".B")));

        byte[] binaryClass = Utils.serialize(classGen.getJavaClass());
        bootstrapFilteredClassLoader.addClass(TARGET_API_PACKAGE.concat(".B_generated_v2"), binaryClass);
        Repository.addClass(classGen.getJavaClass());
        //todo: поддержка женериков как тут: com.cherkovskiy.code_gen.new_api.covalent_return_types.B_gen_v2

        testLoadingNewB();
    }

    private void testLoadingNewB() throws ClassNotFoundException {
        Class<?> newAGen = Class.forName(TARGET_API_PACKAGE.concat(".B_generated_v2"));
        System.out.println(newAGen);
    }


    /**
     * * - генерация прокси класса A_to_A_v2_gen_converter_gen имплементирующего A_v2_gen:
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
     *
     * @param bootstrapFilteredClassLoader
     */
    private void createAGenConvClass(BootstrapFilteredClassLoader bootstrapFilteredClassLoader) throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String currentClassName = NEW_IMPL_PACKAGE.concat(".A_to_A_v2_gen_converter");
        String fromInterface = TARGET_API_PACKAGE.concat(".A");
        String toInterface = TARGET_API_PACKAGE.concat(".A_generated_v2");

        // move new classes in worked packages
        //----
        preloadApi(".C");
        preloadImpl(".CImpl");
        //----

        ClassGen classGen = new ClassGen(
                currentClassName,
                Object.class.getName(),
                currentClassName,
                Modifier.PUBLIC,
                new String[]{toInterface}
        );
        InstructionList instructionList = new InstructionList();

        createField(classGen, fromInterface);
        createConstructor(classGen, instructionList, currentClassName, fromInterface, toInterface);
        createProxyMethods(classGen, instructionList, currentClassName, fromInterface);
        createStubMethods(classGen, instructionList, currentClassName, toInterface);

        Repository.addClass(classGen.getJavaClass());
        byte[] binaryClass = Utils.serialize(classGen.getJavaClass());
        bootstrapFilteredClassLoader.addClass(currentClassName, binaryClass);

        testLoadingNewAGenConv();
    }

    private void createField(@Nonnull ClassGen classGen, String fromInterface) {
        //1. Create field for original object A.class
        FieldGen fieldGen = new FieldGen(Modifier.PRIVATE | Modifier.FINAL, ObjectType.getInstance(fromInterface), "original", classGen.getConstantPool());
        classGen.addField(fieldGen.getField());
    }

    private void createConstructor(ClassGen classGen, InstructionList instructionList, String currentClassName, String fromInterface, String toInterface) {
        //2. Write code which load A.class into field
        /**
         * Code(max_stack = 2, max_locals = 2, code_length = 10)
         0:    aload_0
         1:    invokespecial	java.lang.Object.<init> ()V (1)
         4:    aload_0
         5:    aload_1
         6:    putfield		com.cherkovskiy.code_gen.new_api.covalent_return_types.A_proxy_gen_v2.a Lcom/cherkovskiy/code_gen/new_api/covalent_return_types/A; (2)
         9:    return

         Attribute(s) =
         LineNumber(0, 8), LineNumber(4, 9), LineNumber(9, 10)
         LocalVariable(start_pc = 0, length = 10, index = 0:com.cherkovskiy.code_gen.new_api.covalent_return_types.A_proxy_gen_v2 this)
         LocalVariable(start_pc = 0, length = 10, index = 1:com.cherkovskiy.code_gen.new_api.covalent_return_types.A a)
         */

        InstructionFactory instructionFactory = new InstructionFactory(classGen);

        instructionList.append(new ALOAD(0));
        instructionList.append(instructionFactory.createInvoke("java.lang.Object", "<init>", Type.VOID, new Type[0], Const.INVOKESPECIAL));
        instructionList.append(new ALOAD(0));
        instructionList.append(new ALOAD(1));
        instructionList.append(instructionFactory.createPutField(currentClassName, "original", ObjectType.getInstance(fromInterface)));
        instructionList.append(new RETURN());


        //3. Create constructor
        MethodGen constructor = new MethodGen(
                Modifier.PUBLIC,
                Type.VOID, //or Type.VOID
                new Type[]{ObjectType.getInstance(fromInterface)},
                new String[]{"gen"},
                "<init>",
                toInterface,
                instructionList,
                classGen.getConstantPool()
        );

        constructor.setMaxStack();
        constructor.getMaxLocals();
        classGen.addMethod(constructor.getMethod());
        instructionList.dispose();
    }

    private void createProxyMethods(
            @Nonnull ClassGen classGen,
            @Nonnull InstructionList instructionList,
            @Nonnull String currentClassName,
            @Nonnull String fromInterface
    ) throws ClassNotFoundException {
        InstructionFactory instructionFactory = new InstructionFactory(classGen);
        JavaClass fromClass = Repository.lookupClass(fromInterface);
        Collection<Method> methodForProxy = allMethods(fromClass);

        for (Method method : methodForProxy) {
            instructionList.append(instructionFactory.createPrintln("INVOKE FROM PROXY:"));//for debug
            instructionList.append(new ALOAD(0));
            instructionList.append(instructionFactory.createGetField(currentClassName, "original", ObjectType.getInstance(fromInterface)));
            instructionList.append(instructionFactory.createInvoke(
                    fromInterface,
                    method.getName(),
                    method.getReturnType(),
                    method.getArgumentTypes(),
                    Const.INVOKEINTERFACE)
            );
            instructionList.append(new RETURN());

            MethodGen overrideMethod = new MethodGen(
                    method.getModifiers() & ~Modifier.ABSTRACT,
                    method.getReturnType(),
                    method.getArgumentTypes(),
                    null,
                    method.getName(),
                    currentClassName,
                    instructionList,
                    classGen.getConstantPool()
            );

            if (method.getExceptionTable() != null) {
                Arrays.stream(method.getExceptionTable().getExceptionNames()).forEach(overrideMethod::addException);
            }

            overrideMethod.setMaxStack();
            overrideMethod.getMaxLocals();
            classGen.addMethod(overrideMethod.getMethod());
            instructionList.dispose();
        }
    }

    private void createStubMethods(
            @Nonnull ClassGen classGen,
            @Nonnull InstructionList instructionList,
            @Nonnull String currentClassName,
            @Nonnull String toInterface
    ) throws ClassNotFoundException {
        InstructionFactory instructionFactory = new InstructionFactory(classGen);
        JavaClass fromClass = Repository.lookupClass(toInterface);

        for (Method method : fromClass.getMethods()) {
            //1. create exception object IllegalStateException
            //2. throw this object
            instructionList.append(instructionFactory.createPrintln("INVOKE FROM PROXY:"));//for debug
            instructionList.append(instructionFactory.createNew("java.lang.IllegalStateException"));
            instructionList.append(new DUP());
            instructionList.append(new PUSH(classGen.getConstantPool(), "Attempt to invoke unprovided method. Bundle which provide implementation doesn't know about this method. It is connected with not-default new interface method during hot bundle redeploy."));
            instructionList.append(instructionFactory.createInvoke(IllegalStateException.class.getName(), "<init>", Type.VOID, new Type[]{Type.getType(String.class)}, Const.INVOKESPECIAL));
            instructionList.append(new ATHROW());

            //3. Create constructor
            MethodGen f2 = new MethodGen(
                    method.getModifiers() & ~Modifier.ABSTRACT,
                    method.getReturnType(),
                    method.getArgumentTypes(),
                    null,
                    method.getName(),
                    currentClassName,
                    instructionList,
                    classGen.getConstantPool()
            );


            if (method.getExceptionTable() != null) {
                Arrays.stream(method.getExceptionTable().getExceptionNames()).forEach(f2::addException);
            }

            f2.setMaxStack();
            f2.getMaxLocals();
            classGen.addMethod(f2.getMethod());
            instructionList.dispose();
        }
    }

    private void testLoadingNewAGenConv() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        preloadApi(".C");
        preloadImpl(".CImpl");

        Class<?> newAGenConv = Class.forName(NEW_IMPL_PACKAGE.concat(".A_to_A_v2_gen_converter"));
        System.out.println(newAGenConv);
        Object wrapperGen = newAGenConv.getConstructor(A.class).newInstance(new AImpl());

        Class<?> newAGen = Class.forName(TARGET_API_PACKAGE.concat(".A_generated_v2"));
        System.out.println(newAGen.isAssignableFrom(newAGenConv));

        Object castedWrapper = newAGen.cast(wrapperGen);
        try {
            newAGen.getMethod("f1").invoke(castedWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Class<?> cClass = Class.forName(TARGET_API_PACKAGE.concat(".C"));
        Class<?> cImplClass = Class.forName(TARGET_IMPL_PACKAGE.concat(".CImpl"));
        Object cImplObject = cImplClass.newInstance();
        try {
            newAGen.getMethod("f2", cClass).invoke(castedWrapper, cImplObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preloadApi(@Nonnull String classNameRecent) throws ClassNotFoundException, IOException {
        BootstrapFilteredClassLoader bootstrapFilteredClassLoader = (BootstrapFilteredClassLoader) Thread.currentThread().getContextClassLoader();
        if (!bootstrapFilteredClassLoader.contain(TARGET_API_PACKAGE.concat(classNameRecent))) {
            JavaClass javaClass = Repository.lookupClass(NEW_API_PACKAGE.concat(classNameRecent));
            javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE, TARGET_API_PACKAGE), javaClass);// our class outside of target package
            byte[] binaryClass = Utils.serialize(javaClass);
            bootstrapFilteredClassLoader.addClass(TARGET_API_PACKAGE.concat(classNameRecent), binaryClass);
        }
    }

    private void preloadImpl(@Nonnull String classNameRecent) throws ClassNotFoundException, IOException {
        BootstrapFilteredClassLoader bootstrapFilteredClassLoader = (BootstrapFilteredClassLoader) Thread.currentThread().getContextClassLoader();
        if (!bootstrapFilteredClassLoader.contain(NEW_IMPL_PACKAGE.concat(classNameRecent))) {
            JavaClass javaClass = Repository.lookupClass(NEW_IMPL_PACKAGE.concat(classNameRecent));
            javaClass = Utils.patchPool(ImmutableMap.of(NEW_IMPL_PACKAGE, TARGET_IMPL_PACKAGE), javaClass);// our class outside of target package
            javaClass = Utils.patchPool(ImmutableMap.of(NEW_API_PACKAGE, TARGET_API_PACKAGE), javaClass);// our class outside of target package
            byte[] binaryClass = Utils.serialize(javaClass);
            bootstrapFilteredClassLoader.addClass(TARGET_IMPL_PACKAGE.concat(classNameRecent), binaryClass);
        }
    }


    private Collection<Method> allMethods(@Nonnull JavaClass fromInterface) throws ClassNotFoundException {
        ImmutableSet.Builder<Method> allMethods = ImmutableSet.builder();
        Deque<JavaClass> stack = Lists.newLinkedList();
        stack.add(fromInterface);
        while (!stack.isEmpty()) {
            JavaClass currentClass = stack.poll();
            Arrays.stream(currentClass.getMethods()).forEach(allMethods::add);
            stack.addAll(Arrays.asList(currentClass.getInterfaces()));
        }

        return allMethods.build();
    }


//    private Method simpleLookupMethodByName(@Nonnull String mostChildInterface, @Nonnull String methodName) throws ClassNotFoundException {
//        Optional<Method> f1Method = Optional.empty();
//        Deque<JavaClass> stack = Lists.newLinkedList();
//        stack.add(Repository.lookupClass(mostChildInterface));
//        while (!stack.isEmpty()) {
//            JavaClass currentClass = stack.poll();
//            f1Method = Arrays.stream(currentClass.getMethods()).filter(m -> methodName.equalsIgnoreCase(m.getName())).findAny();
//            if (f1Method.isPresent()) {
//                break;
//            }
//            stack.addAll(Arrays.asList(currentClass.getInterfaces()));
//        }
//
//        return f1Method.orElseThrow(() -> new IllegalStateException(""));
//    }
}














package com.cherkovskiy.comprehensive_serializer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.bcel.Const.CONSTANT_MethodHandle;


public class MethodScanner extends org.apache.bcel.classfile.EmptyVisitor {
    private final JavaClass javaClass;
    private final CodeVisitorImpl codeVisitor;
    private final Set<String> scannedClasses = Sets.newHashSet();
    private final boolean isDebugMode;

    public MethodScanner(JavaClass javaClass, boolean isDebugMode) {
        this.javaClass = javaClass;
        this.codeVisitor = new CodeVisitorImpl(new ConstantPoolGen(javaClass.getConstantPool()));
        this.isDebugMode = isDebugMode;
    }

    public void visitCode(Code obj) {
        final InstructionList instructionHandles = new InstructionList(obj.getCode());
        for (InstructionHandle instructionHandle : instructionHandles) {
            instructionHandle.accept(codeVisitor);
        }
    }

    public ImmutableSet<String> getScannedClasses() {
        return ImmutableSet.copyOf(scannedClasses);
    }


    public static Set<String> readAllClassesFormConstant(Constant c, ConstantPool constantPool) throws ClassFormatException {
        Set<String> classes = Sets.newHashSet();
        int i;
        final byte tag = c.getTag();
        switch (tag) {
            case Const.CONSTANT_Class:
                i = ((ConstantClass) c).getNameIndex();
                c = constantPool.getConstant(i, Const.CONSTANT_Utf8);
                classes.add(Utility.compactClassName(((ConstantUtf8) c).getBytes(), false));
                break;

            case Const.CONSTANT_String:
            case Const.CONSTANT_Utf8:
            case Const.CONSTANT_Double:
            case Const.CONSTANT_Float:
            case Const.CONSTANT_Long:
            case Const.CONSTANT_Integer:
            case Const.CONSTANT_NameAndType:
                break;
            case Const.CONSTANT_InterfaceMethodref:
            case Const.CONSTANT_Methodref:
            case Const.CONSTANT_Fieldref:
                classes.addAll(
                        readAllClassesFormConstant(
                                constantPool.getConstant(((ConstantCP) c).getClassIndex(), Const.CONSTANT_Class),
                                constantPool)
                );
                break;
            case CONSTANT_MethodHandle:
                // Note that the ReferenceIndex may point to a Fieldref, Methodref or
                // InterfaceMethodref - so we need to peek ahead to get the actual type.
                final ConstantMethodHandle cmh = (ConstantMethodHandle) c;
                classes.addAll(
                        readAllClassesFormConstant(
                                constantPool.getConstant(cmh.getReferenceIndex()),
                                constantPool)
                );
                break;
            case Const.CONSTANT_MethodType:
                break;
            case Const.CONSTANT_InvokeDynamic:
                final ConstantInvokeDynamic cid = (ConstantInvokeDynamic) c;
                classes.addAll(
                        readAllClassesFormConstant(
                                constantPool.getConstant(cid.getNameAndTypeIndex(), Const.CONSTANT_NameAndType),
                                constantPool)
                );
                break;
            default: // Never reached
                throw new RuntimeException("Unknown constant type " + tag);
        }
        return classes;
    }

    private class CodeVisitorImpl extends org.apache.bcel.generic.EmptyVisitor {
        private final ConstantPoolGen constantPoolGen;

        public CodeVisitorImpl(ConstantPoolGen constantPoolGen) {
            this.constantPoolGen = constantPoolGen;
        }

        @Override
        public void visitLocalVariableInstruction(final LocalVariableInstruction obj) {
            registerType(obj.getType(constantPoolGen), LocalVariableInstruction.class.getName());
        }


        @Override
        public void visitBranchInstruction(final BranchInstruction obj) {
            obj.getTarget().accept(this);
        }


        @Override
        public void visitLoadClass(final LoadClass obj) {
            registerType(obj.getType(constantPoolGen), LoadClass.class.getName());

            //It is not necessary - obj.getReferenceType use instead
            //registerType(obj.getLoadClassType(constantPoolGen), LoadClass.class.getName());
        }


        @Override
        public void visitFieldInstruction(final FieldInstruction obj) {
            //It is not necessary - obj.getReferenceType use instead
            //registerType(obj.getLoadClassType(constantPoolGen), FieldInstruction.class.getName());
            registerType(obj.getReferenceType(constantPoolGen), FieldInstruction.class.getName());
            registerType(obj.getFieldType(constantPoolGen), FieldInstruction.class.getName());
            registerType(obj.getType(constantPoolGen), FieldInstruction.class.getName());
        }


        @Override
        public void visitIfInstruction(final IfInstruction obj) {
            obj.getTarget().accept(this);
        }


        @Override
        public void visitConversionInstruction(final ConversionInstruction obj) {
            registerType(obj.getType(constantPoolGen), ConversionInstruction.class.getName());
        }

        @Override
        public void visitJsrInstruction(final JsrInstruction obj) {
            registerType(obj.getType(constantPoolGen), JsrInstruction.class.getName());
            obj.getTarget().accept(this);
            obj.physicalSuccessor().accept(this);
        }


        @Override
        public void visitGotoInstruction(final GotoInstruction obj) {
            //TODO: loop detective
            obj.getTarget().accept(this);
        }


        @Override
        public void visitStoreInstruction(final StoreInstruction obj) {
            registerType(obj.getType(constantPoolGen), StoreInstruction.class.getName());
        }


        @Override
        public void visitTypedInstruction(final TypedInstruction obj) {
            registerType(obj.getType(constantPoolGen), TypedInstruction.class.getName());
        }


        @Override
        public void visitSelect(final Select obj) {
            obj.getTarget().accept(this);
            Arrays.stream(obj.getTargets()).forEach(t -> t.accept(this));
        }

        @Override
        public void visitArithmeticInstruction(final ArithmeticInstruction obj) {
            registerType(obj.getType(constantPoolGen), ArithmeticInstruction.class.getName());
        }


        @Override
        public void visitCPInstruction(final CPInstruction obj) {
            registerType(obj.getType(constantPoolGen), CPInstruction.class.getName());
        }


        @Override
        public void visitInvokeInstruction(final InvokeInstruction obj) {
            //obj.getClassName(constantPoolGen) is unnecessary - use obj.getReferenceType instead
            registerType(obj.getReferenceType(constantPoolGen), InvokeInstruction.class.getName());
            Arrays.stream(obj.getArgumentTypes(constantPoolGen)).forEach(t -> registerType(t, InvokeInstruction.class.getName()));

            registerType(obj.getReturnType(constantPoolGen), InvokeInstruction.class.getName());
            registerType(obj.getType(constantPoolGen), InvokeInstruction.class.getName());

            //It is not necessary - obj.getReferenceType use instead
            //registerType(obj.getLoadClassType(constantPoolGen), InvokeInstruction.class.getName());
        }


        @Override
        public void visitArrayInstruction(final ArrayInstruction obj) {
            registerType(obj.getType(constantPoolGen), ArrayInstruction.class.getName());
        }

        @Override
        public void visitReturnInstruction(final ReturnInstruction obj) {
            registerType(obj.getType(), ReturnInstruction.class.getName());
            registerType(obj.getType(constantPoolGen), ReturnInstruction.class.getName());
        }


        @Override
        public void visitFieldOrMethod(final FieldOrMethod obj) {
            //It is not necessary - obj.getReferenceType use instead
            //registerType(obj.getLoadClassType(constantPoolGen), FieldOrMethod.class.getName());
            registerType(obj.getReferenceType(constantPoolGen), FieldOrMethod.class.getName());
            registerType(obj.getType(constantPoolGen), FieldOrMethod.class.getName());
        }


        @Override
        public void visitConstantPushInstruction(final ConstantPushInstruction obj) {
            registerType(obj.getType(constantPoolGen), ConstantPushInstruction.class.getName());
        }

        @Override
        public void visitLoadInstruction(final LoadInstruction obj) {
            registerType(obj.getType(constantPoolGen), ReturnInstruction.class.getName());
        }

        @Override
        public void visitINVOKEDYNAMIC(final INVOKEDYNAMIC obj) {
            final ConstantPool constantPool = constantPoolGen.getFinalConstantPool();
            final ConstantInvokeDynamic invokeDynamic = (ConstantInvokeDynamic) constantPool.getConstant(obj.getIndex(), Const.CONSTANT_InvokeDynamic);

            for (Attribute attribute : javaClass.getAttributes()) {
                final byte tag = attribute.getTag();
                if (tag == Const.ATTR_BOOTSTRAP_METHODS) {
                    final BootstrapMethod bootstrapMethod = ((BootstrapMethods) attribute).getBootstrapMethods()[invokeDynamic.getBootstrapMethodAttrIndex()];
                    final ConstantMethodHandle constantMethodHandle = (ConstantMethodHandle) constantPool.getConstant(bootstrapMethod.getBootstrapMethodRef(), CONSTANT_MethodHandle);
                    final Set<String> fromMethod = readAllClassesFormConstant(constantMethodHandle, constantPool);

                    scannedClasses.addAll(fromMethod);
                    if (isDebugMode) {
                        fromMethod.forEach(t -> System.out.println(INVOKEDYNAMIC.class.getName() + " ==>>> " + t));
                    }

                    final Set<String> fromArgs = Arrays.stream(bootstrapMethod.getBootstrapArguments())
                            .mapToObj(constantPool::getConstant)
                            .flatMap(c -> readAllClassesFormConstant(c, constantPool).stream())
                            .collect(Collectors.toSet());

                    scannedClasses.addAll(fromArgs);
                    if (isDebugMode) {
                        fromArgs.forEach(t -> System.out.println(INVOKEDYNAMIC.class.getName() + " ==>>> " + t));
                    }

                    break;
                }
            }
        }

        private void registerType(Type type, String message) {
            if (type == null) {
                return;
            }
            if (type instanceof ArrayType) {
                registerType(((ArrayType) type).getBasicType(), message);

            } else if (type instanceof ObjectType) {
                scannedClasses.add(((ObjectType) type).getClassName());
                if (isDebugMode) {
                    System.out.println(message + " ==>>> " + ((ObjectType) type).getClassName());
                }

            } else if (type instanceof ReturnaddressType) {
                ((ReturnaddressType) type).getTarget().accept(this); //todo: test

            } else if (type instanceof UninitializedObjectType) {
                registerType(((UninitializedObjectType) type).getInitialized(), message); //todo: test

            }
        }
    }
}

package com.cherkovskiy.code_gen.new_api.covalent_return_types;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface B_gen_v2 extends B {

    //simple value
    String simpleArg(String str, A_gen_v2 a, Collection<Object> obj);

    A_gen_v2 covalent();

    //default method - оставляем как есть
    default void defMethod(A_gen_v2 a) {
        //System.out.println(a.method());
        System.out.println(a.newMethod());
    }

    //standard collections
    void argListMethod_gen_v2(List<A_gen_v2> listOfA); //todo: меняем название метода

    List<A_gen_v2> retListMethod_gen_v2(); //todo: меняем название метода

    //composite complex generic types
    void compositeGenericTypes_gen_v2(Map<UnknownType<A_gen_v2>, A_gen_v2> unknownTypeAMap);

    //read or write generics
    void extendedGenerics_gen_v2(Collection<? extends A_gen_v2> collection); //todo: меняем название метода

    void extendedWriteGenerics_gen_v2(Collection<? super A_gen_v2> collection); //todo: меняем название метода


    //TODO: остальные методы из B
}

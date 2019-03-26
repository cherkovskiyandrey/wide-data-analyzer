package com.cherkovskiy.code_gen.new_api.covalent_return_types;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface B {

    //simple value
    String simpleArg(String str, A a, Collection<Object> obj);

    A covalent();

    //default method
    default void defMethod(A a) {
        System.out.println(a.method());
    }

    //standard collections
    void argListMethod(List<A> listOfA);

    List<A> retListMethod();

    //unknown generic types
    void unknownGenericTypes(UnknownType<A> aUnknownType);

    UnknownType<A> retUnknownGenericTypes();

    //composite complex generic types
    void compositeGenericTypes(Map<UnknownType<A>, A> unknownTypeAMap);

    //read or write generics
    void extendedReadGenerics(Collection<? extends A> collection);

    void extendedWriteGenerics(Collection<? super A> collection);
}

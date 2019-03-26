package com.cherkovskiy.code_gen.api;

import java.util.Collection;

public interface B {
    L f1(String str, A a, Collection<Object> obj);
    //TODO
//    default L f1(A a) {
//        System.out.println("A_gen_v2#f1: ");
//        a.f1();
//        return null;
//    }
}

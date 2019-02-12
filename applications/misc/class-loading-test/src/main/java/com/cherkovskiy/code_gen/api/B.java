package com.cherkovskiy.code_gen.api;

public interface B {
    default L f1(A a) {
        System.out.println("B#f1: ");
        a.f1();
        return null;
    }
}

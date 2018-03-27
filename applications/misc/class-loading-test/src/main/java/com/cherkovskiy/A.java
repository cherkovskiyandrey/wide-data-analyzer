package com.cherkovskiy;

public class A {

    private final B b = new B();

//    public A() {
//        System.out.println(b.foo(15));
//    }

    B getB() {
        return b;
    }
}

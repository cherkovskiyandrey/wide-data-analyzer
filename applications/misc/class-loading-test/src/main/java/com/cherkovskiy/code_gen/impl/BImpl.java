package com.cherkovskiy.code_gen.impl;

import com.cherkovskiy.code_gen.api.A;
import com.cherkovskiy.code_gen.api.B;
import com.cherkovskiy.code_gen.api.L;

public class BImpl implements B {
    @Override
    public L f1(A a) {
        //TODO: создание локального объекта A нужно проверить
        a.f1();
        A otherA = new A() {
            @Override
            public void f1() {
                System.out.println("from f1()");
            }
        };
        otherA.f1();
        return null;
    }
}

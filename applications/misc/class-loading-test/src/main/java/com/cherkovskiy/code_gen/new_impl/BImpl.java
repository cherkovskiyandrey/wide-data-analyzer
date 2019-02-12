package com.cherkovskiy.code_gen.new_impl;

import com.cherkovskiy.code_gen.new_api.A;
import com.cherkovskiy.code_gen.new_api.B;
import com.cherkovskiy.code_gen.new_api.C;
import com.cherkovskiy.code_gen.new_api.L;

public class BImpl implements B {
    @Override
    public L f1(A a) {
        //TODO: создание локального объекта A нужно проверить
        a.f1();
        a.f2(new CImpl());
        a.f3(this);
        A otherA = new A() {
            @Override
            public void f1() {
                System.out.println("from f1()");
            }

            @Override
            public void f2(C c) {
                System.out.println("from f2(C c)");
            }

            @Override
            public void f3(B b) {
                System.out.println("from f3(B b)");
            }
        };
        otherA.f1();
        otherA.f2(new CImpl());
        otherA.f3(this);
        return null;
    }
}

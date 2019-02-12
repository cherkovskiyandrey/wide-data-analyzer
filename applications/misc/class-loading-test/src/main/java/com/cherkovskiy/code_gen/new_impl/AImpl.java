package com.cherkovskiy.code_gen.new_impl;

import com.cherkovskiy.code_gen.new_api.A;
import com.cherkovskiy.code_gen.new_api.B;
import com.cherkovskiy.code_gen.new_api.C;

public class AImpl implements A {
    @Override
    public void f1() {
        System.out.println("AImpl#f1");
    }

    @Override
    public void f2(C c) {
        System.out.println("AImpl#f2");
    }

    @Override
    public void f3(B b) {
        System.out.println("AImpl#f3");
    }
}

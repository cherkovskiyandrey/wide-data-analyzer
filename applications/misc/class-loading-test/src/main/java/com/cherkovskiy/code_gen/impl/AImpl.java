package com.cherkovskiy.code_gen.impl;

import com.cherkovskiy.code_gen.api.A;

public class AImpl implements A {
    @Override
    public void f1() {
        System.out.println(AImpl.class.getName() + "#f1()");
    }
}

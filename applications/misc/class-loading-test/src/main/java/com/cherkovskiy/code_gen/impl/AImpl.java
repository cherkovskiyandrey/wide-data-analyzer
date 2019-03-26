package com.cherkovskiy.code_gen.impl;

import com.cherkovskiy.code_gen.api.A;

import java.io.IOException;

public class AImpl implements A {
    @Override
    public void f1() throws IOException {
        System.out.println(AImpl.class.getName() + "#f1()");
        throw new IOException("Test");
    }
}

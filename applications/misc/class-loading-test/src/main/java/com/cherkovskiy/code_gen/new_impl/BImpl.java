package com.cherkovskiy.code_gen.new_impl;

import com.cherkovskiy.code_gen.new_api.A;
import com.cherkovskiy.code_gen.new_api.B;
import com.cherkovskiy.code_gen.new_api.C;
import com.cherkovskiy.code_gen.new_api.L;

import java.util.Collection;

public class BImpl implements B {
    @Override
    public L f1(String str, A a, Collection<Object> obj) {
        //TODO: создание локального объекта A нужно проверить
        try {
            a.f1();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            a.f2(new CImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            a.f3(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        A otherA = new A() {
            @Override
            public void f1() {
                System.out.println("from f1()");
            }

            @Override
            public void f2(C c) {
                System.out.println("from f2(B c)");
            }

            @Override
            public void f3(B b) {
                System.out.println("from f3(A_gen_v2 b)");
            }
        };
        try {
            otherA.f1();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            otherA.f2(new CImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            otherA.f3(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

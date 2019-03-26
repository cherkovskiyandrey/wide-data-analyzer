package com.cherkovskiy.code_gen;

import com.cherkovskiy.code_gen.new_api.covalent_return_types.A_proxy_gen_v2;
import com.cherkovskiy.code_gen.new_api.covalent_return_types.B_gen_v2_impl;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

public class Analyzer {

    public static void main(String[] args) throws ClassNotFoundException {
        JavaClass javaClass = Repository.lookupClass(B_gen_v2_impl.class);

        System.out.println(javaClass);
    }
}

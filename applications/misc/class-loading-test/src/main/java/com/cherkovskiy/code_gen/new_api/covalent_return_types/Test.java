package com.cherkovskiy.code_gen.new_api.covalent_return_types;

public class Test {
    public static void main(String[] args) {

        System.out.println(B.class);
        System.out.println(B_gen_v2.class);

        B_gen_v2 c_gen_v2 = new B_gen_v2_impl();
        A_gen_v2 agenv2 = c_gen_v2.covalent(); //new code
        System.out.println(agenv2.newMethod());
        c_gen_v2.defMethod(agenv2);

        B b = c_gen_v2;
        A a = b.covalent(); //old code
        //System.out.println(a.method());
        b.defMethod(a);
    }
}

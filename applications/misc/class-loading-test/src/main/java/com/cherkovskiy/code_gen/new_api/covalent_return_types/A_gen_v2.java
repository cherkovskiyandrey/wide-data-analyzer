package com.cherkovskiy.code_gen.new_api.covalent_return_types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface A_gen_v2 extends A {
    @Nonnull
    String newMethod();

    //good practice for callback methods if you want to be reloadable in prev version
    @Nullable
    default String newMethodDefault() {
        return null;
    }

    @Nullable
    String newMethodNullable();
}

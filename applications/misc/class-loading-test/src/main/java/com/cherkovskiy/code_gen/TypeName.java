package com.cherkovskiy.code_gen;

import java.lang.reflect.Type;

public class TypeName implements Type {
    private final String typeName;
    TypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }
}

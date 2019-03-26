package com.cherkovskiy.code_gen.new_api.covalent_return_types;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class A_proxy_gen_v2 implements A_gen_v2 {
    private final A a;

    public A_proxy_gen_v2(A a) {
        this.a = a;
    }

    @Nonnull
    @Override
    public String newMethod() {
        //TODO: если результат nonnull - кидаем исключение, если можно вернуть null - вернём null, если метод дефолтный - не перегружаем его
        throw new UnsupportedOperationException("Attempt to invoke unprovided method. " +
                "Bundle which provide implementation doesn't know about this method. " +
                "It is connected with not-default new interface method during hot bundle redeploy.");
    }

    @Override
    public String newMethodNullable() {
        return null;
    }

    @Override
    public Collection<String> method(String arg1, List<LocalDateTime> times, int num) {
        return a.method(arg1, times, num);
    }

    @Override
    public int hashCode() {
        return a.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return a.equals(o);
    }

    @Override
    public String toString() {
        return a.toString();
    }
}

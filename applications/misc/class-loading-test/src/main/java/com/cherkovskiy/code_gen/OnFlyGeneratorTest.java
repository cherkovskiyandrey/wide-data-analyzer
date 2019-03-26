package com.cherkovskiy.code_gen;


import java.io.IOException;

// решаемая задача - позволить перегружать бандлы с изменённым апи где поддерживается только расширение в виде пока что
// добавления методов
public class OnFlyGeneratorTest {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {

        //Don't allow to load system loader classes from this project: just child first loader
        BootstrapFilteredClassLoader bootstrapFilteredClassLoader = new BootstrapFilteredClassLoader();
        bootstrapFilteredClassLoader.addClass("com.cherkovskiy.code_gen.OnFlyGenerator");

        @SuppressWarnings("unchecked")
        Class<? extends Runnable> generatorClass = (Class<? extends Runnable>) bootstrapFilteredClassLoader.loadClass("com.cherkovskiy.code_gen.OnFlyGenerator");
        generatorClass.newInstance().run();
    }
}

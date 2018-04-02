package com.cherkovskiy.comprehensive_serializer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeserializerTest {

    public static void main(String[] args) throws Exception {

        try (InputStream inputStream = Files.newInputStream(Paths.get("full.eser"))) {
//            TestInterface testInterface = Deserializer.deserializeFrom(inputStream, TestInterface.class);
//            testInterface.foo();
//            System.out.println(testInterface.foo());
//
//


            //TODO: разработать отдельный пакет с API и отдельный с реализацией.
            //Проработаь хорошо API с учётом всех хотелок что тут есть




//            Api api = Deserializer.deserializeFrom(inputStream, Api.class);
//            System.out.println(api.activate(1.));

        }
    }

}

package com.cherkovskiy.comprehensive_serializer;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;


class HeaderBuilder {
    public static final int MAGIC_WORD = 0xABADBABE;
    private static short VERSION = 1;

    private String coreClassName;
    private int coreCodeLength = 0;
    private int coreContentLength = 0;
    private final LinkedHashMap<String, Integer> euxClasses = Maps.newLinkedHashMap();

    byte[] build() {
        if (StringUtils.isBlank(coreClassName)) {
            throw new IllegalArgumentException("Name of core class must be not empty!");
        }

        int arraySize = 4                                                                                         // 4 байт - magic word
                + 2                                                                                               // 2 байта - версия
                + 4                                                                                               // 4 байта - кол-во классов в заголовке
                + 2                                                                                               // 2 байта - размер название основного класса в UTF8 - B
                + coreClassName.getBytes(StandardCharsets.UTF_8).length                                           // B байт - название основого класса в UTF8
                + 4                                                                                               // 4 байт - размер байткода основного класса (идёт сразу же после заголовка)
                + euxClasses.size() * 2                                                                           // 2 байта - размер название 1 вспомогательного класса в UTF8 - A1
                + euxClasses.size() * 4                                                                           // 4 байт - размер байткода 1 вспомогательного класса (идёт сразу же за основным класом)
                + euxClasses.keySet().stream().mapToInt(n -> n.getBytes(StandardCharsets.UTF_8).length).sum();    // A1 байт - название 1 вспомогательного класса в UTF8

        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[arraySize]);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        byteBuffer.putInt(MAGIC_WORD);                                                                            // 4 байт - magic word
        byteBuffer.putShort(VERSION);                                                                             // 2 байта - версия
        byteBuffer.putInt(euxClasses.size());                                                                     // 4 байта - кол-во вспомогательных классов в заголовке
        byteBuffer.putShort((short) coreClassName.getBytes(StandardCharsets.UTF_8).length);                       // 2 байта - размер название основного класса в UTF8 - B
        byteBuffer.put(coreClassName.getBytes(StandardCharsets.UTF_8));                                           // B байт - название основого класса в UTF8
        byteBuffer.putInt(coreCodeLength);                                                                        // 4 байт - размер байткода основного класса (идёт сразу же после заголовка)

        euxClasses.forEach((n, s) -> {
            byteBuffer.putShort((short) n.getBytes(StandardCharsets.UTF_8).length);                               // 2 байта - размер название 1 вспомогательного класса в UTF8 - A1
            byteBuffer.put(n.getBytes(StandardCharsets.UTF_8));                                                   // A1 байт - название 1 вспомогательного класса в UTF8
            byteBuffer.putInt(s);                                                                                 // 4 байт - размер байткода 1 вспомогательного класса (идёт сразу же за основным класом)
        });

        return byteBuffer.array();
    }

    void setCoreClass(String className, int codeLength, int contentLength) {
        this.coreClassName = className;
        this.coreCodeLength = codeLength;
        this.coreContentLength = contentLength;
    }

    void addAuxClass(String className, int codeLength) {
        euxClasses.put(className, codeLength);
    }
}

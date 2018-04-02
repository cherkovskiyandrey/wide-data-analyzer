package com.cherkovskiy.comprehensive_serializer;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Parser {
    public static final int MAGIC_WORD = 0xABADBABE;
    private static short VERSION = 1;

    public static ClassPresenter parse(InputStream from) throws IOException {
        final byte[] content = IOUtils.toByteArray(from);
        final ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        final LinkedHashMap<String, Integer> clsNameToSize = parseHeader(byteBuffer);

        boolean isCoreClass = true;
        final ClassPresenter.Builder builder = new ClassPresenter.Builder();

        for (Map.Entry<String, Integer> stringIntegerEntry : clsNameToSize.entrySet()) {
            byte[] clsCode = new byte[stringIntegerEntry.getValue()];
            byteBuffer.get(clsCode);

            if (isCoreClass) {
                builder.setCoreClass(stringIntegerEntry.getKey(), clsCode);
                isCoreClass = false;
            } else {
                builder.addAuxClass(stringIntegerEntry.getKey(), clsCode);
            }
        }

        byte[] clsContent = new byte[content.length - byteBuffer.position()];
        byteBuffer.get(clsContent);
        builder.setCoreContent(clsContent);

        return builder.build();
    }

    private static LinkedHashMap<String, Integer> parseHeader(ByteBuffer byteBuffer) throws StreamCorruptedException {
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        int magicWord = byteBuffer.getInt();
        if (magicWord != MAGIC_WORD) {
            throw new StreamCorruptedException("Unknown format");
        }

        short version = byteBuffer.getShort();
        if (version != VERSION) {
            throw new StreamCorruptedException(String.format("Unsupported version %d. Current supported version: %d", version, VERSION));
        }

        //final LinkedHashMap<String, Integer> clsNameToSize = Maps.newLinkedHashMap();
        final LinkedHashMap<String, Integer> clsNameToSize = new LinkedHashMap<>();
        int auxClassAmount = byteBuffer.getInt();
        for (int i = 0; i < auxClassAmount + 1; i++) {
            short clsNameSize = byteBuffer.getShort();
            byte[] clsName = new byte[clsNameSize];
            byteBuffer.get(clsName);
            int clsSize = byteBuffer.getInt();

            clsNameToSize.put(new String(clsName, StandardCharsets.UTF_8), clsSize);
        }

        return clsNameToSize;
    }
}










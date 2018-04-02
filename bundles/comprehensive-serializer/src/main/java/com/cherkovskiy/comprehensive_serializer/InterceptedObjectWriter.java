package com.cherkovskiy.comprehensive_serializer;

import com.google.common.collect.Sets;

import java.io.*;
import java.util.Collection;
import java.util.Set;

class InterceptedObjectWriter extends ObjectOutputStream {
    private final ByteArrayOutputStream outputStream;
    private final Set<Class<?>> classes = Sets.newHashSet();

    private InterceptedObjectWriter(ByteArrayOutputStream outputStream) throws IOException {
        super(outputStream);
        this.outputStream = outputStream;
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass objectStreamClass) throws IOException {
        super.writeClassDescriptor(objectStreamClass);
        classes.add(objectStreamClass.forClass());
    }


    public static <T extends Serializable> InterceptedObjectWriter write(T object) throws IOException {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
             final InterceptedObjectWriter interceptedObjectWriter = new InterceptedObjectWriter(byteArrayOutputStream)) {

            interceptedObjectWriter.writeObject(object);
            return interceptedObjectWriter;
        }
    }

    public byte[] getContent() {
        return outputStream.toByteArray();
    }

    public Collection<Class<?>> getRuntimeClasses() {
        return classes;
    }
}

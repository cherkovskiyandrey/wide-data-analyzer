package com.cherkovskiy.comprehensive_serializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class ClassPresenter {
    private final String rootClassName;
    //private final ImmutableMap<String, byte[]> classNameToContent;
    private final Map<String, byte[]> classNameToContent;
    private final byte[] object;

    //public ClassPresenter(String rootClassName, ImmutableMap<String, byte[]> classNameToContent, byte[] object) {
    public ClassPresenter(String rootClassName, Map<String, byte[]> classNameToContent, byte[] object) {
        this.rootClassName = rootClassName;
        this.classNameToContent = classNameToContent;
        this.object = object;
    }

    public InputStream getObjectAsStream() {
        return new ByteArrayInputStream(object);
    }

    public String getRootObjectName() {
        return rootClassName;
    }

    public boolean contain(String className) {
        return classNameToContent.containsKey(className);
    }

    public byte[] getClassByName(String className) {
        return classNameToContent.get(className);
    }

    static class Builder {
        private String rootClassName;
        //private Map<String, byte[]> classNameToContent = Maps.newHashMap();
        private Map<String, byte[]> classNameToContent = new HashMap<>();
        private byte[] coreContent;

        public Builder setCoreClass(String key, byte[] clsContent) {
            this.classNameToContent.put(key, clsContent);
            this.rootClassName = key;
            return this;
        }

        public Builder addAuxClass(String key, byte[] clsContent) {
            this.classNameToContent.put(key, clsContent);
            return this;
        }

        public Builder setCoreContent(byte[] coreContent) {
            this.coreContent = coreContent;
            return this;
        }

        public ClassPresenter build() {
            //return new ClassPresenter(rootClassName, ImmutableMap.<String, byte[]>builder().putAll(classNameToContent).build(), coreContent);
            return new ClassPresenter(rootClassName, classNameToContent, coreContent);
        }
    }
}

package com.cherkovskiy.vfs;

public class BaseAttributesImpl implements Attributes {
    private final Integer mode;
    private final String owner;
    private final String group;
    private final Object extraAttributes;

    public BaseAttributesImpl(Integer mode, String owner, String group) {
        this.mode = mode;
        this.owner = owner;
        this.group = group;
        this.extraAttributes = null;
    }

    public <T> BaseAttributesImpl(Integer mode, String owner, String group, T extraAttributes) {
        this.mode = mode;
        this.owner = owner;
        this.group = group;
        this.extraAttributes = extraAttributes;
    }

    @Override
    public Integer getUnixMode() {
        return mode;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtraAttributesAs(Class<T> token) {
        if (extraAttributes != null && token.isAssignableFrom(extraAttributes.getClass())) {
            return (T) extraAttributes;
        }
        return null;
    }

    @Override
    public String toString() {
        return "BaseAttributesImpl{" +
                "mode=" + mode +
                ", owner='" + owner + '\'' +
                ", group='" + group + '\'' +
                (extraAttributes != null ? (", extraAttributes='" + extraAttributes.toString()) : "") +
                '}';
    }
}

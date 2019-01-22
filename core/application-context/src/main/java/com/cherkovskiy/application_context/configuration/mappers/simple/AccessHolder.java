package com.cherkovskiy.application_context.configuration.mappers.simple;

import java.lang.reflect.AccessibleObject;

class AccessHolder implements AutoCloseable {
    private final AccessibleObject accessibleObject;

    AccessHolder(AccessibleObject accessibleObject) {
        this.accessibleObject = accessibleObject;
        try {
            accessibleObject.setAccessible(true);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    AccessibleObject getObject() {
        return accessibleObject;
    }

    @Override
    public void close() throws Exception {
        accessibleObject.setAccessible(false);
    }
}

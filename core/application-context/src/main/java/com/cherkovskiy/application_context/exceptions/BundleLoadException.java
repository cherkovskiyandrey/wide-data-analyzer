package com.cherkovskiy.application_context.exceptions;

public class BundleLoadException extends RuntimeException {
    public BundleLoadException(Throwable throwable) {
        super(throwable);
    }
}

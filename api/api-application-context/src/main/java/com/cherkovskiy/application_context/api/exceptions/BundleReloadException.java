package com.cherkovskiy.application_context.api.exceptions;

public class BundleReloadException extends Exception {
    public BundleReloadException(String s) {
        super(s);
    }

    public BundleReloadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}

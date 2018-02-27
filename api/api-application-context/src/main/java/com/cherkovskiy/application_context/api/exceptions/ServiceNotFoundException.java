package com.cherkovskiy.application_context.api.exceptions;

public class ServiceNotFoundException extends Exception {
    public ServiceNotFoundException() {
    }

    public ServiceNotFoundException(String s) {
        super(s);
    }

    public ServiceNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }
}

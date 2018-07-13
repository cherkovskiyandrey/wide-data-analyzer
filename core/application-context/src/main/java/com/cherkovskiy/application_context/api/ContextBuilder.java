package com.cherkovskiy.application_context.api;

public interface ContextBuilder {
    
    ContextBuilder setArguments(String[] args);

    ApplicationContext build();
}

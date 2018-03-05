package com.cherkovskiy.application_context;


import com.cherkovskiy.application_context.api.ApplicationContext;

public class ApplicationContextHolder {

    private static ApplicationContext applicationContext;

    public static ApplicationContext currentContext() {
        return applicationContext;
    }

    void setCurrentContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

}

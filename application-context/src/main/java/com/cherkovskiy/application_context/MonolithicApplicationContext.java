package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.ServiceVersion;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;

public class MonolithicApplicationContext implements ApplicationContext {


    //TODO: как передать параметры?
    public MonolithicApplicationContext(String[] args) {

    }

    @Override
    public <T> T getService(Class<T> clsToken) throws ServiceNotFoundException {
        //todo
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public <T> T getService(Class<T> clsToken, ServiceVersion serviceVersion) throws ServiceNotFoundException {
        //todo
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public <T> T getService(Class<T> clsToken, String serviceName) throws ServiceNotFoundException {
        //todo
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public <T> T getService(Class<T> clsToken, String serviceName, ServiceVersion serviceVersion) throws ServiceNotFoundException {
        //todo
        throw new UnsupportedOperationException("It is not supported yet.");
    }
}

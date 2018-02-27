package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;

public interface ApplicationContext {

    <T> T getService(Class<T> clsToken) throws ServiceNotFoundException;

    <T> T getService(Class<T> clsToken, ServiceVersion serviceVersion) throws ServiceNotFoundException;

    <T> T getService(Class<T> clsToken, String serviceName) throws ServiceNotFoundException;

    <T> T getService(Class<T> clsToken, String serviceName, ServiceVersion serviceVersion) throws ServiceNotFoundException;
}

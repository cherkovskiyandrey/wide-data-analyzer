package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static com.cherkovskiy.gradle.plugin.ServiceDescriptorImpl.*;
import static junit.framework.TestCase.assertEquals;

public class ServiceDescriptionTest {

    private final ServiceDescriptorImpl serviceDescription = ServiceDescriptorImpl.builder()
            .setServiceImplName(Object.class.getName())
            .setServiceName("SOME_NAME")
            .setInitType(Service.InitType.LAZY)
            .setLifecycleType(Service.LifecycleType.PROTOTYPE)
            .addInterface(Runnable.class.getName(), ServiceDescriptorImpl.AccessType.PRIVATE)
            .addInterface(Serializable.class.getName(), ServiceDescriptorImpl.AccessType.PUBLIC)
            .build();

    @Test
    public void toFromManifestTest() throws IOException {
        final String str = serviceDescription.toManifestString();
        final ServiceDescriptorImpl serviceDescription2 = ServiceDescriptorImpl.fromManifestString(str);
//
//        Files.write(Paths.get("lllllll.test"), str.getBytes(StandardCharsets.UTF_8));

        assertEquals(serviceDescription, serviceDescription2);
    }

    @Test
    public void fromManifestStrTest() {
        String manifestStr = SERVICE_CLASS + Object.class.getName() + "," +
                SERVICE_NAME + "SOME_NAME," +
                TYPE + Service.LifecycleType.PROTOTYPE + "," +
                INIT_TYPE + Service.InitType.LAZY + "," +
                INTERFACES + "[" +

                CLASS + Serializable.class.getName() + "," +
                ACCESS_TYPE + ServiceDescriptorImpl.AccessType.PUBLIC + "," +

                CLASS + Runnable.class.getName() + "," +
                ACCESS_TYPE + ServiceDescriptorImpl.AccessType.PRIVATE + "," +
                "]";

        final ServiceDescriptorImpl serviceDescription2 = ServiceDescriptorImpl.fromManifestString(manifestStr);
        assertEquals(serviceDescription, serviceDescription2);
    }

}
package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static com.cherkovskiy.gradle.plugin.ServiceDescriptor.*;
import static junit.framework.TestCase.assertEquals;

public class ServiceDescriptionTest {

    private final ServiceDescriptor serviceDescription = ServiceDescriptor.builder()
            .setServiceImplName(Object.class.getName())
            .setServiceName("SOME_NAME")
            .setInitType(Service.InitType.LAZY)
            .setType(Service.Type.PROTOTYPE)
            .addInterface(Runnable.class.getName(), ServiceDescriptor.AccessType.PRIVATE)
            .addInterface(Serializable.class.getName(), ServiceDescriptor.AccessType.PUBLIC)
            .build();

    @Test
    public void toFromManifestTest() throws IOException {
        final String str = serviceDescription.toManifestCompatibleString();
        final ServiceDescriptor serviceDescription2 = ServiceDescriptor.fromManifestString(str);
//
//        Files.write(Paths.get("lllllll.test"), str.getBytes(StandardCharsets.UTF_8));

        assertEquals(serviceDescription, serviceDescription2);
    }

    @Test
    public void fromManifestStrTest() {
        String manifestStr = SERVICE_IMPL_NAME + Object.class.getName() + "," +
                SERVICE_NAME + "SOME_NAME," +
                TYPE + Service.Type.PROTOTYPE + "," +
                INIT_TYPE + Service.InitType.LAZY + "," +
                INTERFACES + "[" +

                CLASS + Serializable.class.getName() + "," +
                ACCESS_TYPE + ServiceDescriptor.AccessType.PUBLIC + "," +

                CLASS + Runnable.class.getName() + "," +
                ACCESS_TYPE + ServiceDescriptor.AccessType.PRIVATE + "," +
                "]";

        final ServiceDescriptor serviceDescription2 = ServiceDescriptor.fromManifestString(manifestStr);
        assertEquals(serviceDescription, serviceDescription2);
    }

}
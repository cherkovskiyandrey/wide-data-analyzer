package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.gradle.plugin.api.ServiceDescriptor;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static com.cherkovskiy.gradle.plugin.ManifestServiceDescriptor.*;
import static junit.framework.TestCase.assertEquals;

public class ServiceDescriptionTest {

    private final ManifestServiceDescriptor serviceDescription = ManifestServiceDescriptor.builder()
            .setServiceImplName(Object.class.getName())
            .setServiceName("SOME_NAME")
            .setInitType(Service.InitType.LAZY)
            .setLifecycleType(Service.LifecycleType.PROTOTYPE)
            .addInterface(Runnable.class.getName(), ManifestServiceDescriptor.AccessType.PRIVATE)
            .addInterface(Serializable.class.getName(), ManifestServiceDescriptor.AccessType.PUBLIC)
            .build();

    @Test
    public void toFromManifestTest() throws IOException {
        final String str = ManifestServiceDescriptor.toManifestString(serviceDescription);
        final ServiceDescriptor serviceDescription2 = ManifestServiceDescriptor.fromManifestString(str).iterator().next();
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
                ACCESS_TYPE + ManifestServiceDescriptor.AccessType.PUBLIC + "," +

                CLASS + Runnable.class.getName() + "," +
                ACCESS_TYPE + ManifestServiceDescriptor.AccessType.PRIVATE + "," +
                "]";

        final ServiceDescriptor serviceDescription2 = ManifestServiceDescriptor.fromManifestString(manifestStr).iterator().next();
        assertEquals(serviceDescription, serviceDescription2);
    }

}
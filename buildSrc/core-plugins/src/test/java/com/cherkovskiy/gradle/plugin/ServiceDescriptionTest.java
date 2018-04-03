package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static com.cherkovskiy.gradle.plugin.ServiceDescription.*;
import static junit.framework.TestCase.assertEquals;

public class ServiceDescriptionTest {

    private final ServiceDescription serviceDescription = ServiceDescription.builder()
            .setServiceImplName(Object.class.getName())
            .setServiceName("SOME_NAME")
            .setInitType(Service.InitType.LAZY)
            .setType(Service.Type.PROTOTYPE)
            .addInterface(Runnable.class.getName(), ServiceDescription.AccessType.PRIVATE)
            .addInterface(Serializable.class.getName(), ServiceDescription.AccessType.PUBLIC)
            .build();

    @Test
    public void toFromManifestTest() throws IOException {
        final String str = serviceDescription.toManifestCompatibleString();
        final ServiceDescription serviceDescription2 = ServiceDescription.fromManifestString(str);
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
                ACCESS_TYPE + ServiceDescription.AccessType.PUBLIC + "," +

                CLASS + Runnable.class.getName() + "," +
                ACCESS_TYPE + ServiceDescription.AccessType.PRIVATE + "," +
                "]";

        final ServiceDescription serviceDescription2 = ServiceDescription.fromManifestString(manifestStr);
        assertEquals(serviceDescription, serviceDescription2);
    }

}
package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.application_context.api.annotations.Service;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ManifestServiceDescriptor implements ServiceDescriptor {
    public static final String GROUP_SEPARATOR = ";";
    public static final String SERVICE_CLASS = "class=>";
    public static final String SERVICE_NAME = "name=>";
    public static final String TYPE = "type=>";
    public static final String INIT_TYPE = "initType=>";
    public static final String INTERFACES = "interfaces=>";
    public static final String CLASS = "cls=>";
    public static final String ACCESS_TYPE = "accessType=>";

    private final static Pattern MAVEN_PATTERN = Pattern.compile("^" +
            SERVICE_CLASS + "([^,]+)," +
            SERVICE_NAME + "([^,]*)," +
            TYPE + "([^,]+)," +
            INIT_TYPE + "([^,]+)," +
            INTERFACES + "\\[(.*)\\]$");

    private final static Pattern MAVEN_INTERFACES_PATTERN = Pattern.compile(CLASS + "([^,]+)," +
            ACCESS_TYPE + "(" +
            Arrays.stream(AccessType.values()).map(Enum::toString).collect(Collectors.joining("|")) +
            ")");

    private final String serviceImplName;
    private final String serviceName;
    private final Service.LifecycleType lifecycleType;
    private final Service.InitType initType;
    private final Map<String, AccessType> interfaces;

    private ManifestServiceDescriptor(Builder builder) {
        this.serviceImplName = builder.serviceImplName;
        this.serviceName = builder.serviceName;
        this.lifecycleType = builder.lifecycleType;
        this.initType = builder.initType;
        this.interfaces = Maps.newHashMap(builder.interfaces);
    }

    @Nonnull
    public static String toManifestString(@Nonnull ServiceDescriptor serviceDescriptor) {
        final StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append(SERVICE_CLASS).append(serviceDescriptor.getServiceClass()).append(",");
        stringBuilder.append(SERVICE_NAME).append(StringUtils.isNoneBlank(serviceDescriptor.getServiceName()) ?
                serviceDescriptor.getServiceName() : "").append(",");
        stringBuilder.append(TYPE).append(serviceDescriptor.getLifecycleType()).append(",");
        stringBuilder.append(INIT_TYPE).append(serviceDescriptor.getInitType()).append(",");

        stringBuilder.append(INTERFACES + "[");
        stringBuilder.append(serviceDescriptor.getInterfaces().entrySet().stream()
                .map(entry -> String.format(CLASS + "%s," +
                                ACCESS_TYPE + "%s",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","))
        );
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    @Nonnull
    public static Set<ServiceDescriptor> fromManifestString(@Nullable String serviceDescAsStr) {
        return StringUtils.isBlank(serviceDescAsStr) ? ImmutableSet.of() : Arrays.stream(serviceDescAsStr.split(GROUP_SEPARATOR))
                .map(str -> {
                    Matcher matcher = MAVEN_PATTERN.matcher(str);

                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("Invalid format: " + serviceDescAsStr);
                    }

                    final Builder builder = builder().setServiceImplName(matcher.replaceFirst("$1"))
                            .setServiceName(matcher.replaceFirst("$2"))
                            .setLifecycleType(Service.LifecycleType.valueOf(matcher.replaceFirst("$3")))
                            .setInitType(Service.InitType.valueOf(matcher.replaceFirst("$4")));

                    String interfaces = matcher.replaceFirst("$5");
                    matcher = MAVEN_INTERFACES_PATTERN.matcher(interfaces);

                    int end = 0;
                    while (matcher.find()) {
                        int start = matcher.start();
                        if (end != 0 && (interfaces.charAt(end) != ',' || start != end + 1)) {
                            throw new IllegalArgumentException("Invalid format: " + serviceDescAsStr);
                        }
                        end = matcher.end();

                        builder.addInterface(matcher.group(1), AccessType.valueOf(matcher.group(2)));
                    }

                    return builder.build();
                })
                .collect(Collectors.toSet());

    }

    @Override
    public String getServiceClass() {
        return serviceImplName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Service.LifecycleType getLifecycleType() {
        return lifecycleType;
    }

    @Override
    public Service.InitType getInitType() {
        return initType;
    }

    @Override
    public Map<String, AccessType> getInterfaces() {
        return interfaces;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManifestServiceDescriptor that = (ManifestServiceDescriptor) o;

        return Objects.equals(serviceImplName, that.serviceImplName) &&
                Objects.equals(serviceName, that.serviceName) &&
                lifecycleType == that.lifecycleType &&
                initType == that.initType &&
                Objects.equals(interfaces, that.interfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceImplName, serviceName, lifecycleType, initType, interfaces);
    }

    public static class Builder {
        private String serviceImplName;
        private String serviceName;
        private Service.LifecycleType lifecycleType = Service.LifecycleType.SINGLETON;
        private Service.InitType initType = Service.InitType.LAZY;
        private final LinkedHashMap<String, AccessType> interfaces = Maps.newLinkedHashMap();

        public Builder setServiceImplName(@Nonnull String serviceImplName) {
            this.serviceImplName = serviceImplName;
            return this;
        }

        public Builder setServiceName(@Nonnull String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setLifecycleType(@Nonnull Service.LifecycleType lifecycleType) {
            this.lifecycleType = lifecycleType;
            return this;
        }

        public Builder setInitType(@Nonnull Service.InitType initType) {
            this.initType = initType;
            return this;
        }

        public Builder addInterface(String interfaceName, AccessType accessType) {
            interfaces.put(interfaceName, accessType);
            return this;
        }

        @Nonnull
        public ManifestServiceDescriptor build() {
            if (StringUtils.isBlank(serviceImplName)) {
                throw new IllegalArgumentException("serviceImplName is empty!");
            }
            return new ManifestServiceDescriptor(this);
        }
    }
}

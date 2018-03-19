package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//TODO: move to common:application-context-common ???? нужно подождать как напишу плагин сборки плагинов - что там будет в манифесте - из него мы ведь будем читать
public class ServiceDescription {

    public enum AccessType {
        PUBLIC,
        PRIVATE,
    }

    public static final String SERVICE_IMPL_NAME = "serviceImplName=>";
    public static final String SERVICE_NAME = "serviceName=>";
    public static final String TYPE = "type=>";
    public static final String INIT_TYPE = "initType=>";
    public static final String INTERFACES = "interfaces=>";
    public static final String CLASS = "cls=>";
    public static final String ACCESS_TYPE = "accessType=>";

    private final static Pattern MAVEN_PATTERN = Pattern.compile("^" +
            SERVICE_IMPL_NAME + "([^,]+)," +
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
    private final Service.Type type;
    private final Service.InitType initType;
    private final Map<String, AccessType> interfaces;

    private ServiceDescription(Builder builder) {
        this.serviceImplName = builder.serviceImplName;
        this.serviceName = builder.serviceName;
        this.type = builder.type;
        this.initType = builder.initType;
        this.interfaces = Maps.newHashMap(builder.interfaces);
    }

    @Nonnull
    public String toManifestCompatibleString() {
        final StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append(SERVICE_IMPL_NAME).append(serviceImplName).append(",");
        stringBuilder.append(SERVICE_NAME).append(StringUtils.isNoneBlank(serviceName) ? serviceName : "").append(",");
        stringBuilder.append(TYPE).append(type).append(",");
        stringBuilder.append(INIT_TYPE).append(initType).append(",");

        stringBuilder.append(INTERFACES + "[");
        stringBuilder.append(interfaces.entrySet().stream()
                .map(entry -> String.format(CLASS + "%s," +
                                ACCESS_TYPE + "%s",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","))
        );
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    public static ServiceDescription fromManifestString(String serviceDescAsStr) {
        Matcher matcher = MAVEN_PATTERN.matcher(serviceDescAsStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid format: " + serviceDescAsStr);
        }

        final Builder builder = builder().setServiceImplName(matcher.replaceFirst("$1"))
                .setServiceName(matcher.replaceFirst("$2"))
                .setType(Service.Type.valueOf(matcher.replaceFirst("$3")))
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
    }

    public String getServiceImplName() {
        return serviceImplName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Service.Type getType() {
        return type;
    }

    public Service.InitType getInitType() {
        return initType;
    }

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
        ServiceDescription that = (ServiceDescription) o;

        return Objects.equals(serviceImplName, that.serviceImplName) &&
                Objects.equals(serviceName, that.serviceName) &&
                type == that.type &&
                initType == that.initType &&
                Objects.equals(interfaces, that.interfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceImplName, serviceName, type, initType, interfaces);
    }

    public static class Builder {
        private String serviceImplName;
        private String serviceName;
        private Service.Type type = Service.Type.SINGLETON;
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

        public Builder setType(@Nonnull Service.Type type) {
            this.type = type;
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
        public ServiceDescription build() {
            if (StringUtils.isBlank(serviceImplName)) {
                throw new IllegalArgumentException("serviceImplName is empty!");
            }
            return new ServiceDescription(this);
        }
    }
}

package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//TODO: move to common:application-context-common ???? нужно подождать как напишу плагин сборки плагинов - что там будет в манифесте - из него мы ведь будем читать
public class ServiceDescription {

    public enum AccessType {
        PUBLIC,
        PRIVATE
    }

    private final String serviceImplName;
    private final String serviceName;
    private final Service.Type type;
    private final Service.InitType initType;
    private final ImmutableList<String> interfaces;
    private final AccessType accessType;

    private ServiceDescription(Builder builder) {
        this.serviceImplName = builder.serviceImplName;
        this.serviceName = builder.serviceName;
        this.type = builder.type;
        this.initType = builder.initType;
        this.interfaces = new ImmutableList.Builder<String>().addAll(builder.interfaces).build();
        this.accessType = builder.accessType;
    }

    @Nonnull
    public String toManifestCompatibleString() {
        final StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append("serviceImplName=>").append(serviceImplName).append(",");
        stringBuilder.append("serviceName=>").append(StringUtils.isNoneBlank(serviceName) ? serviceName : "").append(",");
        stringBuilder.append("type=>").append(type).append(",");
        stringBuilder.append("initType=>").append(initType).append(",");
        stringBuilder.append("accessType=>").append(accessType).append(",");

        stringBuilder.append("interfaces=>[");
        stringBuilder.append(interfaces.stream().collect(Collectors.joining(",")));
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    public static ServiceDescription fromManifestString(String serviceDescAsStr) {
        final Pattern pattern = Pattern.compile("^serviceImplName=>([^,]+),serviceName=>([^,]*),type=>([^,]+),initType=>([^,]+),accessType=>([^,]+),interfaces=>\\[(.*)\\]$");
        final Matcher matcher = pattern.matcher(serviceDescAsStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid format: " + serviceDescAsStr);
        }

        return builder().setServiceImplName(matcher.replaceFirst("$1"))
                .setServiceName(matcher.replaceFirst("$2"))
                .setType(Service.Type.valueOf(matcher.replaceFirst("$3")))
                .setInitType(Service.InitType.valueOf(matcher.replaceFirst("$4")))
                .setAccessType(AccessType.valueOf(matcher.replaceFirst("$5")))
                .setInterfaces(Arrays.stream(matcher.replaceFirst("$6").split(",")).collect(Collectors.toList()))
                .build();
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

    public ImmutableList<String> getInterfaces() {
        return interfaces;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String serviceImplName;
        private String serviceName;
        private Service.Type type = Service.Type.SINGLETON;
        private Service.InitType initType = Service.InitType.LAZY;
        private List<String> interfaces = Lists.newArrayList();
        private AccessType accessType = AccessType.PUBLIC;

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

        public Builder setInterfaces(@Nonnull List<String> interfaces) {
            this.interfaces = interfaces;
            return this;
        }

        public Builder setAccessType(@Nonnull AccessType accessType) {
            this.accessType = accessType;
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

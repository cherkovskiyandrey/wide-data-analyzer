package com.cherkovskiy.gradle.plugin.application;

import com.google.common.collect.Sets;
import org.gradle.api.Action;
import org.gradle.api.Project;

import java.util.Objects;
import java.util.Set;

public class LazyLoaderConfig {
    public static final String NAME = "lazyLoader";

    private final Project project;
    private final Set<LazyPluginConfig> pluginConfigSet = Sets.newHashSet();

    @javax.inject.Inject
    public LazyLoaderConfig(Project project) {
        this.project = project;
    }

    public void lazyPlugin(Action<? super LazyPluginConfig> lazyPluginConfigure) {
        final LazyPluginConfig lazyPluginConfig = project.getObjects().newInstance(LazyPluginConfig.class);
        lazyPluginConfigure.execute(lazyPluginConfig);
        pluginConfigSet.add(lazyPluginConfig);

        //project.getTasks().getAt("compileJava").dependsOn(lazyPluginConfig.getLoad() + ":build");
    }

    public Set<LazyPluginConfig> getPluginConfigSet() {
        return pluginConfigSet;
    }

    public static class LazyPluginConfig {
        private String load;
        private String cfgName;
        private Action deferredCfgAction;

        public String getLoad() {
            return load;
        }

        public void setLoad(String load) {
            this.load = load;
        }

        public void cfg(Action deferredCfgAction) {
            this.deferredCfgAction = deferredCfgAction;
        }

        public Action getCfgAction() {
            return deferredCfgAction;
        }

        public String getCfgName() {
            return cfgName;
        }

        public void setCfgName(String cfgName) {
            this.cfgName = cfgName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LazyPluginConfig that = (LazyPluginConfig) o;
            return Objects.equals(load, that.load);
        }

        @Override
        public int hashCode() {

            return Objects.hash(load);
        }
    }
}

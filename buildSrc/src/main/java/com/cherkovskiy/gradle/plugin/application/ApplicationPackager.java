package com.cherkovskiy.gradle.plugin.application;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.jvm.tasks.Jar;

import javax.annotation.Nonnull;


public class ApplicationPackager implements Plugin<Project> {


    @Override
    public void apply(@Nonnull Project project) {
        final LazyLoaderConfig configuration = project.getExtensions().create(LazyLoaderConfig.NAME, LazyLoaderConfig.class, project);

        project.getTasks().getAt("compileJava").doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {


                //Загружаем код плагина
                configuration.getPluginConfigSet().forEach(lazyPluginConfig -> {

                    System.out.println("Plugin artifacts ===>>>> " +
                            project.project(lazyPluginConfig.getLoad()).getTasks().withType(Jar.class).iterator().next().getArchivePath());

                    //TODO: получить все его зависимости и запихать в URL класслоадер, а потом его установить как текущий потоковый
                    // найти в архиве класс который имплементит плагин, попросить его у лоадера, дернуть ему apply,
                    // потом подпихнуть конйигурацию и всё

                });


//                // это сделает плагин
//                final ApplicationPackagerConfiguration confOfLazyPlugin = project.getExtensions()
//                        .create(ApplicationPackagerConfiguration.NAME, ApplicationPackagerConfiguration.class);
//                //-------------
//
//
//                // а потом мы ему подпихнём конфигурацию
//                project.getExtensions().configure(configuration.getCfgName(), configuration.getCfgAction());
//
//                System.out.println("================>>>>>> Plugin applied for " + project.getDisplayName() + " => " + confOfLazyPlugin.format);
            }
        });


//        //вызвать у подгружаемого плагина apply(), он может зарегать свой конфиг, а потом мы его таким мокаром сконфигурируем
//        // но делаем это только в project.getTasks().getAt("compileJava").doBefore()
//        project.getExtensions().configure("", new Action<Object>() {
//            @Override
//            public void execute(Object o) {
//                //todo
//            }
//        });
//
//        project.getTasks().getAt("build").doLast(task -> {
//
//
//            System.out.println(configuration.format);
//
//            System.out.println(project.toString());
//
//            System.out.println(configuration.getSettings());
//
//            //Thread.currentThread().getContextClassLoader().
//
//        });
    }
//    @Override
//    public void apply(Project project) {
//        // Apply BundlePackager plugin first of all
//        //project.getPluginManager().apply(BundlePackager.class);
//
//        //TEST: try to depends on plugin code as singleton project
//
//        //final Jar jarTask = project.getTasks().withType(Jar.class).iterator().next();
//        //does not work
//        //project.project(":common:virtual-file-system").getTasks().getAt("build").mustRunAfter(jarTask);
//        //project.project(":api:api-application-context").getTasks().getAt("build").getActions().stream().forEach(action -> action.execute());
//
////        project.getRootProject().getConfigurations()
////                .detachedConfiguration(project.getRootProject()
////                        .getDependencies()
////                        .project(Collections.singletonMap("path", ":plugins:neural-network-plugin"))
////                ).getResolvedConfiguration();
////
////        Set<ResolvedDependency> resolvedDependencies = project.getConfigurations()
////                .detachedConfiguration(project.getDependencies().create(":plugins:neural-network-plugin"))
////                .getResolvedConfiguration()
////                .getFirstLevelModuleDependencies();
////
////        project.getLogger().log(LogLevel.ERROR, resolvedDependencies.toString());
//
////        //TEST: Try to lazy load plugin in execution stage
////        final Jar jarTask = project.getTasks().withType(Jar.class).iterator().next();
////        jarTask.doLast(task -> {
////
////            System.out.println("sss");
////
////
////            project.getConfigurations()
////                    .getByName(DependencyType.COMPILE.getGradleString());
////
////        });
////
////        project.getTasks().getAt("build").doLast(task -> {
////            project.project(":api:api-application-context").getTasks().getAt("build").mustRunAfter();
////        });
//    }
}

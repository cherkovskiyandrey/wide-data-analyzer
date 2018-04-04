//package com.cherkovskiy.gradle.plugin;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import org.apache.commons.io.IOUtils;
//import org.gradle.api.GradleException;
//import org.gradle.api.Plugin;
//import org.gradle.api.Project;
//import org.gradle.api.artifacts.Dependency;
//import org.gradle.api.artifacts.ResolvedDependency;
//import org.gradle.jvm.tasks.Jar;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.StandardOpenOption;
//import java.util.List;
//import java.util.Map;
//import java.util.jar.Attributes;
//import java.util.jar.JarFile;
//import java.util.jar.JarOutputStream;
//import java.util.jar.Manifest;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class CustomPluginTest implements Plugin<Project> {
//    private final static String TASK_NAME = "rebuildManifest";
//    private final static String AFTER_TASK_NAME = "jar";
//
//    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";
//
//    //---- Add own task after other
//    @Override
//    public void apply(Project project) {
////        project.task(TASK_NAME)
////                .setActions(Collections.singletonList(new Action<Task>() {
////                    @Override
////                    public void execute(Task o) {
////                        System.out.println(">>>>>>>>>>>>>>>    rebuildManifest has been invoked form action!!!!!!!!");
////                        System.out.println(project.getConfigurations().getByName("compileOnly").getIncoming().getResolutionResult().getAllDependencies().size());
////                        System.out.println(project.getConfigurations().getByName("compile").getResolvedConfiguration().getFirstLevelModuleDependencies().size());
////                    }
////                }));
////
////        project.getTasksByName(AFTER_TASK_NAME, false)
////                .forEach(compileJavaTask -> compileJavaTask.finalizedBy(TASK_NAME));
////
////        //-------------- How to write to Manifest -------------------
////        project.getTasks().withType(Jar.class).forEach(jar -> {
////
////            System.out.println(jar.getTaskActions());
////
////
////            jar.doLast(new Action<Task>() {
////                @Override
////                public void execute(Task task) {
////                    final Jar jarTask = (Jar)task;
////
////                    //ok
////            System.out.println(project.getConfigurations().getByName("compile").getResolvedConfiguration().getFirstLevelModuleDependencies().size());
////            System.out.println(project.getConfigurations().getByName("compileOnly").getIncoming().getResolutionResult().getAllDependencies().size());
////
////
////            //does not change - too late
////                    final Manifest manifest = jarTask.getManifest();
////                    final Attributes attributes = manifest.getAttributes();
////                    attributes.put("Custom-Attribute", "Hello from gradle plugin!");
////
////                    jarTask.setManifest(manifest);
////                }
////            });
////        });
//        //--------------------------- Change Manifest after jar --------------------------
//
//        project.getTasks().withType(Jar.class).forEach(jar -> {
//            jar.doLast(task -> {
//                final Jar jarTask = (Jar) task;
//
//
//                testCreateNewConfiguration(project);
//
//
//
//
//                //ok
//                System.out.println("api:");
//                for (Dependency dependency : project.getConfigurations().getByName("api").getDependencies()) {
//                    System.out.println(String.join(":", dependency.getGroup(), dependency.getName(), dependency.getVersion()));
//                }
//                System.out.println("compileClasspath:");
//                for (ResolvedDependency resolvedDependency : project.getConfigurations().getByName("compileClasspath").getResolvedConfiguration().getFirstLevelModuleDependencies()) {
//                    printDependency(resolvedDependency, 0);
//                }
//
////
////                // repackage
////                final Map<String, String> extraAttributes = Maps.newHashMap();
////                addToManifest(jarTask.getArchivePath(), extraAttributes);
//
//            });
//        });
//    }
//
//    private void testCreateNewConfiguration(Project project) {
//
//        final List<DependencyHolder> dependencies = Lists.newArrayList();
//
//        for (ResolvedDependency resolvedDependency : project.getConfigurations()
//                .detachedConfiguration(
//                        ((Map<String, Dependency>)project.getProperties().get("dependencyManagement")).values().toArray(new Dependency[]{}))
//                .getResolvedConfiguration()
//                .getFirstLevelModuleDependencies()) {
//
//            walkDependency(resolvedDependency, dependencies, null);
//        }
//    }
//
//    private void walkDependency(ResolvedDependency resolvedDependency, List<DependencyHolder> dependencies, DependencyHolder parent) {
//        final DependencyHolder holder = DependencyHolder.builder()
//                .setGroup(resolvedDependency.getModule().getId().getGroup())
//                .setName(resolvedDependency.getModule().getId().getName())
//                .setVersion(resolvedDependency.getModule().getId().getVersion())
//                .setFile(resolvedDependency.getModuleArtifacts().iterator().next().getMainFile())
//                .setParent(parent)
//                .setType(DependencyType.IMPLEMENTATION)
//                .build();
//
//        dependencies.add(holder);
//        resolvedDependency.getChildren().forEach(childDependency -> walkDependency(childDependency, dependencies, holder));
//    }
//
//
//    private void printDependency(ResolvedDependency resolvedDependency, int i) {
//        System.out.println(blankets(i) + resolvedDependency.getModule().getId().getGroup() + ":" +
//                resolvedDependency.getModule().getId().getName() + ":" +
//                resolvedDependency.getModule().getId().getVersion()
//                + "     =>      " +
//                resolvedDependency.getModuleArtifacts().iterator().next().getMainFile()
//        );
//        if(!resolvedDependency.getChildren().isEmpty()) {
//            System.out.println(blankets(i) + "dependencies: [");
//        }
//        resolvedDependency.getChildren().forEach(resolvedDependency1 -> printDependency(resolvedDependency1, i + 1));
//        if(!resolvedDependency.getChildren().isEmpty()) {
//            System.out.println(blankets(i) + "]");
//        }
//    }
//
//    private String blankets(int nums) {
//        return IntStream.range(0, nums).mapToObj(i -> "\t").collect(Collectors.joining(""));
//    }
//
//
//    private void addToManifest(File archivePath, Map<String, String> extraAttributes) {
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024)) {
//            try (final JarFile jarFile = new JarFile(archivePath)) {
//
//                final Manifest manifest = new Manifest(jarFile.getManifest());
//                final Attributes attributes = manifest.getMainAttributes();
//                extraAttributes.forEach((key, value) -> attributes.put(new Attributes.Name(key), value));
//
//                try (JarOutputStream jarOutputStream = new JarOutputStream(byteArrayOutputStream, manifest)) {
//                    jarFile.stream()
//                            .filter(jarEntry -> !MANIFEST_ENTRY_NAME.equalsIgnoreCase(jarEntry.getName()))
//                            .forEach(jarEntry -> {
//                                try {
//                                    jarOutputStream.putNextEntry(jarEntry);
//                                    try (final InputStream jarEntryStream = jarFile.getInputStream(jarEntry)) {
//                                        IOUtils.copyLarge(jarEntryStream, jarOutputStream);
//                                    }
//                                    jarOutputStream.closeEntry();
//                                } catch (IOException e) {
//                                    throw new GradleException("Could not copy artifact: " + archivePath.getAbsolutePath(), e);
//                                }
//                            });
//                }
//            }
//            Files.write(archivePath.toPath(), byteArrayOutputStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//        } catch (IOException e) {
//            throw new GradleException("Could not open artifact: " + archivePath.getAbsolutePath(), e);
//        }
//    }
//}

package com.cherkovskiy.gradle.plugin.application;

import org.gradle.api.Project;

import java.util.Set;

public class BundleResolver {
    public BundleResolver(Project project, String configaration) {

    }

    /**
     *
     *
     * @return all bundles form which application get on board + embedded in application bundle
     */
    public Set<BundleHolder> getBundles() {
        //todo
        throw new UnsupportedOperationException("");
    }



//            for(Dependency dependency : project.getConfigurations().getByName(ONBOARD_CONF_NAME).getDependencies()) {
//        if (dependency instanceof DefaultProjectDependency) {
//            final Project depProject = ((DefaultProjectDependency) dependency).getDependencyProject();
//
//            if(isBundle(depProject)) {
//                final DependencyScanner dependencyScanner = new DependencyScanner(project);
//                final List<DependencyHolder> dependencies = dependencyScanner.getRuntimeDependencies();
//                final ResolvedByTypesBundleDependencies dependencyCollection = new ResolvedByTypesBundleDependencies(dependencies);
//
//                //todo: compare all common by versions and against impl in each bundle + compare all api versions
//
//                //todo: split api api from api impl for checking!
//
//                //todo: put dependencies in right places
//
//            }
//            //todo: other project elements
//        } else {
//            //todo: external bundles
//            for (ResolvedDependency resolvedDependency : project.getConfigurations()
//                    .detachedConfiguration(dependency)
//                    .getResolvedConfiguration()
//                    .getFirstLevelModuleDependencies()) {
//
//                //todo: gather like in DependencyScanner
//            }
//            final List<DependencyHolder> dependencies = Collections.emptyList(); //todo: gather like in DependencyScanner
//            final ResolvedByTypesBundleDependencies dependencyCollection = new ResolvedByTypesBundleDependencies(dependencies);
//
//            //todo: compare all common by versions and against impl in each bundle + compare all api versions
//
//            //todo: put dependencies in right places - OK
//
//            //todo: split api api from api impl for checking! - PROBLEM....
//        }
//    }
}

package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.bundles.BundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;


//todo: для Володи. Написать спеку что нужно сделать
class ApplicationResolver {


    //TODO: в первую очередь нам нужно написать свою имплементацию резолвера BundleResolver
    //сейчас уже есть 2 имплементации EmbeddedResolver и ProjectBundleResolver - они используются во время сборки проекта.
    // А тут рантайм. У нас есть устоявшеяся структура каталогов приложения (ApplicationDirectories), этот резолвер должен работать с ней.
    // Логика резолвинга так же простая и схожа с ProjectBundleResolver, так же используем ResolvedBundleFile.builder(),
    // только все зависимости берём из наших каталогов ApplicationDirectories.
    // Позже напишу дизайн по резолвингу перегруженных бандлов.
    //private final BundleResolver bundleResolver = new ApplicationBundleResolver(appHome);
    ApplicationResolver(String appHome) {

    }

    /**
     * Return resolved main application bundle.
     *
     * @return
     */
    public ResolvedBundleArtifact resolveApplicationBundle() {
        throw new UnsupportedOperationException("It is not supported yet.");

//        File appBundleFile = Paths.get(appHome, ApplicationDirectories.APP.getPath()).toFile();
//        BundleArtifact appBundleArtifact = new BundleFile(appBundleFile);
//        return bundleResolver.resolve(appBundleArtifact);
    }

    public List<ResolvedBundleArtifact> resolveOtherBundles() {
        throw new UnsupportedOperationException("It is not supported yet.");
//        Path onBoardBundlesPath = Paths.get(appHome, ApplicationDirectories.BUNDLES.getPath());
//        List<BundleFile> bundles = Files.walk(onBoardBundlesPath, 0)
//                .filter(p -> p.getFileName().endsWith(".jar"))
//                .map(Path::toFile)
//                .map(file -> {
//                    try {
//                        return new BundleFile(file);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .collect(Collectors.toList());
//
//
//        bundles.stream().map(bundleResolver::resolve).collect(Collectors.toList());
    }

    //todo: разложить артефакты в соответсвующие каталоги. Продумать структуру каталогов для перегруженных бандлов.
    public void addReloadedBundle(@Nonnull ResolvedBundleArtifact bundleFile) throws BundleReloadException {
    }

    /**
     * Return sorted list of provided bundle's name and version.
     * Usually the last one is used at one moment.
     * Others could be as reloaded versions
     *
     * @param bundleVersionName
     * @return
     */
    public List<ResolvedBundleArtifact> getBundles(@Nonnull BundleVersionName bundleVersionName) {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    /**
     * Remove unused bundles.
     * For example reloaded intermediate bundles. But api jars of these bundles have to be saved.
     * These api jars could be removed after reloading whole application.
     */
    public void removeUnusedBundles() {
        //todo
    }

    /**
     * Resolve bundle against current bundle environment and in tmp directory.
     *
     * @param bundleFile
     * @return
     */
    public ResolvedBundleArtifact resolveOutsideBundle(@Nonnull BundleArtifact bundleFile) {
        //todo: скопировать файл в спец каталог, разрезолвить его если он embeded или нет - используя текущий енв, если не получилось - исключение
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    public void removeReloadedBundle(@Nonnull ResolvedBundleArtifact patchedBundle) {
        //todo
    }
}

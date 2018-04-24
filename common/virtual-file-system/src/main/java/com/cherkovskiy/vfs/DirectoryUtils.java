package com.cherkovskiy.vfs;

public class DirectoryUtils {
    public static void copyRecursive(Directory from, String fromDir, MutableDirectory to, String toDir) {

        throw new UnsupportedOperationException("!");
        //todo
//        for (File file : FileUtils.listFiles(resourcesPath, null, true)) {
//            if (file.isFile()) {
//                String relativePath = resourcesPath.toPath().relativize(file.toPath()).toString();
//                try (InputStream inputStream = FileUtils.openInputStream(file)) {
//                    directory.createIfNotExists("bin/" + relativePath, inputStream, null);
//                }
//            }
//        }
    }
}

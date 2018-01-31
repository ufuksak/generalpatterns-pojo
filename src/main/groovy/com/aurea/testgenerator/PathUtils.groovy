package com.aurea.testgenerator

import com.github.javaparser.ast.PackageDeclaration

import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function


final class PathUtils {

    static Path nthParent(Path path, int n) {
        Path result = path
        while (n-- > 0) {
            result = result.getParent()
        }
        return result
    }

    static String packageNameToFileName(String packageName) {
        packageName.replace(".", File.separator)
    }

    static Path packageNameToPath(String packageName) {
        Paths.get(packageNameToFileName(packageName))
    }

    static Path packageToPath(PackageDeclaration packageDeclaration) {
        packageNameToPath(packageDeclaration.getNameAsString())
    }

    static Function<Path, String> newPathToPackage(Path root) {
        return { root.relativize(it).toFile().getPath().replace(File.separator, ".") }
    }

}

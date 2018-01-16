package com.aurea.testgenerator.source;

import com.github.javaparser.ast.PackageDeclaration;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public final class PathUtils {

    public static Path nthParent(Path path, int n) {
        Path result = path;
        while (n-- > 0) {
            result = result.getParent();
        }
        return result;
    }

    public static String packageNameToFileName(String packageName) {
        return packageName.replace(".", File.separator);
    }

    public static Path packageNameToPath(String packageName) {
        return Paths.get(packageNameToFileName(packageName));
    }

    public static Path packageToPath(PackageDeclaration packageDeclaration) {
        return packageNameToPath(packageDeclaration.getNameAsString());
    }

    public static Function<Path, String> newPathToPackage(Path root) {
        return (Path p) -> root.relativize(p).toFile().getPath().replace(File.separator, ".");
    }
}

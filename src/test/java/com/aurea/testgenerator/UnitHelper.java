package com.aurea.testgenerator;

import com.aurea.testgenerator.source.PathToUnitMapper;
import com.aurea.testgenerator.source.PathUtils;
import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.System.lineSeparator;

public final class UnitHelper {

    public static final String TEST_CLASS_NAME = "TestClass";
    public static final String PACKAGE_NAME = "org.example";

    public static Unit getUnitForTestJavaFile(Class<?> testClass) throws URISyntaxException {
        Path classFilePath = Paths.get(UnitHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path root = PathUtils.nthParent(classFilePath, 2);
        Path testRoot = root.resolve(Paths.get("src", "test", "java"));
        Path testFile = Paths.get(testClass.getName().replace(".", File.separator) + ".java");
        Path pathToJavaFile = testRoot.resolve(testFile);

        return new PathToUnitMapper(testRoot).apply(pathToJavaFile).get();
    }

    public static Unit getUnit(Path root, Path pathToUnit) throws URISyntaxException {
        return new PathToUnitMapper(root).apply(pathToUnit).get();
    }

    public static Optional<Unit> getUnitForCode(String code) {
        return getUnitForCode(code, TEST_CLASS_NAME, PACKAGE_NAME);
    }

    public static Optional<Unit> getUnitForMethod(String code) {
        return getUnitForMethod(code, TEST_CLASS_NAME, PACKAGE_NAME);
    }

    public static Optional<Unit> getUnitForMethod(String code, String className, String packageName) {
        String javaFileCode = "package " + packageName + "; "
                + lineSeparator() + "public class " + className + " { "
                + lineSeparator() + code + lineSeparator() + "}";

        return getUnitForCode(javaFileCode, className, packageName);
    }

    public static Optional<Unit> getUnitForCode(String code, String className, String packageName) {
        try {
            CompilationUnit cu = JavaParser.parse(code);
            return Optional.of(new Unit(cu, className, packageName, Paths.get("")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Unit> getUnitForCode(File code, String className, String packageName) {
        try {
            CompilationUnit cu = JavaParser.parse(code);
            return Optional.of(new Unit(cu, className, packageName, Paths.get("")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

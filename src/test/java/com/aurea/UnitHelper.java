package com.aurea;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.util.Optional;

import static java.lang.System.lineSeparator;

public final class UnitHelper {

    public static final String TEST_CLASS_NAME = "TestClass";
    public static final String PACKAGE_NAME = "org.example";

    public static Optional<CompilationUnit> getUnitForMethod(String code) {
        String javaFileCode = "package " + PACKAGE_NAME + "; "
                + lineSeparator() + "public class " + TEST_CLASS_NAME + " { "
                + lineSeparator() + code + lineSeparator() + "}";

        return getUnitForCode(javaFileCode);
    }

    public static Optional<CompilationUnit> getUnitForCode(String code) {
        try {
            return Optional.of(JavaParser.parse(code));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<CompilationUnit> getUnitForCode(File code) {
        try {
            return Optional.of(JavaParser.parse(code));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

package com.aurea.testgenerator.source;

import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;

public class Unit {

    private final CompilationUnit cu;
    private final String className;
    private final String packageName;
    private final Path modulePath;

    public Unit(CompilationUnit cu, String className, String packageName, Path modulePath) {
        this.cu = cu;
        this.className = className;
        this.packageName = packageName;
        this.modulePath = modulePath;
    }

    public CompilationUnit getCu() {
        return cu;
    }

    public String getClassName() {
        return className;
    }

    public String fullName() {
        return getPackageName() + "." + getClassName();
    }

    public String getPackageName() {
        return packageName;
    }

    public Path getModulePath() {
        return modulePath;
    }
}

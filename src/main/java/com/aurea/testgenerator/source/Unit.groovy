package com.aurea.testgenerator.source

import com.aurea.common.JavaClass
import com.github.javaparser.ast.CompilationUnit
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.nio.file.Path

@EqualsAndHashCode(excludes = 'cu')
@ToString(includePackage = false, excludes = ['cu', 'modulePath'])
class Unit {

    CompilationUnit cu
    JavaClass javaClass
    Path modulePath

    Unit(CompilationUnit cu, JavaClass javaClass, Path modulePath) {
        this.cu = cu
        this.javaClass = javaClass
        this.modulePath = modulePath
    }

    Unit(CompilationUnit cu, String className, String packageName, Path modulePath) {
        this(cu, new JavaClass(packageName, className), modulePath)
    }

    Unit(CompilationUnit cu, String classFullName, Path modulePath) {
        this(cu, new JavaClass(classFullName), modulePath)
    }

    String getClassName() {
        javaClass.name
    }

    String getFullName() {
        javaClass.fullName
    }

    String getPackageName() {
        javaClass.package
    }
}

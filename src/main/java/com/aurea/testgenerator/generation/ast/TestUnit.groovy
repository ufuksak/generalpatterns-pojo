package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical
import groovy.transform.Memoized
import one.util.streamex.StreamEx

@Canonical
class TestUnit {
    Unit test

    NodeList<ImportDeclaration> getImports() {
        test.cu.imports
    }

    TestUnit addImport(ImportDeclaration id) {
        test.cu.imports << id
        this
    }

    TestUnit addDependency(TestDependency dependency) {
        dependency.imports.each {
            addImport it
        }
        //TODO: impl
        this
    }

    TestUnit addDependency(Dependable dependable) {
        addDependency(dependable.dependency)
        this
    }

    TestUnit addDependencies(List<? extends Dependable> testNodes) {
        StreamEx.of(testNodes).map { it.dependency }.each { addDependency(it) }
        this
    }

    TestUnit addTest(MethodDeclaration test) {
        getTestClass().addMember(test)
        this
    }

    @Memoized
    ClassOrInterfaceDeclaration getTestClass() {
        test.cu.findFirst(ClassOrInterfaceDeclaration).get()
    }
}

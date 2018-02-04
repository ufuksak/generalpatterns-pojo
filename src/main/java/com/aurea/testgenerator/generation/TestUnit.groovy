package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Immutable
import groovy.transform.Memoized

@Immutable
class TestUnit {
    Unit unitUnderTest
    Unit test

    NodeList<ImportDeclaration> getImports() {
        test.cu.imports
    }

    void addTest(MethodDeclaration test) {
        getTestClass().addMember(test)
    }

    @Memoized
    ClassOrInterfaceDeclaration getTestClass() {
        test.cu.findFirst(ClassOrInterfaceDeclaration).get()
    }
}

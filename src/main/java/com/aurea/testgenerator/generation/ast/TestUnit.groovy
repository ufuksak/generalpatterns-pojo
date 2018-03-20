package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
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

    TestUnit addTestsAndDependencies(List<DependableNode<MethodDeclaration>> testNodes) {
        addImports(testNodes)

        StreamEx.of(testNodes).flatMap{it.dependency.fields.stream()}.toSet().sort { it.getVariable(0).nameAsString }.each {
            addField(it)
        }

        StreamEx.of(testNodes).flatMap{it.dependency.methodSetups.stream()}.toSet().sort{it.nameAsString}.each {
            addTest(it)
        }

        testNodes.each {addTest(it.node)}

        this
    }

    private List<ImportDeclaration> addImports(List<DependableNode<MethodDeclaration>> testNodes) {
        StreamEx.of(testNodes).flatMap { it.dependency.imports.stream() }.each {
            addImport(it)
        }
        test.cu.imports = test.cu.imports.toSet().sort{it.toString()}
    }

    TestUnit addTest(MethodDeclaration test) {
        getTestClass().addMember(test)
        this
    }

    TestUnit addField(FieldDeclaration field){
        getTestClass().addMember(field)
        this
    }

    @Memoized
    ClassOrInterfaceDeclaration getTestClass() {
        test.cu.findFirst(ClassOrInterfaceDeclaration).get()
    }
}

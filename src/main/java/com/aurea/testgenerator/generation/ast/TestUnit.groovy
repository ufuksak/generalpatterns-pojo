package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Modifier
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
        if (!test.cu.imports.contains(id)) {
            test.cu.imports << id
        }
        this
    }

    TestUnit addDependency(TestDependency dependency) {
        dependency.imports.each {
            addImport it
        }
        addFields dependency
        addClassAnnotations dependency
    }

    TestUnit addClassAnnotations(TestDependency dependency) {
        dependency.classAnnotations.each {
            if (!test.cu.types[0].annotations.contains(it)) {
                test.cu.types[0].annotations.add(it)
            }
        }
        this
    }

    TestUnit addFields(TestDependency dependency) {
        ClassOrInterfaceDeclaration classDeclaration = test.cu.findFirst(ClassOrInterfaceDeclaration).get()
        dependency.fields.findAll { field ->
            !(field.nameAsString in classDeclaration.getFields().nameAsString)
        }.each { field ->
            def fieldDeclaration = classDeclaration.addField(field.type.asString(), field.nameAsString, field.modifiers.toArray() as Modifier[])
            fieldDeclaration.annotations.addAll(field.annotations)
        }
        this
    }

    TestUnit addDependency(Dependable dependable) {
        addDependency(dependable.dependency)
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

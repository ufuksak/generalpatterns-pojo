package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.Modifier
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
        if (!test.cu.imports.contains(id)) {
            test.cu.imports << id
        }
        this
    }

    TestUnit addTestsAndDependencies(List<DependableNode<MethodDeclaration>> testNodes) {
        addImports(testNodes)

        testNodes.dependency.fields.flatten().toSet().sort { it.nameAsString }.each{
            addField(it)
        }


        testNodes.dependency.methodSetups.flatten().toSet().sort { it.nameAsString }.each{
            addTest(it)
        }

        testNodes.dependency.classAnnotations.flatten().toSet().each {
            if (!test.cu.types[0].annotations.contains(it)) {
                test.cu.types[0].annotations.add(it)
            }
        }

        testNodes.each {addTest(it.node)}

        this
    }


    private List<ImportDeclaration> addImports(List<DependableNode<MethodDeclaration>> testNodes) {
        testNodes.dependency.imports.flatten().each {
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

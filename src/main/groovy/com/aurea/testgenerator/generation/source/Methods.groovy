package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.TestNodeMethod
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.VoidType


class Methods {

    static MethodDeclaration appendTest(TestNodeMethod ut, String name) {
        MethodDeclaration md = new MethodDeclaration(EnumSet.of(Modifier.PUBLIC), new VoidType(), name)
        ut.imports << Imports.JUNIT_TEST
        md.addAnnotation(Annotations.TEST)
        md.addThrownException(Throwable)
        md
    }
}

package com.aurea.testgenerator.generation

import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical

@Canonical
class TestNodeMethod implements TestNode<MethodDeclaration> {
    TestDependency dependency
    MethodDeclaration md

    @Override
    Optional<MethodDeclaration> getNode() {
        Optional.of(md)
    }
}

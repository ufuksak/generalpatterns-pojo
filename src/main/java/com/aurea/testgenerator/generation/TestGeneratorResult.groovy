package com.aurea.testgenerator.generation

import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical


@Canonical
class TestGeneratorResult {
    List<TestGeneratorError> errors = []
    List<DependableNode<MethodDeclaration>> tests = []
    TestType type
}

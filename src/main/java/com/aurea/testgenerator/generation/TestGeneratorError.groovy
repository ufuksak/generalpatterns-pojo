package com.aurea.testgenerator.generation

import com.github.javaparser.ast.Node
import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage = false)
class TestGeneratorError extends RuntimeException {
    String message

    static TestGeneratorError unsolved(Node node) {
        new TestGeneratorError("Failed to solve $node")
    }

    static TestGeneratorError parseFailure(String text) {
        new TestGeneratorError("Failed to parse $text")
    }
}

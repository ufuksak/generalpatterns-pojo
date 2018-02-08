package com.aurea.testgenerator.generation

import com.github.javaparser.ast.stmt.Statement
import groovy.transform.Canonical

@Canonical
class TestNodeStatement implements TestNode<Statement> {
    TestDependency dependency = new TestDependency()
    Statement stmt

    @Override
    Optional<Statement> getNode() {
        Optional.of(stmt)
    }
}

package com.aurea.testgenerator.generation

import com.github.javaparser.ast.expr.Expression
import groovy.transform.Canonical

@Canonical
class TestNodeExpression implements TestNode<Expression> {
    TestDependency dependency = new TestDependency()
    Expression expr

    @Override
    Optional<Expression> getNode() {
        Optional.of(expr)
    }
}

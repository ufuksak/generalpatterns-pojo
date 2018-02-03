package com.aurea.testgenerator.generation

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.Canonical


@Canonical
class TestNodeExpression implements TestNode<Expression> {
    TestDependency dependency
    Expression expr

    @Override
    Optional<Expression> getNode() {
        Optional.of(expr)
    }
}

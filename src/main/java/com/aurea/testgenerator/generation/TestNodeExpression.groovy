package com.aurea.testgenerator.generation

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.Canonical


@Canonical
class TestNodeExpression extends TestNode<Expression> {
//    TestNodeDependency dependency
    Expression expr
}

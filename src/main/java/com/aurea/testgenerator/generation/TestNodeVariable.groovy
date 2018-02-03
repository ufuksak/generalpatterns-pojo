package com.aurea.testgenerator.generation

import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.Canonical

@Canonical
class TestNodeVariable implements TestNode<VariableDeclarationExpr> {
    TestDependency dependency = new TestDependency()
    Optional<VariableDeclarationExpr> node
}

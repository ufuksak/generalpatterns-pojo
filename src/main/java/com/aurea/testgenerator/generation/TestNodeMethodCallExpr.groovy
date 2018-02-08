package com.aurea.testgenerator.generation

import com.github.javaparser.ast.expr.MethodCallExpr
import groovy.transform.Canonical


@Canonical
class TestNodeMethodCallExpr implements TestNode<MethodCallExpr> {
    TestDependency dependency = new TestDependency()
    MethodCallExpr mce

    @Override
    Optional<MethodCallExpr> getNode() {
        Optional.of(mce)
    }
}

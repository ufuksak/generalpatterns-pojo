package com.aurea.testgenerator.generation

import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.Canonical

@Canonical
class TestNodeVariable extends TestNode<VariableDeclarationExpr> {
}

package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.TestUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr


class AssertionBuilder {

    Expression scope
    TestUnit testUnit

    AssertionBuilder(TestUnit testUnit, Expression scope) {
        this.testUnit = testUnit
        this.scope = scope
    }

    Optional<MethodCallExpr> buildFieldAssertion(FieldDeclaration fieldDeclaration, String fieldName, Expression expected) {
        Optional<VariableDeclarator> maybeVariableDeclarator = fieldDeclaration.getVariableByName(fieldName)
        maybeVariableDeclarator.map { v ->
            new MethodCallExpr()

        }
    }
}

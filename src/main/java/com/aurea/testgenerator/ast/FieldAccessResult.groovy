package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.TestGeneratorError
import com.github.javaparser.ast.expr.Expression
import groovy.transform.Canonical


@Canonical
class FieldAccessResult {

    static FieldAccessResult NO_ACCESS = new FieldAccessResult(
            type: Type.NO_ACCESS,
            expression: null
    )

    static enum Type {
        NO_ACCESS,
        FAILED,
        SUCCESS
    }

    static FieldAccessResult success(Expression expression) {
        new FieldAccessResult(Type.SUCCESS, expression)
    }

    static FieldAccessResult failed(TestGeneratorError error) {
        new FieldAccessResult(Type.FAILED, null, error)
    }

    Type type
    Expression expression
    TestGeneratorError error
}

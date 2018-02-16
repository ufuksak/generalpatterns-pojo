package com.aurea.testgenerator.ast

import com.github.javaparser.ast.expr.Expression
import groovy.transform.Canonical

@Canonical
class FieldAssignment {
    Expression target
    Expression value
    Type type

    boolean isLiteral() {
        value.isLiteralExpr()
    }

    enum Type {
        DIRECT,
        SUPER,
        THIS
    }
}

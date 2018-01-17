package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical


@Canonical
class LiteralAccessorMatch extends AccessorMatch {

    static final String NULL = 'null'

    String expression

    LiteralAccessorMatch(Unit unit, MethodDeclaration n, String expression) {
        super(unit, n)
        this.expression = expression
    }

    boolean isNull() {
        expression == NULL
    }
}

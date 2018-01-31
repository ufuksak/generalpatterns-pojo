package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical

@Canonical
class ClassConstantAccessorMatch extends AccessorMatch {

    String expression

    ClassConstantAccessorMatch(Unit unit, MethodDeclaration n, String expression) {
        super(unit, n)
        this.expression = expression
    }

    boolean isNull() {
        expression == LiteralResolver.NULL
    }
}

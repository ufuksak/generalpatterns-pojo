package com.aurea.testgenerator.pattern.easy

import com.github.javaparser.ast.body.ConstructorDeclaration
import groovy.transform.Canonical

@Canonical
class ConstructorLiteralFieldInitializer extends ConstructorFieldInitializer {
    String expression

    ConstructorLiteralFieldInitializer(ConstructorDeclaration constructorDeclaration, String expression) {
        super(constructorDeclaration)
        this.expression = expression
    }
}

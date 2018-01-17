package com.aurea.testgenerator.pattern.easy

import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.Parameter
import groovy.transform.Canonical


@Canonical
class ConstructorParameterFieldInitializer extends ConstructorFieldInitializer {
    Parameter parameter

    ConstructorParameterFieldInitializer(ConstructorDeclaration constructorDeclaration, Parameter parameter) {
        super(constructorDeclaration)
        this.parameter = parameter
    }
}

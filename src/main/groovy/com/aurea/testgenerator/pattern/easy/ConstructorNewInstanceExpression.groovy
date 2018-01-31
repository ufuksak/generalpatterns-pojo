package com.aurea.testgenerator.pattern.easy

import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.Parameter
import groovy.transform.Canonical


@Canonical
class ConstructorNewInstanceExpression {
    ConstructorDeclaration constructorDeclaration
    Parameter constructorParameterInitializer
    String constantInitialValue
}

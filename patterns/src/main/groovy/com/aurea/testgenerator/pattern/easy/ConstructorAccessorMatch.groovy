package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical

@Canonical
class ConstructorAccessorMatch extends AccessorMatch {

    ConstructorFieldInitializer constructorFieldInitializer

    ConstructorAccessorMatch(Unit unit, MethodDeclaration n, ConstructorFieldInitializer constructorFieldInitializer) {
        super(unit, n)
        this.constructorFieldInitializer = constructorFieldInitializer
    }
}

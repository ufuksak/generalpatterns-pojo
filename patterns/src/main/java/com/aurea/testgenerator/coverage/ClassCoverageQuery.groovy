package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import groovy.transform.Immutable

@Immutable(knownImmutableClasses = [Unit.class, ClassOrInterfaceDeclaration.class])
class ClassCoverageQuery {
    Unit unit
    ClassOrInterfaceDeclaration classOfTheMethod
    int anonymousClassIndex

    static ClassCoverageQuery of(Unit unit, ClassOrInterfaceDeclaration classOfTheMethod) {
        new ClassCoverageQuery(unit, classOfTheMethod, 0)
    }
}

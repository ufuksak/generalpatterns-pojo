package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.Immutable

@Immutable(knownImmutableClasses = [Unit.class, TypeDeclaration.class])
class ClassCoverageQuery {
    Unit unit
    TypeDeclaration type
    int anonymousClassIndex

    static ClassCoverageQuery of(Unit unit, TypeDeclaration type) {
        new ClassCoverageQuery(unit, type, 0)
    }
}

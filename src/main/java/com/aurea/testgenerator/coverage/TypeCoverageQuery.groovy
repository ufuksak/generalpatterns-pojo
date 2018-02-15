package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.Immutable

@Immutable(knownImmutableClasses = [Unit.class, TypeDeclaration.class])
class TypeCoverageQuery {
    Unit unit
    TypeDeclaration type
    int anonymousClassIndex

    static TypeCoverageQuery of(Unit unit, TypeDeclaration type) {
        new TypeCoverageQuery(unit, type, 0)
    }
}

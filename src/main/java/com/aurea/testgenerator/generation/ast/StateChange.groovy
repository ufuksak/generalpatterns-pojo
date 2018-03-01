package com.aurea.testgenerator.generation.ast

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import groovy.transform.Canonical


@Canonical
class StateChange {
    ResolvedFieldDeclaration resolvedField
    Expression assignment
    StateChangeVisitResultType type

    StateChange(StateChange stateChange) {
        this.resolvedField = stateChange.resolvedField
        this.assignment = stateChange.assignment
        this.type = stateChange.type
    }

    StateChange(ResolvedFieldDeclaration resolvedField,
                Expression assignment,
                StateChangeVisitResultType type) {
        this.resolvedField = resolvedField
        this.assignment = assignment
        this.type = type
    }
}

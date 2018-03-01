package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.generation.TestGeneratorError
import com.github.javaparser.ast.expr.Expression
import groovy.transform.Canonical
import one.util.streamex.StreamEx


@Canonical
class StateChangeVisitResult {

    List<StateChange> stateChanges
    TestGeneratorError error

    static StateChangeVisitResult noChanges() {
        new StateChangeVisitResult([])
    }

    StateChangeVisitResult unwind(ArgumentStack argumentStack) {
        Map<String, Expression> stack = argumentStack.stack
        List<StateChange> stateChanges = StreamEx.of(stateChanges).map { stateChange ->
            Expression assignment = stateChange.assignment
            if (assignment.nameExpr) {
                String referenceName = assignment.asNameExpr().nameAsString
                return new StateChange(
                        stateChange.resolvedField,
                        stack.getOrDefault(referenceName, stateChange.assignment),
                        stateChange.type
                )
            }
            return new StateChange(stateChange)
        }.toList()

        new StateChangeVisitResult(stateChanges: stateChanges, error: this.error)
    }

    boolean isSuccess() {
        stateChanges.stream().allMatch { it.type == StateChangeVisitResultType.SUCCESS }
    }

    boolean isFailed() {
        stateChanges.stream().anyMatch { it.type == StateChangeVisitResultType.FAILURE }
    }
}

package com.aurea.testgenerator.pattern.general

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.FieldAccessExpr

import java.util.function.BiPredicate

class FieldAccessExprPureFunctionPredicate implements BiPredicate<FieldAccessExpr, MethodContext> {
    @Override
    boolean test(FieldAccessExpr expr, MethodContext context) {
        String assignmentName = expr.nameAsString
        boolean isAssignment = expr.parentNode.map { it instanceof AssignExpr }.orElse(false)
        !(context.outOfMethodScope(assignmentName) && isAssignment)
    }
}

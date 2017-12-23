package com.aurea.methobase.meta.purity

import com.github.javaparser.ast.expr.ArrayAccessExpr
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.NameExpr

import java.util.function.BiPredicate

class ArrayAccessExprPureFunctionPredicate implements BiPredicate<ArrayAccessExpr, MethodContext> {
    @Override
    boolean test(ArrayAccessExpr expr, MethodContext context) {
        String assignmentName = (expr.name as NameExpr).nameAsString
        boolean isAssignment = expr.parentNode.map { it instanceof AssignExpr }.orElse(false)
        !(context.outOfMethodScope(assignmentName) && isAssignment)
    }
}

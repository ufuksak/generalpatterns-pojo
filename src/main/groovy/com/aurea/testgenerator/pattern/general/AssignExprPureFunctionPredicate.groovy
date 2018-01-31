package com.aurea.testgenerator.pattern.general

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.NameExpr

import java.util.function.BiPredicate

class AssignExprPureFunctionPredicate implements BiPredicate<AssignExpr, MethodContext> {
    @Override
    boolean test(AssignExpr expr, MethodContext context) {
        Expression target = expr.target
        if (target instanceof NameExpr) {
            return !context.outOfMethodScope(target.nameAsString)
        }
        return true
    }
}

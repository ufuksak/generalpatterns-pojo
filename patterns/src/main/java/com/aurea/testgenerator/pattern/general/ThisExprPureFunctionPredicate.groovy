package com.aurea.testgenerator.pattern.general

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.ThisExpr

import java.util.function.BiPredicate

class ThisExprPureFunctionPredicate implements BiPredicate<ThisExpr, MethodContext> {
    @Override
    boolean test(ThisExpr expr, MethodContext context) {
        return expr.parentNode
            .map { parentNode -> (parentNode instanceof FieldAccessExpr) &&
                parentNode.parentNode.map{ !(it instanceof AssignExpr) }.orElse(false)}
            .orElse(false)
    }
}

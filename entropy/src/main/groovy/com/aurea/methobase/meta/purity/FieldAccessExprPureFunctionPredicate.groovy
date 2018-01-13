package com.aurea.methobase.meta.purity

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade

import java.util.function.BiPredicate

class FieldAccessExprPureFunctionPredicate implements BiPredicate<FieldAccessExpr, JavaParserFacade> {
    @Override
    boolean test(FieldAccessExpr expr, JavaParserFacade context) {
        SimpleName assignment = expr.name
        boolean isAssignment = expr.parentNode.map { it instanceof AssignExpr }.orElse(false)
        !(new SimpleNamePureFunctionPredicate().test(assignment, context) && isAssignment)
    }
}

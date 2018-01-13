package com.aurea.methobase.meta.purity

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade

import java.util.function.BiPredicate

class AssignExprPureFunctionPredicate implements BiPredicate<AssignExpr, JavaParserFacade> {
    @Override
    boolean test(AssignExpr expr, JavaParserFacade context) {
        Expression target = expr.target
        if (target instanceof NameExpr) {
            return new NameExprPureFunctionPredicate().test(target, context)
        }
        return true
    }
}

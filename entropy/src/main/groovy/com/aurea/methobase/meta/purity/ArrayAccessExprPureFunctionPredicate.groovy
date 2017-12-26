package com.aurea.methobase.meta.purity

import com.github.javaparser.ast.expr.ArrayAccessExpr
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference

import java.util.function.BiPredicate

class ArrayAccessExprPureFunctionPredicate implements BiPredicate<ArrayAccessExpr, JavaParserFacade> {
    @Override
    boolean test(ArrayAccessExpr expr, JavaParserFacade context) {
        NameExpr nameExpr = expr.name as NameExpr
        boolean isAssignment = expr.parentNode.map { it instanceof AssignExpr }.orElse(false)
        !(isAssignment && !isVariable(nameExpr, context))
    }

    static boolean isVariable(NameExpr nameExpr, JavaParserFacade context) {
        SymbolReference<? extends ResolvedValueDeclaration> ref = context.solve(nameExpr)
        ref.solved && ref.correspondingDeclaration.variable
    }
}

package com.aurea.methobase.meta.purity

import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference

import java.util.function.BiPredicate

class NameExprPureFunctionPredicate implements BiPredicate<NameExpr, JavaParserFacade> {
    @Override
    boolean test(NameExpr expr, JavaParserFacade context) {
        SymbolReference<? extends ResolvedValueDeclaration> reference = context.solve(expr)
        reference.solved && (reference.correspondingDeclaration.parameter ||
                reference.correspondingDeclaration.variable ||
                isConstantField(reference.correspondingDeclaration))
    }

    private static boolean isConstantField(ResolvedValueDeclaration reference) {
        reference.isField() && reference.asField().isStatic() && reference.asField().isFinal()

    }
}

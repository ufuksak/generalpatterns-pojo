package com.aurea.methobase.meta.purity

import com.aurea.methobase.meta.JavaParserFacadeFactory
import com.github.javaparser.ast.expr.ThisExpr
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import com.jasongoodwin.monads.Try

import java.util.function.BiPredicate

class ThisExprPureFunctionPredicate implements BiPredicate<ThisExpr, JavaParserFacade> {
    @Override
    boolean test(ThisExpr expr, JavaParserFacade context) {
        SymbolReference<ResolvedTypeDeclaration> ref = Try.<SymbolReference<ResolvedTypeDeclaration>> ofFailable { context.solve(expr) }
                                                          .onFailure { JavaParserFacadeFactory.reportAsUnsolved(expr)}
                                                          .orElse(SymbolReference.unsolved(ResolvedTypeDeclaration))
        ref.solved && (ref.correspondingDeclaration.isParameter() ||
                ref.correspondingDeclaration.isField() &&
                ref.correspondingDeclaration.asField().isStatic() &&
                ref.correspondingDeclaration.asField().isFinal())
    }
}

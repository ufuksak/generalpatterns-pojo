package com.aurea.testgenerator.ast

import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import groovy.util.logging.Log4j2

@Log4j2
class FieldResolver {

    JavaParserFacade solver

    FieldResolver(JavaParserFacade solver) {
        this.solver = solver
    }

    Optional<ResolvedFieldDeclaration> resolve(FieldAccessExpr fieldAccessExpr) {
        try {
            SymbolReference<? extends ResolvedValueDeclaration> reference = solver.solve(fieldAccessExpr.name)
            if (reference.solved) {
                if (reference.correspondingDeclaration.field) {
                    return Optional.of(reference.correspondingDeclaration.asField())
                } else {
                    log.error("Solved reference of $fieldAccessExpr is not a field in ${ASTNodeUtils.getNameOfCompilationUnit(fieldAccessExpr)}")
                }
            } else {
                log.error "Failed to solve $fieldAccessExpr in ${ASTNodeUtils.getNameOfCompilationUnit(fieldAccessExpr)}"
            }
        } catch (UnsolvedSymbolException | com.github.javaparser.resolution.UnsolvedSymbolException use) {
            log.error "Failed to solve field access $fieldAccessExpr"
        }
        return Optional.empty()
    }
}

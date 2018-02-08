package com.aurea.testgenerator.extensions

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class FieldAccessExprExtension implements ASTExtension {

    FieldAccessExprExtension() {
        log.debug "Adding FieldAccessExpr::solve"
        FieldAccessExpr.metaClass.findField() { JavaParserFacade facade ->
            FieldAccessExpr n = delegate as FieldAccessExpr
            try {
                SymbolReference<? extends ResolvedValueDeclaration> reference = facade.solve(n.name)
                if (reference.solved) {
                    if (reference.correspondingDeclaration.field) {
                        return Optional.of(reference.correspondingDeclaration.asField())
                    } else {
                        log.error("Solved reference of $n is not a field in ${ASTNodeUtils.getNameOfCompilationUnit(n)}")
                    }
                } else {
                    log.error "Failed to solve $n in ${ASTNodeUtils.getNameOfCompilationUnit(n)}"
                }
            } catch (UnsolvedSymbolException use) {
                log.error "Failed to solve $n declared in ${ASTNodeUtils.getNameOfCompilationUnit(n)}"
            } catch (com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException use) {
                log.error "Failed to solve $n in ${ASTNodeUtils.getNameOfCompilationUnit(n)}"
            }
            Optional.empty()
        }
    }
}

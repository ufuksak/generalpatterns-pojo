package com.aurea.testgenerator.extensions

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
                        log.error("Solved reference of $n is not a field in ${n.findCompilationUnit()}")
                    }
                } else {
                    log.error "Failed to solve $n in ${n.findCompilationUnit()}"
                }
            } catch (UnsolvedSymbolException use) {
                log.error "Failed to solve $n in ${n.findCompilationUnit()}", use
            }
            Optional.empty()
        }
    }
}

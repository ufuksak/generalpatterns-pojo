package com.aurea.testgenerator.ast

import com.aurea.testgenerator.value.Resolution
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import groovy.util.logging.Log4j2

@Log4j2
class FieldResolver {

    JavaParserFacade solver

    FieldResolver(JavaParserFacade solver) {
        this.solver = solver
    }

    Optional<ResolvedFieldDeclaration> resolve(SimpleName name) {
        //There is a bug in JSS - if name of the field is the same as name of fieldAccessExpr.name it will resolve
        //reference to be a parameter declaration, so we need to work around that
        Optional<CallableDeclaration> callable = name.getAncestorOfType(CallableDeclaration)
        if (callable.present) {
            if (callable.get().parameters.any { it.name == name }) {
                //Solve outside of the callable scope
                Optional<TypeDeclaration> maybeType = callable.get().getAncestorOfType(TypeDeclaration)
                if (maybeType.present) {
                    TypeDeclaration typeDeclaration = maybeType.get()
                    Optional<? extends ResolvedTypeDeclaration> resolvedType = Optional.empty()
                    if (typeDeclaration.classOrInterfaceDeclaration) {
                        resolvedType = Resolution.tryResolve(typeDeclaration.asClassOrInterfaceDeclaration())
                    } else if (typeDeclaration.enumDeclaration) {
                        resolvedType = Resolution.tryResolve(typeDeclaration.asEnumDeclaration())
                    }
                    if (resolvedType.present) {
                        SymbolReference<? extends ResolvedValueDeclaration> resolvedValueDeclaration =
                                Resolution.trySolveSymbolInType(solver.symbolSolver, resolvedType.get(), name.identifier)
                        return asSolvedField(name, resolvedValueDeclaration)
                    }
                }
            }
            SymbolReference<? extends ResolvedValueDeclaration> reference = Resolution.trySolve(solver, name)
            return asSolvedField(name, reference)
        }
        return Optional.empty()
    }

    static Optional<ResolvedFieldDeclaration> asSolvedField(SimpleName name, SymbolReference<? extends ResolvedValueDeclaration> reference) {
        if (reference.solved) {
            if (reference.correspondingDeclaration.field) {
                return Optional.of(reference.correspondingDeclaration.asField())
            } else {
                log.error("Solved reference of $name is not a field in ${ASTNodeUtils.getNameOfCompilationUnit(name)}")
            }
        } else {
            log.error "Failed to solve $name in ${ASTNodeUtils.getNameOfCompilationUnit(name)}"
        }
        return Optional.empty()
    }
}

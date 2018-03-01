package com.aurea.testgenerator.value

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.Resolvable
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import com.github.javaparser.symbolsolver.resolution.SymbolSolver
import com.jasongoodwin.monads.Try


class Resolution {
    static <T> Optional<T> tryResolve(Resolvable<T> resolvable) {
        Try.ofFailable { resolvable.resolve() }.toOptional()
    }

    static Optional<ResolvedType> tryResolve(Type type) {
        Try.ofFailable { type.resolve() }.toOptional()
    }

    static Optional<ResolvedType> tryCalculateResolvedType(Expression expression) {
        Try.ofFailable { expression.calculateResolvedType() }.toOptional()
    }

    static Optional<ResolvedType> tryGetType(ResolvedFieldDeclaration field) {
        Try.ofFailable { field.getType() }.toOptional()
    }

    static Optional<ResolvedMethodDeclaration> tryResolve(MethodCallExpr methodCall) {
        Try.ofFailable { methodCall.resolveInvokedMethod() }.toOptional()
    }

    static SymbolReference<? extends ResolvedValueDeclaration> trySolveSymbolInType(SymbolSolver symbolSolver, ResolvedTypeDeclaration typeDeclaration, String name) {
        Try.ofFailable { symbolSolver.solveSymbolInType(typeDeclaration, name) }
           .orElse(SymbolReference.unsolved(ResolvedValueDeclaration))
    }

    static SymbolReference<? extends ResolvedValueDeclaration> trySolve(JavaParserFacade solver, SimpleName name) {
        Try.ofFailable { solver.solve(name) }
           .orElse(SymbolReference.unsolved(ResolvedValueDeclaration))
    }

    static Optional<ResolvedConstructorDeclaration> tryResolveInvokedConstructor(ObjectCreationExpr objectCreationExpr) {
        Try.ofFailable { objectCreationExpr.resolveInvokedConstructor() }.toOptional()
    }

    static Optional<ResolvedConstructorDeclaration> tryResolveInvokedConstructor(ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt) {
        Try.ofFailable { explicitConstructorInvocationStmt.resolveInvokedConstructor() }.toOptional()
    }
}

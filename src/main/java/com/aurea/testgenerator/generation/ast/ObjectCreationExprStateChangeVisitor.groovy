package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.generation.TestGeneratorError
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.nodeTypes.NodeWithArguments
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration

import static com.aurea.testgenerator.value.Resolution.tryResolveInvokedConstructor

class ObjectCreationExprStateChangeVisitor implements StateChangeVisitor {

    ObjectCreationExpr objectCreation
    JavaParserFacade solver
    ArgumentStack stack

    ObjectCreationExprStateChangeVisitor(JavaParserFacade solver, ObjectCreationExpr objectCreation, ArgumentStack stack) {
        this.solver = solver
        this.objectCreation = objectCreation
        this.stack = stack
    }

    @Override
    StateChangeVisitResult visit() {
        StateChangeVisitResult result = tryResolveInvokedConstructor(objectCreation)
                .map { visitResolvedConstructor(it, objectCreation) }
                .orElse(StateChangeVisitResult.noChanges())
        result.unwind(stack)
    }

    StateChangeVisitResult visitResolvedConstructor(ResolvedConstructorDeclaration resolvedConstructor, ExplicitConstructorInvocationStmt explicitConstructor) {
        visitResolvedConstructor(resolvedConstructor, explicitConstructor.arguments)
    }

    StateChangeVisitResult visitResolvedConstructor(ResolvedConstructorDeclaration resolvedConstructor, NodeWithArguments caller) {
        visitResolvedConstructor(resolvedConstructor, caller.arguments)
    }

    StateChangeVisitResult visitResolvedConstructor(ResolvedConstructorDeclaration resolvedConstructor, Collection<Expression> arguments) {
        asConstructorDeclaration(resolvedConstructor)
                .map { constructor ->
            stack.goDeeper(constructor, arguments)
            StateChangeVisitResult visitedThisOrSuper = visitThisOrSuper(constructor)
            List<StateChange> stateChanges = new StateChangeDetector(solver, constructor)
                    .findChanges({ it.target.asFieldAccessExpr().scope.thisExpr })
            visitedThisOrSuper.stateChanges += stateChanges
            return visitedThisOrSuper
        }.orElse(StateChangeVisitResult.noChanges())
    }

    StateChangeVisitResult visitThisOrSuper(ConstructorDeclaration constructor) {
        constructor.findFirst(ExplicitConstructorInvocationStmt).map { explicitConstructor ->
            tryResolveInvokedConstructor(explicitConstructor).map { resolvedConstructor ->
                asConstructorDeclaration(resolvedConstructor).map { invokedConstructor ->
                    stack.goDeeper(invokedConstructor, explicitConstructor)
                    visitResolvedConstructor(resolvedConstructor, explicitConstructor)
                }.orElse(StateChangeVisitResult.noChanges())
            }.orElse(StateChangeVisitResult.noChanges())
        }.orElse(StateChangeVisitResult.noChanges())
    }

    private static Optional<ConstructorDeclaration> asConstructorDeclaration(ResolvedConstructorDeclaration resolvedConstructor) {
        //If it is a default constructor - not really interested in it
        //Sadly it is not accessible, so check by class name
        if (resolvedConstructor.class.simpleName == 'DefaultConstructorDeclaration') {
            return Optional.empty()
        }
        //Assume that constructor is declared in the same unit,
        //hence guaranteed to be JavaParser resolution
        if (!(resolvedConstructor instanceof JavaParserConstructorDeclaration)) {
            throw new TestGeneratorError("Expected $resolvedConstructor to be " +
                    "JavaParserConstructorDeclaration")
        }
        return Optional.of((resolvedConstructor as JavaParserConstructorDeclaration).wrappedNode)
    }
}

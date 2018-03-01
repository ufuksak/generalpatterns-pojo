package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.value.Resolution
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration
import groovy.util.logging.Log4j2

/**
 *  Checks ObjectCreation and assignment (setters/field). Doesn't check alien method calls. Actually,
 *  it might be a good idea to fail if there are method calls that consume tracked expression. Who
 *  knows what lies inside...
 */
@Log4j2
class MethodStateChangeVisitor implements StateChangeVisitor {

    JavaParserFacade solver
    MethodDeclaration context
    Expression tracked
    StateChangeDetector stateChangeDetector
    ArgumentStack stack

    MethodStateChangeVisitor(JavaParserFacade solver, MethodDeclaration context, Expression tracked, ArgumentStack stack) {
        this.stateChangeDetector = new StateChangeDetector(solver, context)
        this.solver = solver
        this.context = context
        this.tracked = tracked
        this.stack = stack
    }

    @Override
    StateChangeVisitResult visit() {
        try {
            if (tracked.objectCreationExpr) {
                //Just simple call to the constructor, like return new Foo(123);
                return new ObjectCreationExprStateChangeVisitor(solver, tracked.asObjectCreationExpr(), stack)
                        .visit()
            }

            if (!tracked.nameExpr) {
                throw new TestGeneratorError("Unknown type of expression $tracked ! " +
                        "Expected nameExpr or objectCreationExpr")
            }

            NameExpr name = tracked.asNameExpr()

            ResolvedValueDeclaration initialization = Resolution.tryResolve(name).orElseThrow {
                TestGeneratorError.unsolved(tracked)
            }
            if (!initialization.variable || !(initialization instanceof JavaParserSymbolDeclaration)) {
                log.debug "Expression [$tracked] was not a variable it was [$initialization]." +
                        "Only variables are supported. "
                return StateChangeVisitResult.noChanges()
            }
            Optional<StateChangeVisitResult> initializationChanges = checkInitializationForChanges(initialization as JavaParserSymbolDeclaration)
            List<StateChange> changes = stateChangeDetector.findChanges({ it.target.asFieldAccessExpr().scope == tracked })
            StateChangeVisitResult result = initializationChanges.orElse(StateChangeVisitResult.noChanges())
            result.stateChanges.addAll(changes)
            return result.unwind(stack)
        } catch (TestGeneratorError tge) {
            return new StateChangeVisitResult(error: tge, stateChanges: [])
        }
    }

    private Optional<StateChangeVisitResult> checkInitializationForChanges(JavaParserSymbolDeclaration symbol) {
        Node wrapped = symbol.wrappedNode
        VariableDeclarator variable = (wrapped as VariableDeclarator)
        variable.initializer.flatMap { initializer ->
            if (initializer.objectCreationExpr) {
                ObjectCreationExpr objectCreation = initializer.asObjectCreationExpr()
                return Optional.of(new ObjectCreationExprStateChangeVisitor(solver, objectCreation, stack)
                        .visit())
            }
            log.debug "Initializer [$initializer] was not object creation expr. " +
                    "Only object creation expressions are supported."
            Optional.empty()
        }
    }
}

package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.generation.patterns.pojos.PojoFieldFinder
import com.aurea.testgenerator.value.Resolution
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade

import java.util.function.Predicate


class StateChangeDetector {

    JavaParserFacade solver
    Node context

    StateChangeDetector(JavaParserFacade solver, Node context) {
        this.solver = solver
        this.context = context
    }

    List<StateChange> findChanges(Predicate<AssignExpr> assignExprPredicate) {
        List<StateChange> stateChanges = []

        context.findAll(Expression).findAll {
            StateChangeFunctions.isNoAlienAssignment(it) && assignExprPredicate.test(it.asAssignExpr()) ||
                    StateChangeFunctions.isNoAlienMethodsSetterCall(it)
        }.each { stateChangingExpr ->
            Optional<StateChange> maybeStateChange = Optional.empty()
            if (stateChangingExpr.assignExpr) {
                maybeStateChange = assignExprToStateChange(stateChangingExpr.asAssignExpr())
            } else if (stateChangingExpr.methodCallExpr) {
                maybeStateChange = setterCallToStateChange(stateChangingExpr.asMethodCallExpr())
            }
            maybeStateChange.ifPresent {
                stateChanges << it
            }
        }

        stateChanges
    }

    private Optional<StateChange> assignExprToStateChange(AssignExpr assign) {
        FieldAccessExpr fieldAccess = assign.target.asFieldAccessExpr()
        FieldResolver resolver = new FieldResolver(solver)
        resolver.resolve(fieldAccess.name).map { resolvedField ->
            new StateChange(
                    resolvedField,
                    assign.value,
                    StateChangeVisitResultType.SUCCESS)
        }
    }

    private static Optional<StateChange> setterCallToStateChange(MethodCallExpr setterCall) {
        Resolution.tryResolve(setterCall).flatMap { resolvedSetter ->
            PojoFieldFinder.findSetterField(resolvedSetter).map { resolvedField ->
                new StateChange(
                        resolvedField,
                        setterCall.arguments.first(),
                        StateChangeVisitResultType.SUCCESS)
            }
        }
    }
}

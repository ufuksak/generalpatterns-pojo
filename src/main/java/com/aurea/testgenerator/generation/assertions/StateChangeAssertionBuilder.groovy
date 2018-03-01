package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.ast.FieldAccessBuilder
import com.aurea.testgenerator.ast.FieldAccessResult
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.ast.MethodStateChangeVisitor
import com.aurea.testgenerator.generation.ast.ArgumentStack
import com.aurea.testgenerator.generation.ast.StateChangeVisitResult
import com.aurea.testgenerator.generation.ast.StateChangeVisitResultType
import com.aurea.testgenerator.generation.ast.StateChangeVisitor
import com.aurea.testgenerator.value.Resolution
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2

import static com.aurea.testgenerator.generation.ast.StateChangeFunctions.onlyLastChanges

@Log4j2
class StateChangeAssertionBuilder extends SimpleAssertionBuilder {

    Expression verifiedExpression
    JavaParserFacade solver
    StateChangeVisitor visitor

    StateChangeAssertionBuilder(Expression trackedExpression,
                                Expression verifiedExpression,
                                MethodDeclaration context,
                                JavaParserFacade solver) {
        this.verifiedExpression = verifiedExpression
        this.solver = solver
        this.visitor = new MethodStateChangeVisitor(solver, context, trackedExpression, new ArgumentStack())
    }

    @Override
    List<DependableNode<MethodCallExpr>> getAssertions() {
        StateChangeVisitResult stateChangeVisitResult = visitor.visit()
        StateChangeVisitResult lastStateChangeVisitResult = onlyLastChanges(stateChangeVisitResult)
        if (lastStateChangeVisitResult.failed) {
            throw lastStateChangeVisitResult.error
        }

        FieldAccessBuilder fieldAccessBuilder = new FieldAccessBuilder(verifiedExpression)
        lastStateChangeVisitResult.stateChanges
                              .findAll { it.type != StateChangeVisitResultType.NONCOMPREHENSIVE }
                              .each { stateChange ->
            ResolvedFieldDeclaration resolvedField = stateChange.resolvedField
            FieldAccessResult fieldAccessResult = fieldAccessBuilder.build(resolvedField)
            if (fieldAccessResult.success) {
                Resolution.tryGetType(resolvedField).ifPresent { fieldType ->
                    with(fieldType, fieldAccessResult.expression, stateChange.assignment)
                }
            } else if (fieldAccessResult.failed) {
                throw fieldAccessResult.error
            }
            log.debug "Field $resolvedField is not accessible from tests, skipping assertion"
        }
        return this.@assertions
    }
}

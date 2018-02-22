package com.aurea.testgenerator.generation.patterns.methods

import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestGeneratorErrorContainer
import com.aurea.testgenerator.generation.patterns.constructors.Pojos
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import one.util.streamex.StreamEx

import java.util.function.BinaryOperator

class StateChangeAssertionBuilder {

    Expression trackedExpression
    Node context
    JavaParserFacade solver
    TestGeneratorErrorContainer errorContainer

    StateChangeAssertionBuilder(Expression trackedExpression, Node context,
                                JavaParserFacade solver,
                                TestGeneratorErrorContainer errorContainer) {
        this.trackedExpression = trackedExpression
        this.context = context
        this.solver = solver
        this.errorContainer = errorContainer
    }
    
    List<DependableNode<Statement>> buildAssertions() {
        AssertionBuilder builder = new AssertionBuilder()
        AssignAssertionBuilder assertionBuilder = new AssignAssertionBuilder(trackedExpression, solver, errorContainer, builder)
        Collection<AssignExpr> assignExprs = findAssignments()
        assertionBuilder.withAssertions(assignExprs)

        SetterAssertionBuilder setterAssertionBuilder = new SetterAssertionBuilder(trackedExpression, solver, errorContainer, builder)
        Collection<MethodCallExpr> setterCalls = findSetterCalls()
        setterAssertionBuilder.withAssertions(setterCalls)
        builder.softly((assignExprs.size() + setterCalls.size()) > 1).build()
    }

    private Collection<AssignExpr> findAssignments() {
        List<AssignExpr> assignExprs = context.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = FieldAssignments.findLastAssignExpressionsByField(assignExprs)
        Collection<AssignExpr> targetingExprAssignments = onlyLastAssignExprs.findAll {
            it.target.fieldAccessExpr && it.target.asFieldAccessExpr().scope == trackedExpression
        }
        targetingExprAssignments
    }

    private Collection<MethodCallExpr> findSetterCalls() {
        List<MethodCallExpr> setterCallsOnExpression = context
                .findAll(MethodCallExpr)
                .findAll { it.scope.present && it.scope.get() == trackedExpression && Pojos.isSetterCall(it) }

        findLastSetterCalls(setterCallsOnExpression)
    }

    private static Collection<MethodCallExpr> findLastSetterCalls(Collection<MethodCallExpr> methodCalls) {
        Map<SimpleName, MethodCallExpr> mapByName = StreamEx.of(methodCalls)
                                                            .toMap(
                { it.name },
                { it },
                { mce1, mce2 -> mce2 } as BinaryOperator<MethodCallExpr>)
        mapByName.values()
    }

}

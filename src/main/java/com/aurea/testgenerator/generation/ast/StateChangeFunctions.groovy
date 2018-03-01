package com.aurea.testgenerator.generation.ast

import com.aurea.testgenerator.generation.patterns.pojos.Pojos
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithArguments
import com.github.javaparser.ast.nodeTypes.NodeWithParameters
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt
import one.util.streamex.EntryStream
import one.util.streamex.IntStreamEx
import one.util.streamex.StreamEx

import java.util.function.BinaryOperator

class StateChangeFunctions {

    static boolean isNoAlienAssignment(Expression expr) {
        expr.assignExpr &&
                expr.asAssignExpr().target.fieldAccessExpr &&
                !expr.asAssignExpr().value.methodCallExpr
    }

    static boolean assignsToScope(FieldAccessExpr expr, Expression scope) {
        expr.scope == scope
    }

    static boolean isNoAlienMethodsSetterCall(Expression expr) {
        expr.methodCallExpr && Pojos.isSetterCall(expr.asMethodCallExpr()) &&
                expr.asMethodCallExpr().arguments.stream().noneMatch { it.methodCallExpr }
    }

    static StateChangeVisitResult onlyLastChanges(StateChangeVisitResult result) {
        Map<String, StateChange> mapByName = StreamEx.of(result.stateChanges)
                                                        .toMap(
                { it.resolvedField.name },
                { it },
                { sc1, sc2 -> sc2 } as BinaryOperator<StateChange>)

        List<StateChange> lastChanges = mapByName.values().toList()
        return new StateChangeVisitResult(stateChanges: lastChanges, error: result.error)
    }

    static StateChangeVisitResult unwindPassedArguments(StateChangeVisitResult stateChangeVisitResult,
                                                                NodeWithParameters called,
                                                                NodeWithArguments call) {
        unwindPassedArguments(stateChangeVisitResult, called.parameters, call.arguments)
    }

    static StateChangeVisitResult unwindPassedArguments(StateChangeVisitResult stateChangeVisitResult,
                                                                NodeWithParameters called,
                                                                ExplicitConstructorInvocationStmt explicitConstructor) {
        unwindPassedArguments(stateChangeVisitResult, called.parameters, explicitConstructor.arguments)
    }

    static StateChangeVisitResult unwindPassedArguments(StateChangeVisitResult stateChangeVisitResult,
                                                        Collection<Parameter> parameters,
                                                        Collection<Expression> arguments) {
        Map<String, Expression> callMap = buildCallMap(parameters, arguments)
        List<StateChange> stateChanges = StreamEx.of(stateChangeVisitResult.stateChanges).map { stateChange ->
            Expression assignment = stateChange.assignment
            if (assignment.nameExpr) {
                String referenceName = assignment.asNameExpr().nameAsString
                return new StateChange(
                        stateChange.resolvedField,
                        callMap.getOrDefault(referenceName, stateChange.assignment),
                        stateChange.type
                )
            }
            return new StateChange(stateChange)
        }.toList()

        new StateChangeVisitResult(stateChanges: stateChanges, error: stateChangeVisitResult.error)
    }


    private static Map<String, Expression> buildCallMap(NodeWithParameters called, ExplicitConstructorInvocationStmt explicitConstructor) {
        buildCallMap(called.parameters, explicitConstructor.arguments)
    }

    private static Map<String, Expression> buildCallMap(Collection<Parameter> parameters, Collection<Expression> arguments) {
        assert parameters.size() == arguments.size()

        IntStreamEx.range(0, parameters.size())
                   .boxed()
                   .toMap(
                { parameters[it].nameAsString },
                { arguments[it] })
    }
}

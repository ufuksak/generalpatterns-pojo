package com.aurea.testgenerator.ast

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.SimpleName
import one.util.streamex.StreamEx

import java.util.function.BinaryOperator

class FieldAssignments {

    static Collection<AssignExpr> findLastAssignExpressionsByField(List<AssignExpr> exprs) {
        Map<SimpleName, AssignExpr> mapByName = StreamEx.of(exprs)
                                                        .filter { it.target.fieldAccessExpr }
                                                        .toMap(
                { it.target.asFieldAccessExpr().name },
                { it },
                { ae1, ae2 -> ae2 } as BinaryOperator<AssignExpr>)

        mapByName.values()
    }
}

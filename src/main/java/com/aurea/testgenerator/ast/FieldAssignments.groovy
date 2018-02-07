package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.source.FieldAccessorBuilder
import com.aurea.testgenerator.generation.source.StaticFieldAccessorBuilder
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.function.BinaryOperator

@Component
class FieldAssignments {

    JavaParserFacade solver

    @Autowired
    FieldAssignments(JavaParserFacade solver) {
        this.solver = solver
    }

    Collection<AssignExpr> findLastAssignExpressionsByField(List<AssignExpr> exprs) {
        Map<SimpleName, AssignExpr> mapByName = StreamEx.of(exprs)
                                                        .filter { it.target.fieldAccessExpr }
                                                        .toMap(
                { it.target.asFieldAccessExpr().name },
                { it },
                { ae1, ae2 -> ae2 } as BinaryOperator<AssignExpr>)

        mapByName.values()
    }

    Optional<Expression> buildFieldAccessExpression(AssignExpr assignExpr, Expression scope) {
        FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
        Optional<ResolvedFieldDeclaration> maybeField = fieldAccessExpr.findField(solver)
        Optional<Expression> maybeFieldAccessExpression = maybeField.flatMap { field ->
            buildAccessToField(field, scope)
        }
    }

    static Optional<Expression> buildAccessToField(ResolvedFieldDeclaration rfd, Expression scope) {
        if (rfd.static) {
            return new StaticFieldAccessorBuilder(rfd).build()
        } else {
            return new FieldAccessorBuilder(rfd, scope).build()
        }
    }
}

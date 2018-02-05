package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.pattern.PatternType
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.stmt.Statement


enum ConstructorPatterns implements PatternType {
    EMPTY {
        @Override
        boolean is(ConstructorDeclaration cd, Unit unit) {
            cd.body.empty
        }
    },
    FIELD_LITERAL_ASSIGNMENTS {
        @Override
        boolean is(ConstructorDeclaration cd, Unit unit) {
            for (Statement statement: cd.body.statements) {
                if (!statement.expressionStmt) {
                    return false
                } else {
                    Expression expression = statement.asExpressionStmt().expression
                    if (!expression.assignExpr) {
                        return false
                    }
                    AssignExpr assignExpr = expression.asAssignExpr()
                    return assignExpr.targetsThis() && assignExpr.value.literalExpr
                }
            }
            return false
        }
    }

    abstract boolean is(ConstructorDeclaration cd, Unit unit)
}

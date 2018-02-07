package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.pattern.PatternType
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.Statement


enum ConstructorPatterns implements PatternType {
    IS_EMPTY{
        @Override
        boolean is(ConstructorDeclaration cd, Unit unit) {
            cd.body.empty
        }
    },
    HAS_FIELD_LITERAL_ASSIGNMENTS{
        @Override
        boolean is(ConstructorDeclaration cd, Unit unit) {
            for (Statement statement : cd.body.statements) {
                if (statement.expressionStmt) {
                    Expression expression = statement.asExpressionStmt().expression
                    if (expression.assignExpr) {
                        AssignExpr assignExpr = expression.asAssignExpr()
                        if (assignExpr.value.literalExpr && assignExpr.targetsThis()) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    },
    HAS_ARGUMENT_ASSIGNMENTS {
        @Override
        boolean is(ConstructorDeclaration cd, Unit unit) {
            for (Statement statement: cd.body.statements) {
                if (statement.expressionStmt) {
                    Expression expression = statement.asExpressionStmt().expression
                    if (expression.assignExpr) {
                        AssignExpr assignExpr = expression.asAssignExpr()
                        if (assignExpr.value.nameExpr && cd.isNameOfArgument(assignExpr.value.asNameExpr().name) && assignExpr.targetsThis()) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }

    abstract boolean is(ConstructorDeclaration cd, Unit unit)
}

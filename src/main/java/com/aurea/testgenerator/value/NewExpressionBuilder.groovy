package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.type.Type

class NewExpressionBuilder {

    static DependableNode<Expression> buildDependableNode(String type) {
        DependableNode.from(buildExpression(type))
    }

    static Expression buildExpression(String type) {
        JavaParser.parseExpression("new ${type}()")
    }

    static DependableNode<VariableDeclarationExpr> getNewVariable(String name, Type type) {
        Expression newExpression = NewExpressionBuilder.buildExpression(type.toString())
        VariableDeclarator variableDeclarator = new VariableDeclarator(type.clone(), name, newExpression)
        DependableNode.from(new VariableDeclarationExpr(variableDeclarator))
    }
}

package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.source.Imports
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression

class NewExpressionBuilder {

    static DependableNode<Expression> buildDependableNode(String type) {
        DependableNode.from(builExpression(type))
    }

    static Expression builExpression(String type){
        JavaParser.parseExpression("new ${type}()")
    }
}

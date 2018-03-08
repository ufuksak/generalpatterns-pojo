package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.source.Imports
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression

class NewExpressionBuilder {

    static DependableNode<Expression> build(String type) {
        DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression("new ${type}()"))
    }
}

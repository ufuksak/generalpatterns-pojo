package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.source.Imports
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression

class MockExpressionBuilder {

    static DependableNode<Expression> build(String type) {
        DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression("mock(${type}.class, RETURNS_DEEP_STUBS)"))
        expression.dependency.imports << Imports.STATIC_MOCK
        expression.dependency.imports << Imports.STATIC_RETURNS_DEEP_STUBS
        expression
    }
}

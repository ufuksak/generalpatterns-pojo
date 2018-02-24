package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.resolution.types.ResolvedPrimitiveType

interface PrimitiveValueFactory {
    DependableNode<Expression> get(PrimitiveType type)

    DependableNode<Expression> get(ResolvedPrimitiveType type)
}

package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.TestNodeExpression
import com.github.javaparser.ast.type.PrimitiveType


interface PrimitiveValueFactory {
    TestNodeExpression get(PrimitiveType type)
}

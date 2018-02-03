package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestNodeVariable
import com.github.javaparser.ast.type.Type

interface ValueFactory {

    Optional<TestNodeExpression> getExpression(Type type)

    Optional<TestNodeVariable> getVariable(Type type)
}
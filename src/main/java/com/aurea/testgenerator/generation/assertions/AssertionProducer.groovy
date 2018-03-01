package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.expr.MethodCallExpr


interface AssertionProducer {
    List<DependableNode<MethodCallExpr>> getAssertions()
}

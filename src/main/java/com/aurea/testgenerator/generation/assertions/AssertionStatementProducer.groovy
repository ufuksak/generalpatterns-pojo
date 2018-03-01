package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.stmt.Statement


interface AssertionStatementProducer {
    List<DependableNode<Statement>> getStatements()
}

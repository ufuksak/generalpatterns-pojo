package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.DependableNode
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.resolution.types.ResolvedReferenceType

interface ReferenceTypeFactory {

    Optional<DependableNode<Expression>> get(ResolvedReferenceType type)

    void setValueFactory(ValueFactory valueFactory)

}
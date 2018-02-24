package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.types.ResolvedType

interface ValueFactory {

    Optional<DependableNode<Expression>> getExpression(ResolvedType type)

    DependableNode<Expression> getStubExpression(Type type)

    Optional<DependableNode<VariableDeclarationExpr>> getVariable(String name, Type type)
}
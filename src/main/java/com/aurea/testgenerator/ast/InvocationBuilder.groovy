package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.TestDependencyMerger
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.ObjectCreationExpr
import one.util.streamex.StreamEx

class InvocationBuilder {

    ValueFactory factory

    InvocationBuilder(ValueFactory factory) {
        this.factory = factory
    }

    Optional<TestNodeExpression> build(ConstructorDeclaration cd) {
        if (!Callability.isCallableFromTests(cd)) {
            return Optional.empty()
        }
        List<TypeDeclaration> parents = ASTNodeUtils.parents(cd, TypeDeclaration).toList()
        if (parents.size() == 1) {
            TestNodeExpression expr = new TestNodeExpression()
            List<TestNodeExpression> parameterExpressions = StreamEx.of(cd.parameters).map { parameter ->
                factory.getExpression(parameter.type).orElseThrow {
                    throw new IllegalArgumentException("Failed to create expression for ${parameter} of ${cd}")
                }
            }.toList()
            expr.dependency = TestDependencyMerger.merge(StreamEx.of(parameterExpressions.stream().map {it.dependency}).toList())
            expr.expr = new ObjectCreationExpr(null,
                    JavaParser.parseClassOrInterfaceType(cd.nameAsString),
                    NodeList.nodeList(StreamEx.of(parameterExpressions).map {it.expr}.toList()))
            return Optional.of(expr)
        }

        Optional.empty()
    }
}

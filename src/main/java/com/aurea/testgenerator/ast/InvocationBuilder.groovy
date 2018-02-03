package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.TestDependencyMerger
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalScope
import com.github.javaparser.ast.type.ClassOrInterfaceType
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
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
            return Optional.of(buildConstructorInvocation(cd))
        } else {
            boolean previousParentStatic = parents.first().static
            TestNodeExpression expr = buildConstructorInvocation(cd)
            for (int i = 1; i < parents.size(); i++) {
                TypeDeclaration parent = parents[i]
                if (previousParentStatic) {
                    prependWithScope(expr, parent.nameAsString)
                } else {
                    ConstructorDeclaration simplestConstructor = findSimplestConstructor(parent)
                    TestNodeExpression invocation = buildConstructorInvocation(simplestConstructor)
                    prependWithInvocation(invocation, expr)
                }
                previousParentStatic = parent.static
            }
            return Optional.of(expr)
        }
    }

    private TestNodeExpression buildConstructorInvocation(ConstructorDeclaration cd) {
        TestNodeExpression expr = new TestNodeExpression()
        List<TestNodeExpression> parameterExpressions = StreamEx.of(cd.parameters).map { parameter ->
            factory.getExpression(parameter.type).orElseThrow {
                throw new IllegalArgumentException("Failed to create expression for ${parameter} of ${cd}")
            }
        }.toList()
        expr.dependency = TestDependencyMerger.merge(StreamEx.of(parameterExpressions.stream().map { it.dependency }).toList())
        expr.expr = new ObjectCreationExpr(null,
                JavaParser.parseClassOrInterfaceType(cd.nameAsString),
                NodeList.nodeList(StreamEx.of(parameterExpressions).map { it.expr }.toList()))
        expr
    }

    private static void prependWithScope(TestNodeExpression expr, String scopeName) {
        NodeWithOptionalScope expressionWithScope = expr.expr.asObjectCreationExpr()
        ObjectCreationExpr topObjectCreationExpr = findTopObjectCreationExpr(expressionWithScope)
        topObjectCreationExpr.setType(JavaParser.parseClassOrInterfaceType(scopeName + "." + topObjectCreationExpr.getType().nameAsString))
    }

    private static void prependWithInvocation(TestNodeExpression invocation, TestNodeExpression expr) {
        appendParentScope(expr.expr.asObjectCreationExpr(), invocation.expr)
    }

    private static ObjectCreationExpr findTopObjectCreationExpr(ObjectCreationExpr expr) {
        if (expr.scope.present) {
            return findTopObjectCreationExpr(expr.scope.get().asObjectCreationExpr())
        } else {
            return expr
        }
    }

    private static appendParentScope(NodeWithOptionalScope expr, Expression scope) {
        if (expr.scope.present) {
            appendParentScope(expr.scope.get().asObjectCreationExpr(), scope)
        } else {
            expr.setScope(scope)
        }
    }

    private static ConstructorDeclaration findSimplestConstructor(TypeDeclaration td) {
        NodeList<ConstructorDeclaration> constructorDeclarations = td.findAll(ConstructorDeclaration)
        if (constructorDeclarations.empty) {
            //Nice, we can just use default constructor
            return new ConstructorDeclaration(td.nameAsString)
        } else {
            Optional<ConstructorDeclaration> cd = StreamEx.of(constructorDeclarations)
                                                          .filter { !it.private }
                                                          .sortedBy { it.parameters.size() }
                                                          .findFirst()
            return cd.orElseThrow { new IllegalStateException("Failed to find accessible constructor in $td. It is not invocable!")}
        }
    }
}

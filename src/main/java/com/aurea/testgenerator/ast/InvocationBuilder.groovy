package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.EnumConstantDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithConstructors
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalScope
import com.github.javaparser.ast.type.ClassOrInterfaceType
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class InvocationBuilder {

    ValueFactory factory
    Map<SimpleName, TestNodeExpression> expressionsForParameters

    InvocationBuilder(ValueFactory factory) {
        this.factory = factory
    }

    InvocationBuilder usingForParameters(Map<SimpleName, TestNodeExpression> expressionsForParameters) {
        this.expressionsForParameters = expressionsForParameters
        this
    }

    Optional<TestNodeExpression> build(ConstructorDeclaration cd) {
        if (!Callability.isCallableFromTests(cd)) {
            return Optional.empty()
        }
        List<TypeDeclaration> parents = ASTNodeUtils.parents(cd, TypeDeclaration).toList()
        if (parents.size() == 1) {
            return Optional.of(buildConstructorInvocation(cd))
        } else {
            if (parents.empty) {
                log.error "No type declaration for $cd"
            }
            boolean isParentStatic = parents.first().static
            TestNodeExpression expr = buildConstructorInvocation(cd)
            for (int i = 1; i < parents.size(); i++) {
                TypeDeclaration parent = parents[i]
                if (isParentStatic) {
                    prependWithScope(expr, parent.nameAsString)
                } else {
                    if (parent.enumDeclaration) {
                        Optional<FieldAccessExpr> accessFirst = parent.asEnumDeclaration().accessFirst()
                        if (!accessFirst.present) {
                            return Optional.empty()
                        }
                        appendParentScope(expr.node.asObjectCreationExpr(), accessFirst.get())
                    } else if (defaultConstructor(parent)) {
                        TestNodeExpression defaultInvocation = new TestNodeExpression(
                                node: new ObjectCreationExpr(null,
                                        JavaParser.parseClassOrInterfaceType(parent.nameAsString),
                                        NodeList.nodeList())
                        )
                        prependWithInvocation(defaultInvocation, expr)
                    } else {
                        Optional<ConstructorDeclaration> simplestConstructor = findSimplestConstructor(parent)
                        if (!simplestConstructor.present) {
                            return Optional.empty()
                        }
                        TestNodeExpression invocation = buildConstructorInvocation(simplestConstructor.get())
                        prependWithInvocation(invocation, expr)
                    }
                }
                isParentStatic = parent.static
            }
            return Optional.of(expr)
        }
    }

    private TestNodeExpression buildConstructorInvocation(ConstructorDeclaration cd) {
        TestNodeExpression expr = new TestNodeExpression()
        List<TestNodeExpression> parameterExpressions
        if (expressionsForParameters) {
            parameterExpressions = getFromGivenParameters(cd)
        } else {
            parameterExpressions = StreamEx.of(cd.parameters).map { parameter ->
                factory.getExpression(parameter.type).orElseThrow {
                    throw new IllegalArgumentException("Failed to create expression for ${parameter} of ${cd}")
                }
            }.toList()
        }
        expr.dependency = TestNodeMerger.merge(StreamEx.of(parameterExpressions.stream().map { it.dependency }).toList())
        expr.node = new ObjectCreationExpr(null,
                JavaParser.parseClassOrInterfaceType(cd.nameAsString),
                NodeList.nodeList(StreamEx.of(parameterExpressions).map { it.node }.toList()))
        expr
    }

    private List<TestNodeExpression> getFromGivenParameters(ConstructorDeclaration cd) {
        StreamEx.of(cd.parameters).map { parameter ->
            Optional.ofNullable(expressionsForParameters.get(parameter.name)).orElseThrow {
                throw new IllegalArgumentException("Failed to find a parameter value for $parameter in $cd. " +
                        "Only $expressionsForParameters were provided!")
            }
        }.toList()
    }

    private static void prependWithScope(TestNodeExpression expr, String scopeName) {
        ObjectCreationExpr parentScope = findParentObjectCreationScope(expr.node.asObjectCreationExpr())
        ClassOrInterfaceType parentTypeScope = findParentTypeScope(parentScope.type)
        parentTypeScope.setScope(JavaParser.parseClassOrInterfaceType(scopeName))
    }

    private static void prependWithInvocation(TestNodeExpression invocation, TestNodeExpression expr) {
        appendParentScope(expr.node.asObjectCreationExpr(), invocation.node)
    }

    private static ClassOrInterfaceType findParentTypeScope(ClassOrInterfaceType type) {
        if (type.scope.present) {
            return findParentTypeScope(type.scope.get())
        } else {
            return type
        }
    }

    private static ObjectCreationExpr findParentObjectCreationScope(ObjectCreationExpr n) {
        if (n.scope.present) {
            return findParentObjectCreationScope(n.scope.get().asObjectCreationExpr())
        } else {
            return n
        }
    }

    private static appendParentScope(NodeWithOptionalScope expr, Expression scope) {
        if (expr.scope.present) {
            appendParentScope(expr.scope.get().asObjectCreationExpr(), scope)
        } else {
            expr.setScope(scope)
        }
    }

    private static Optional<ConstructorDeclaration> findSimplestConstructor(TypeDeclaration td) {
        Collection<ConstructorDeclaration> constructorDeclarations = getConstructors(td)
        findNonPrivateLeastArgumentsConstructor(constructorDeclarations)
    }

    private static boolean defaultConstructor(TypeDeclaration td) {
        getConstructors(td).empty
    }

    private static Collection<ConstructorDeclaration> getConstructors(TypeDeclaration td) {
        if (td.annotationDeclaration) {
            return Collections.emptyList()
        } else {
            List<ConstructorDeclaration> cds = (td as NodeWithConstructors).getConstructors()
            return cds
        }
    }

    private static Optional<ConstructorDeclaration> findNonPrivateLeastArgumentsConstructor(Collection<ConstructorDeclaration> constructors) {
        StreamEx.of(constructors)
                .filter { !it.private }
                .sortedBy { it.parameters.size() }
                .findFirst()
    }
}

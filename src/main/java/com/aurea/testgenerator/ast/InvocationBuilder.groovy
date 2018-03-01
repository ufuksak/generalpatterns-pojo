package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.value.Resolution
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithConstructors
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalScope
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.resolution.types.ResolvedType
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class InvocationBuilder {

    ValueFactory factory
    Map<SimpleName, DependableNode<Expression>> expressionsForParameters

    InvocationBuilder(ValueFactory factory) {
        this.factory = factory
    }

    InvocationBuilder usingForParameters(Map<SimpleName, DependableNode<Expression>> expressionsForParameters) {
        this.expressionsForParameters = expressionsForParameters
        this
    }

    Optional<DependableNode<ObjectCreationExpr>> build(ConstructorDeclaration constructor) {
        if (!Callability.isCallableFromTests(constructor)) {
            return Optional.empty()
        }
        List<TypeDeclaration> parents = ASTNodeUtils.parents(constructor, TypeDeclaration).toList()
        try {
            if (parents.size() == 1) {
                return Optional.of(buildConstructorInvocation(constructor))
            } else {
                if (parents.empty) {
                    log.error "No type declaration for $constructor"
                }
                boolean isParentStatic = parents.first().static
                DependableNode<ObjectCreationExpr> expr = buildConstructorInvocation(constructor)
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
                            DependableNode<Expression> defaultInvocation = DependableNode.from(
                                    new ObjectCreationExpr(null,
                                            JavaParser.parseClassOrInterfaceType(parent.nameAsString),
                                            NodeList.nodeList()))
                            prependWithInvocation(defaultInvocation, expr)
                        } else {
                            Optional<ConstructorDeclaration> simplestConstructor = findSimplestConstructor(parent)
                            if (!simplestConstructor.present) {
                                return Optional.empty()
                            }
                            DependableNode<Expression> invocation = buildConstructorInvocation(simplestConstructor.get())
                            prependWithInvocation(invocation, expr)
                        }
                    }
                    isParentStatic = parent.static
                }
                return Optional.of(expr)
            }
        } catch (IllegalArgumentException iae) {
            log.debug "Failed to build constructor invocation for $constructor"
            log.trace "Failed to build constructor invocation for $constructor", iae
            return Optional.empty()
        }
    }

    Optional<DependableNode<MethodCallExpr>> buildMethodInvocation(CallableDeclaration callable) {
        try {
            def argumentsList = createArgumentsList(callable)
            def dependency = TestNodeMerger.merge(argumentsList.dependency)
            def scope = null
            if (callable.static) {
                // TODO: Probably mostly the same scope magic required as with constructor invocation above
                // Issue: https://github.com/trilogy-group/BigCodeTestGenerator/issues/31
                def methodClass = callable.getParentNode().get() as ClassOrInterfaceDeclaration
                scope = new NameExpr(methodClass.nameAsString)
            }
            def node = new MethodCallExpr(scope, callable.nameAsString, NodeList.nodeList(argumentsList.node))

            return Optional.of(DependableNode.from(node, dependency))
        } catch (IllegalArgumentException iae) {
            log.debug "Failed to build method invocation $callable"
            log.trace "Failed to build method invocation $callable", iae
            return Optional.empty()
        }
    }

    private List<DependableNode<Expression>> createArgumentsList(CallableDeclaration cd) {
        if (expressionsForParameters) {
            return getFromGivenParameters(cd)
        }

        cd.parameters.collect { parameter ->
            Optional<ResolvedType> resolvedType = Resolution.tryResolve(parameter.type)
            resolvedType.flatMap { factory.getExpression(it)}.orElseThrow {
                new IllegalArgumentException("Failed to create expression for ${parameter} of ${cd}")
            }
        }
    }

    private DependableNode<ObjectCreationExpr> buildConstructorInvocation(CallableDeclaration cd) {
        List<DependableNode<Expression>> argumentsList = createArgumentsList(cd)
        def dependency = TestNodeMerger.merge(argumentsList.dependency)
        def type = JavaParser.parseClassOrInterfaceType(cd.nameAsString)
        ObjectCreationExpr node = new ObjectCreationExpr(null,
                type,
                NodeList.nodeList(argumentsList.node))

        DependableNode.from(node, dependency)
    }

    private List<DependableNode<Expression>> getFromGivenParameters(CallableDeclaration cd) {
        StreamEx.of(cd.parameters).map { parameter ->
            Optional.ofNullable(expressionsForParameters.get(parameter.name)).orElseThrow {
                new IllegalArgumentException("Failed to find a parameter value for $parameter in $cd. " +
                        "Only $expressionsForParameters were provided!")
            }
        }.toList()
    }

    private static void prependWithScope(DependableNode<ObjectCreationExpr> expr, String scopeName) {
        ObjectCreationExpr parentScope = findParentObjectCreationScope(expr.node.asObjectCreationExpr())
        ClassOrInterfaceType parentTypeScope = findParentTypeScope(parentScope.type)
        parentTypeScope.setScope(JavaParser.parseClassOrInterfaceType(scopeName))
    }

    private static void prependWithInvocation(DependableNode<Expression> invocation, DependableNode<ObjectCreationExpr> expr) {
        appendParentScope(expr.node, invocation.node)
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

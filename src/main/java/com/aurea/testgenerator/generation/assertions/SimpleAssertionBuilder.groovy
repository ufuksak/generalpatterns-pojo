package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.ast.TestDependency
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.value.Resolution
import com.aurea.testgenerator.value.Types
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import groovy.util.logging.Log4j2

import static com.github.javaparser.JavaParser.parseExpression
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.BOOLEAN
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.BYTE
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.CHAR
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.DOUBLE
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.FLOAT
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.INT
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.LONG
import static com.github.javaparser.resolution.types.ResolvedPrimitiveType.SHORT

@Log4j2
class SimpleAssertionBuilder implements AssertionProducer, AssertionStatementProducer {

    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOUBLE = '0.001D'

    protected List<DependableNode<MethodCallExpr>> assertions = []

    SimpleAssertionBuilder with(Type type, Expression actual, Expression expected) {
        assert type.findCompilationUnit().present
        Optional<ResolvedType> resolvedType = Resolution.tryResolve(type)
        resolvedType.ifPresent {
            with(it, actual, expected)
        }
        this
    }

    SimpleAssertionBuilder with(ResolvedType type, Expression actual, Expression expected) {
        if (type.primitive) {
            addPrimitiveAssertion(type.asPrimitive(), actual, expected)
        } else if (type.referenceType) {
            ResolvedReferenceType resolved = type.asReferenceType()
            if (Types.isBoxedPrimitive(resolved)) {
                addPrimitiveAssertion(Types.unbox(resolved), actual, expected)
            } else if (Types.isComparable(resolved)) {
                addComparableAssertion(actual, expected)
            } else {
                addEqualAssertion(actual, expected)
            }
        } else if (type.array) {
            addEqualAssertion(actual, expected)
        }
        this
    }

    SimpleAssertionBuilder assertListContainsSameElements(ResolvedType type, Expression actual, Expression expected) {
        assert type.referenceType
        assert Types.isList(type.asReferenceType())
        addContainsAllAssertion(actual, expected)
        this
    }

    @Override
    List<DependableNode<Statement>> getStatements() {
        assertions.collect {
            DependableNode.from(new ExpressionStmt(it.node), it.dependency)
        }
    }

    private void addComparableAssertion(Expression actual, Expression expected) {
        assertions << createAssertThatMethodCallExpression("assertThat($actual).isEqualByComparingTo($expected)")
    }

    private void addPrimitiveAssertion(ResolvedPrimitiveType type, Expression actual, Expression expected) {
        switch (type) {
            case BOOLEAN:
                addBooleanPrimitiveAssertion(actual, expected)
                break
            case [CHAR, BYTE, LONG, SHORT, INT]:
                addEqualAssertion(actual, expected)
                break
            case FLOAT:
                DependableNode<MethodCallExpr> methodCallExpr = createAssertThatMethodCallExpression(
                        "assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_FLOAT))")
                methodCallExpr.dependency.imports << Imports.ASSERTJ_OFFSET
                assertions << methodCallExpr
                break
            case DOUBLE:
                DependableNode<MethodCallExpr> methodCallExpr = createAssertThatMethodCallExpression(
                        "assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_DOUBLE))")
                methodCallExpr.dependency.imports << Imports.ASSERTJ_OFFSET
                assertions << methodCallExpr
                break
        }
    }

    private void addEqualAssertion(Expression actual, Expression expected) {
        assertions << createAssertThatMethodCallExpression("assertThat($actual).isEqualTo($expected)")
    }

    private void addContainsAllAssertion(Expression actual, Expression expected) {
        assertions << createAssertThatMethodCallExpression("assertThat($actual).containsAll($expected)")
    }

    private void addBooleanPrimitiveAssertion(Expression actual, Expression expected) {
        if (expected.isBooleanLiteralExpr()) {
            boolean value = expected.asBooleanLiteralExpr().value
            assertions << createAssertThatMethodCallExpression("assertThat($actual).is${value ? 'True' : 'False'}()")
        } else {
            addEqualAssertion(actual, expected)
        }
    }

    private static DependableNode<MethodCallExpr> createAssertThatMethodCallExpression(String expr) {
        DependableNode<MethodCallExpr> testNodeMethodCallExpr = new DependableNode<>()
        testNodeMethodCallExpr.dependency.imports << Imports.ASSERTJ_ASSERTTHAT
        testNodeMethodCallExpr.node = parseExpression(expr).asMethodCallExpr()
        testNodeMethodCallExpr
    }

    @Override
    List<DependableNode<MethodCallExpr>> getAssertions() {
        this.@assertions
    }
}

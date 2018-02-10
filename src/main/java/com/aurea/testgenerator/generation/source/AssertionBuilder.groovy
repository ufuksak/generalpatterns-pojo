package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.TestDependency
import com.aurea.testgenerator.generation.TestNodeMethodCallExpr
import com.aurea.testgenerator.generation.TestNodeStatement
import com.aurea.testgenerator.value.Types
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import groovy.util.logging.Log4j2

import static com.github.javaparser.JavaParser.parseExpression

@Log4j2
class AssertionBuilder {

    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOUBLE = '0.001D'

    boolean softly
    List<TestNodeMethodCallExpr> assertions = []

    AssertionBuilder softly(boolean softly) {
        this.softly = softly
        this
    }

    AssertionBuilder with(Type type, Expression actual, Expression expected) {
        assert type.findCompilationUnit().present
        try {
            ResolvedType resolvedType = type.resolve()
            with(resolvedType, actual, expected)
        } catch (UnsolvedSymbolException use) {
            log.error "Failed to solve type $type. Skipping assertion $actual -> $expected", use
        }
        this
    }

    AssertionBuilder with(ResolvedType type, Expression actual, Expression expected) {
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

    AssertionBuilder assertListContainsSameElements(ResolvedType type, Expression actual, Expression expected) {
        assert type.referenceType
        assert Types.isList(type.asReferenceType())
        addContainsAllAssertion(actual, expected)
        this
    }

    List<TestNodeStatement> build() {
        if (assertions.empty) {
            return Collections.emptyList()
        }

        softly ? asSoftStatements() : asStatements()
    }

    private List<TestNodeStatement> asStatements() {
        assertions.collect {
            new TestNodeStatement(
                    dependency: it.dependency,
                    node: new ExpressionStmt(it.node)
            )
        }
    }

    private List<TestNodeStatement> asSoftStatements() {
        List<TestNodeStatement> softAssertions = new ArrayList<>(assertions.size() + 2)
        softAssertions << new TestNodeStatement(
                dependency: new TestDependency(imports: [Imports.SOFT_ASSERTIONS]),
                //TODO: 'sa' should be taken from name registry, not hardcoded
                node: JavaParser.parseStatement("SoftAssertions sa = new SoftAssertions();"))

        assertions.each { assertion ->
            Expression sa = new NameExpr("sa")
            MethodCallExpr assertThatMethodCall = assertion.node.scope.get().asMethodCallExpr()
            assertThatMethodCall.setScope(sa)
            softAssertions << new TestNodeStatement(
                    dependency: assertion.dependency,
                    node: new ExpressionStmt(assertion.node)
            )
        }
        softAssertions << new TestNodeStatement(node: JavaParser.parseStatement("sa.assertAll();"))
    }

    private void addComparableAssertion(Expression actual, Expression expected) {
        assertions << createAssertThatMethodCallExpression("assertThat($actual).isEqualByComparingTo($expected)")
    }

    private void addPrimitiveAssertion(ResolvedPrimitiveType type, Expression actual, Expression expected) {
        if (type == ResolvedPrimitiveType.BOOLEAN) {
            addBooleanPrimitiveAssertion(actual, expected)
        } else if (
        type == ResolvedPrimitiveType.CHAR ||
                type == ResolvedPrimitiveType.BYTE ||
                type == ResolvedPrimitiveType.LONG ||
                type == ResolvedPrimitiveType.SHORT ||
                type == ResolvedPrimitiveType.INT) {
            addEqualAssertion(actual, expected)
        } else if (type == ResolvedPrimitiveType.FLOAT) {
            TestNodeMethodCallExpr methodCallExpr = createAssertThatMethodCallExpression(
                    "assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_FLOAT))")
            methodCallExpr.dependency.imports << Imports.ASSERTJ_OFFSET
            assertions << methodCallExpr
        } else if (type == ResolvedPrimitiveType.DOUBLE) {
            TestNodeMethodCallExpr methodCallExpr = createAssertThatMethodCallExpression(
                    "assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_DOUBLE))")
            methodCallExpr.dependency.imports << Imports.ASSERTJ_OFFSET
            assertions << methodCallExpr
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

    private TestNodeMethodCallExpr createAssertThatMethodCallExpression(String expr) {
        TestNodeMethodCallExpr testNodeMethodCallExpr = new TestNodeMethodCallExpr()
        if (!softly) {
            testNodeMethodCallExpr.dependency.imports << Imports.ASSERTJ_ASSERTTHAT
        }
        testNodeMethodCallExpr.node = parseExpression(expr).asMethodCallExpr()
        testNodeMethodCallExpr
    }
}

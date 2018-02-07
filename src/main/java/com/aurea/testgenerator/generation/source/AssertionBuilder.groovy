package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.value.Types
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import groovy.util.logging.Log4j2

import static com.github.javaparser.JavaParser.parseExpression

@Log4j2
class AssertionBuilder {

    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOUBLE = '0.001D'

    TestUnit testUnit
    boolean softly
    List<MethodCallExpr> assertions = []

    static AssertionBuilder buildFor(TestUnit testUnit) {
        new AssertionBuilder(testUnit)
    }

    private AssertionBuilder(TestUnit testUnit) {
        this.testUnit = testUnit
    }

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

    List<Statement> build() {
        if (assertions.empty) {
            return Collections.emptyList()
        }

        List<Statement> statements = []
        testUnit.addImport(Imports.ASSERTJ_ASSERTTHAT)
        if (softly) {
            testUnit.addImport(Imports.SOFT_ASSERTIONS)
            statements << JavaParser.parseStatement("SoftAssertions sa = new SoftAssertions();")
        }
        assertions.each {
            if (softly) {
                Expression sa = new NameExpr("sa")
                MethodCallExpr assertThatMethodCall = it.scope.get().asMethodCallExpr()
                assertThatMethodCall.setScope(sa)
            }
            ExpressionStmt stmt = new ExpressionStmt(it)
            statements << stmt
        }
        if (softly) {
            statements << JavaParser.parseStatement("sa.assertAll();")
        }

        statements
    }

    private void addComparableAssertion(Expression actual, Expression expected) {
        assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).isEqualByComparingTo($expected)").asMethodCallExpr()
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
            testUnit.addImport Imports.ASSERTJ_OFFSET
            assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_FLOAT))")
                    .asMethodCallExpr()
        } else if (type == ResolvedPrimitiveType.DOUBLE) {
            testUnit.addImport Imports.ASSERTJ_OFFSET
            assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_DOUBLE))")
                    .asMethodCallExpr()
        }
    }

    private void addEqualAssertion(Expression actual, Expression expected) {
        assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).isEqualTo($expected)").asMethodCallExpr()
    }

    private void addContainsAllAssertion(Expression actual, Expression expected) {
        assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).containsAll($expected)").asMethodCallExpr()
    }

    private void addBooleanPrimitiveAssertion(Expression actual, Expression expected) {
        if (expected.isBooleanLiteralExpr()) {
            boolean value = expected.asBooleanLiteralExpr().value
            assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).is${value ? 'True' : 'False'}()").asMethodCallExpr()
        } else {
            addEqualAssertion(actual, expected)
        }
    }
}

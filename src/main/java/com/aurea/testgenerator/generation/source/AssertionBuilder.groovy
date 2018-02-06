package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.TestUnit
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
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import groovy.util.logging.Log4j2

import static com.github.javaparser.JavaParser.parseExpression

@Log4j2
class AssertionBuilder {

    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOUBLE = '0.001D'

    static final Set<String> KNOWN_COMPARABLE_TYPES = [
            'BigDecimal',
            'BigInteger',
            'java.math.BigDecimal',
            'java.math.BigInteger']

    static final Set<String> KNOWN_COLLECTION_TYPES = [
            'Iterable',
            'java.util.Iterable',
            'Collection',
            'java.util.Collection',
            'List',
            'java.util.List',
            'Set',
            'java.util.Set',
            'SortedSet',
            'java.util.SortedSet',
    ]

    static final Set<String> KNOWN_MAP_TYPES = [
            'Map',
            'java.util.Map',
            'HashMap',
            'java.util.HashMap',
    ]

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
            if (isBoxedPrimitive(resolved)) {
                addPrimitiveAssertion(unbox(resolved), actual, expected)
            } else if (isComparable(resolved)) {
                addComparableAssertion(actual, expected)
            } else if (isCollection(resolved)) {
                throw new UnsupportedOperationException("Collections are not supported but $resolved.qualifiedName is. " +
                        "Please use specialized methods for building assertions for collections")
            } else if (isMap(resolved)) {
                throw new UnsupportedOperationException("Maps are not supported but $resolved.qualifiedName is. " +
                        "Please use specialized methods for building assertions for maps")
            } else {
                addEqualAssertion(actual, expected)
            }
        } else if (type.array) {
            throw new UnsupportedOperationException("Arrays are not supported. Please use specialized methods " +
                    "for building assertions for arrays")
        }
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

    private void addPrimitiveAssertion(PrimitiveType type, Expression actual, Expression expected) {
        switch (type.type) {
            case PrimitiveType.Primitive.BOOLEAN:
                addBooleanPrimitiveAssertion(actual, expected)
                break
            case PrimitiveType.Primitive.CHAR:
            case PrimitiveType.Primitive.BYTE:
            case PrimitiveType.Primitive.LONG:
            case PrimitiveType.Primitive.SHORT:
            case PrimitiveType.Primitive.INT:
                addEqualAssertion(actual, expected)
                break
            case PrimitiveType.Primitive.FLOAT:
                testUnit.addImport Imports.ASSERTJ_OFFSET
                assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_FLOAT))")
                        .asMethodCallExpr()
                break
            case PrimitiveType.Primitive.DOUBLE:
                testUnit.addImport Imports.ASSERTJ_OFFSET
                assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_DOUBLE))")
                        .asMethodCallExpr()
                break
        }
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

    private void addBooleanPrimitiveAssertion(Expression actual, Expression expected) {
        if (expected.isBooleanLiteralExpr()) {
            boolean value = expected.asBooleanLiteralExpr().value
            assertions << parseExpression("${softly ? 'sa.' : ''}assertThat($actual).is${value ? 'True' : 'False'}()").asMethodCallExpr()
        } else {
            addEqualAssertion(actual, expected)
        }
    }

    private static boolean isComparable(ClassOrInterfaceType type) {
        KNOWN_COMPARABLE_TYPES.contains(type.nameAsString)
    }

    private static boolean isCollection(ClassOrInterfaceType type) {
        KNOWN_COLLECTION_TYPES.contains(type.nameAsString)
    }

    private static boolean isMap(ClassOrInterfaceType type) {
        KNOWN_MAP_TYPES.contains(type.nameAsString)
    }

    private static boolean isComparable(ResolvedReferenceType type) {
        KNOWN_COMPARABLE_TYPES.contains(type.qualifiedName)
    }

    private static boolean isCollection(ResolvedReferenceType type) {
        KNOWN_COLLECTION_TYPES.contains(type.qualifiedName)
    }

    private static boolean isMap(ResolvedReferenceType type) {
        KNOWN_MAP_TYPES.contains(type.qualifiedName)
    }

    private static boolean isBoxedPrimitive(ResolvedReferenceType type) {
        for (ResolvedPrimitiveType primitive: ResolvedPrimitiveType.ALL) {
            if (primitive.boxTypeQName == type.qualifiedName) {
                return true
            }
        }
        return false
    }

    private static ResolvedPrimitiveType unbox(ResolvedReferenceType type) {
        for (ResolvedPrimitiveType primitive: ResolvedPrimitiveType.ALL) {
            if (primitive.boxTypeQName == type.qualifiedName) {
                return primitive
            }
        }
        return null
    }
}

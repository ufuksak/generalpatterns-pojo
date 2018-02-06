package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.TestUnitSpec
import com.aurea.testgenerator.generation.TestUnit
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import spock.lang.Unroll

import static com.github.javaparser.JavaParser.parseExpression
import static com.github.javaparser.JavaParser.parseType
import static org.assertj.core.api.Assertions.assertThat

class AssertionBuilderSpec extends TestUnitSpec{

    TestUnit unit = newTestUnit()
    AssertionBuilder builder = AssertionBuilder.buildFor(unit)

    @Unroll
    def "correctly builds assertions for char/byte/long/short/int primitives"() {
        when:
        List<Statement> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().toString() == "assertThat(${actual}).isEqualTo(${expected});"
        unit.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual | expected | type
        "5"    | "3 + 2"  | PrimitiveType.intType()
        "5"    | "3 + 2"  | parseType("Integer")
        "5"    | "3 + 2"  | parseType("java.lang.Integer")
        "c"    | "c"      | PrimitiveType.charType()
        "c"    | "c"      | parseType("Character")
        "c"    | "c"      | parseType("java.lang.Character")
        "5"    | "3 + 2"  | PrimitiveType.byteType()
        "5"    | "3 + 2"  | parseType("Byte")
        "5"    | "3 + 2"  | parseType("java.lang.Byte")
        "5"    | "3 + 2"  | PrimitiveType.longType()
        "5"    | "3 + 2"  | parseType("Long")
        "5"    | "3 + 2"  | parseType("java.lang.Long")
        "5"    | "3 + 2"  | PrimitiveType.shortType()
        "5"    | "3 + 2"  | parseType("Short")
        "5"    | "3 + 2"  | parseType("java.lang.Short")
    }

    @Unroll
    def "correctly build for booleans"() {
        when:
        List<Statement> statements = builder.with(wrapWithCompilationUnit(PrimitiveType.booleanType()),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().toString() == "assertThat(${actual}).${expectedAssertion};"
        unit.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual  | expected       | expectedAssertion
        "true"  | "false"        | "isFalse()"
        "false" | "true"         | "isTrue()"
        "true"  | "Boolean.TRUE" | "isEqualTo(Boolean.TRUE)"
    }

    @Unroll
    def "correctly build for boxed booleans"() {
        when:
        List<Statement> statements = builder.with(wrapWithCompilationUnit(parseType('Boolean')),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().toString() == "assertThat(${actual}).${expectedAssertion};"
        unit.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual  | expected       | expectedAssertion
        "true"  | "false"        | "isFalse()"
        "false" | "true"         | "isTrue()"
        "true"  | "Boolean.TRUE" | "isEqualTo(Boolean.TRUE)"
    }

    @Unroll
    def "correctly build for floating numbers"() {
        when:
        List<Statement> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().toString() == "assertThat(${actual}).isCloseTo($expected, Offset.offset($offset));"
        unit.imports.contains Imports.ASSERTJ_OFFSET

        where:
        actual   | expected | offset                                        | type
        "0.1f"   | "0.2f"   | AssertionBuilder.FLOATING_POINT_OFFSET_FLOAT  | PrimitiveType.floatType()
        "0.001f" | "2.3f"   | AssertionBuilder.FLOATING_POINT_OFFSET_FLOAT  | parseType('Float')
        "0.001f" | "2.3f"   | AssertionBuilder.FLOATING_POINT_OFFSET_FLOAT  | parseType('java.lang.Float')
        "0.001"  | "2.3"    | AssertionBuilder.FLOATING_POINT_OFFSET_DOUBLE | PrimitiveType.doubleType()
        "0.001"  | "2.3"    | AssertionBuilder.FLOATING_POINT_OFFSET_DOUBLE | parseType('Double')
        "0.001"  | "2.3"    | AssertionBuilder.FLOATING_POINT_OFFSET_DOUBLE | parseType('java.lang.Double')
    }

    def "soft assertions are properly grouped"() {
        when:
        List<Statement> statements = builder
                .with(wrapWithCompilationUnit(PrimitiveType.intType()), parseExpression("3"), parseExpression("2 + 1"))
                .with(wrapWithCompilationUnit(PrimitiveType.floatType()), parseExpression("3.4"), parseExpression("3.4"))
                .softly(true)
                .build()

        then:
        statements.size() == 1 + 2 + 1
        unit.imports.containsAll([Imports.SOFT_ASSERTIONS, Imports.ASSERTJ_OFFSET, Imports.ASSERTJ_ASSERTTHAT])
        assertThat(statements.join(System.lineSeparator()))
                .isEqualToIgnoringWhitespace("""
    SoftAssertions sa = new SoftAssertions();
    sa.assertThat(3).isEqualTo(2 + 1);
    sa.assertThat(3.4).isCloseTo(3.4, Offset.offset(${AssertionBuilder.FLOATING_POINT_OFFSET_FLOAT}));
    sa.assertAll();
""")
    }

    def "no assertions added - no statements generated"() {
        expect:
        builder.softly(true).build().empty
    }

    def "asserting strings work"() {
        when:
        List<Statement> statements = builder
                .with(wrapWithCompilationUnit(parseType("String")), parseExpression("\"ABC\""), parseExpression("\"AAB\""))
                .build()

        then:
        statements.size() == 1
        statements.first().toString() == 'assertThat("ABC").isEqualTo("AAB");'
        unit.imports.contains Imports.ASSERTJ_ASSERTTHAT
    }

    @Unroll
    def "known comparable types are being asserted by isEqualByComparingTo"() {
        when:
        List<Statement> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(value),
                parseExpression(value)
        ).build()

        then:
        statements.size() == 1
        statements.first().toString() == "assertThat($value).isEqualByComparingTo($value);"
        unit.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        value            | type
        "BigDecimal.ONE" | parseType("java.math.BigDecimal")
        "BigInteger.ONE" | parseType("java.math.BigInteger")
    }

    @Unroll
    def "maps, collections, arrays are not supported by simple with(type, actual, expected)"() {
        when:
        builder.with(wrapWithCompilationUnit(type),
                parseExpression(value),
                parseExpression(value)
        ).build()

        then:
        thrown UnsupportedOperationException

        where:
        value                     | type
        "Collections.emptyMap()"  | parseType('java.util.Map')
        "Collections.emptyList()" | parseType('java.util.List')
        "Collections.emptyList()" | parseType('java.util.Collection')
        "new int[] { 1 } "        | parseType('int[]')
    }
}

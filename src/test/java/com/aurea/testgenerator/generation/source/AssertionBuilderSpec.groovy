package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.TestUnitSpec
import com.aurea.testgenerator.generation.TestNodeStatement
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.type.PrimitiveType
import one.util.streamex.StreamEx
import spock.lang.Unroll

import static com.github.javaparser.JavaParser.parseExpression
import static com.github.javaparser.JavaParser.parseType
import static org.assertj.core.api.Assertions.assertThat

class AssertionBuilderSpec extends TestUnitSpec {

    AssertionBuilder builder = new AssertionBuilder()

    @Unroll
    def "correctly builds assertions for char/byte/long/short/int primitives"() {
        when:
        List<TestNodeStatement> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().stmt.toString() == "assertThat(${actual}).isEqualTo(${expected});"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

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
        List<TestNodeStatement> statements = builder.with(wrapWithCompilationUnit(PrimitiveType.booleanType()),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().stmt.toString() == "assertThat(${actual}).${expectedAssertion};"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual  | expected       | expectedAssertion
        "true"  | "false"        | "isFalse()"
        "false" | "true"         | "isTrue()"
        "true"  | "Boolean.TRUE" | "isEqualTo(Boolean.TRUE)"
    }

    @Unroll
    def "correctly build for boxed booleans"() {
        when:
        List<TestNodeStatement> statements = builder.with(wrapWithCompilationUnit(parseType('Boolean')),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().stmt.toString() == "assertThat(${actual}).${expectedAssertion};"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual  | expected       | expectedAssertion
        "true"  | "false"        | "isFalse()"
        "false" | "true"         | "isTrue()"
        "true"  | "Boolean.TRUE" | "isEqualTo(Boolean.TRUE)"
    }

    @Unroll
    def "correctly build for floating numbers"() {
        when:
        List<TestNodeStatement> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(actual),
                parseExpression(expected)
        ).build()

        then:
        statements.size() == 1
        statements.first().stmt.toString() == "assertThat(${actual}).isCloseTo($expected, Offset.offset($offset));"
        statements.first().dependency.imports.contains Imports.ASSERTJ_OFFSET

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
        List<TestNodeStatement> statements = builder
                .with(wrapWithCompilationUnit(PrimitiveType.intType()), parseExpression("3"), parseExpression("2 + 1"))
                .with(wrapWithCompilationUnit(PrimitiveType.floatType()), parseExpression("3.4"), parseExpression("3.4"))
                .softly(true)
                .build()

        then:
        statements.size() == 1 + 2 + 1
        Set<ImportDeclaration> imports = StreamEx.of(statements).flatMap { it.dependency.imports.stream() }.toSet()
        imports.containsAll([Imports.SOFT_ASSERTIONS, Imports.ASSERTJ_OFFSET, Imports.ASSERTJ_ASSERTTHAT])
        assertThat(statements.collect { it.stmt }.join(System.lineSeparator()))
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
        List<TestNodeStatement> statements = builder
                .with(wrapWithCompilationUnit(parseType("String")), parseExpression("\"ABC\""), parseExpression("\"AAB\""))
                .build()

        then:
        statements.size() == 1
        statements.first().stmt.toString() == 'assertThat("ABC").isEqualTo("AAB");'
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT
    }

    @Unroll
    def "known comparable types are being asserted by isEqualByComparingTo"() {
        when:
        List<TestNodeStatement> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(value),
                parseExpression(value)
        ).build()

        then:
        statements.size() == 1
        statements.first().stmt.toString() == "assertThat($value).isEqualByComparingTo($value);"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        value            | type
        "BigDecimal.ONE" | parseType("java.math.BigDecimal")
        "BigInteger.ONE" | parseType("java.math.BigInteger")
    }
}

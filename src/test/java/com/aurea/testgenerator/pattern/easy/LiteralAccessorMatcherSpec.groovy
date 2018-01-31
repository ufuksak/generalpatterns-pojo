package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.coverage.CoverageService
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade

class LiteralAccessorMatcherSpec extends AccesorMatcherBaseSpec<LiteralAccessorMatch> {

    def "should find simple literals"() {
        when:
        LiteralAccessorMatch match = onFooClassCode(
                """
class Foo {
 public Object getFoo() {
  return $literal;
 }
}
""")

        then:
        match.expression == expectation

        where:
        literal | expectation
        3       | "3"
        '"ABC"' | '"ABC"'
        3.0     | "3.0"
        "3L"    | "3L"
        "'c'"   | "'c'"
        false   | "false"
        true    | "true"
    }

    def "should find null literal"() {
        when:
        LiteralAccessorMatch match = onFooClassCode(
                """
class Foo {
 public Object getFoo() {
  return null;
 }
}
""")
        then:
        match.expression == LiteralResolver.NULL
    }

    def "should return complex arithmetic on primitives as an expression"() {
        when:
        LiteralAccessorMatch match = onFooClassCode(
                """
class Foo {
 public int getFoo() {
  return 1 + 2 * 3 / 4 % 123 >> 3;
 }
}
""")
        then:
        match.expression == "1 + 2 * 3 / 4 % 123 >> 3"
    }

    def "should be able to find concatenated string literals resulting into single expression"() {
        when:
        LiteralAccessorMatch match = onFooClassCode(
                """
class Foo {
 public String getFoo() {
  return "A" + "B" + "C";
 }
}
""")
        then:
        match.expression == '"ABC"'
    }

    def "should be able to find concatenated string and primitive literals resulting into single expression"() {
        when:
        LiteralAccessorMatch match = onFooClassCode(
                """
class Foo {
 public String getFoo() {
  return "A" + 123 + "C";
 }
}
""")
        then:
        match.expression == '"A123C"'
    }

    def "creates correct "() {}


    @Override
    AccessorMatcher newMatcher(CoverageService coverageService, JavaParserFacade facade) {
        new LiteralAccessorMatcher(coverageService, facade)
    }
}

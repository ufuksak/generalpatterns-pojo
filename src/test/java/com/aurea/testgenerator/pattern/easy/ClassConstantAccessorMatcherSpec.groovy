package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.coverage.CoverageService
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade

class ClassConstantAccessorMatcherSpec extends AccesorMatcherBaseSpec<ClassConstantAccessorMatch> {

    def "should find simple object constants"() {
        when:
        ClassConstantAccessorMatch match = onFooClassCode(
                """
class Foo {
    public final int foo = $literal;

    public Object getFoo() {
        return foo;
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


    def "should find class level constant accessor binary expr"() {
        when:
        ClassConstantAccessorMatch match = onFooClassCode(
                """
class Foo {
 public static final int foo = 123 + 456;
 public int getFoo() {
  return foo;
 }
}
""")
        then:
        match.expression == '123 + 456'
    }

    def "should find class level constant accessor simple expr as initializer"() {
        when:
        ClassConstantAccessorMatch match = onFooClassCode(
                """
class Foo {
 public static final int foo = 123;
 public int getFoo() {
  return foo;
 }
}
""")
        then:
        match.expression == '123'
    }

    def "should find class level constant accessor in inner classes"() {
        when:
        ClassConstantAccessorMatch match = onFooClassCode(
                """
class Foo {
 public static class Bar {
    public static final int foo = 123;
    
    public int getFoo() {
        return foo;
    }
 }
}
""")
        then:
        match.expression == '123'
    }

    def "should deduce field initialization in static block"() {
        when:
        ClassConstantAccessorMatch match = onFooClassCode(
                """
class Foo {
    public static final int foo;
    
    static {
        foo = 123;
    }
    
    public int getFoo() {
        return foo;
    }
}
""")
        then:
        match.expression == '123'
    }

    @Override
    AccessorMatcher newMatcher(CoverageService coverageService, JavaParserFacade facade) {
        new ClassConstantAccessorMatcher(coverageService, facade)
    }
}

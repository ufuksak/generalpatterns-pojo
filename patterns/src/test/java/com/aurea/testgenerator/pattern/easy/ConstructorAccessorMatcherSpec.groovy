package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.coverage.CoverageService
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade


class ConstructorAccessorMatcherSpec extends AccesorMatcherBaseSpec<ConstructorAccessorMatch> {

    def "should find object constant initialized in constructor"() {
        when:
        ConstructorAccessorMatch match = onFooClassCode(
                """
class Foo {
    public final int foo;
    public Foo() {
        foo = 123;
    }                  

    public Object getFoo() {
        return foo;
    }
}
""")
        then:
        match.constructorFieldInitializer instanceof ConstructorLiteralFieldInitializer
        (match.constructorFieldInitializer as ConstructorLiteralFieldInitializer).expression == '123'
    }

    def "should find object constant initialized in constructor via constructor argument"() {
        when:
        ConstructorAccessorMatch match = onFooClassCode(
                """
class Foo {
    public final int foo;
    public Foo(int constructorArg) {
        this.foo = constructorArg;
    }                  

    public Object getFoo() {
        return foo;
    }
}
""")
        then:
        match.constructorFieldInitializer instanceof ConstructorParameterFieldInitializer
        (match.constructorFieldInitializer as ConstructorParameterFieldInitializer).parameter.nameAsString == 'constructorArg'
    }

    def "should find when assigned not with 'this.' in constructor via constructor argument"() {
        when:
        ConstructorAccessorMatch match = onFooClassCode(
                """
class Foo {
    public final int foo;
    public Foo(int constructorArg) {
        foo = constructorArg;
    }                  

    public Object getFoo() {
        return foo;
    }
}
""")
        then:
        match.constructorFieldInitializer instanceof ConstructorParameterFieldInitializer
        (match.constructorFieldInitializer as ConstructorParameterFieldInitializer).parameter.nameAsString == 'constructorArg'
    }

    @Override
    AccessorMatcher newMatcher(CoverageService coverageService, JavaParserFacade facade) {
        new ConstructorAccessorMatcher(coverageService, facade)
    }
}

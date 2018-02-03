package com.aurea.testgenerator.ast

import com.aurea.testgenerator.extensions.Extensions
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.value.TypeAsLiteralValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

class InvocationBuilderSpec extends Specification {

    def setupSpec() {
        Extensions.enable()
    }

    def "should be able to invoke simple constructor"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
                Foo() {}
            }
        """, """
            new Foo()
        """
    }

    void onConstructorCodeExpect(String code, String expected) {
        ConstructorDeclaration cd = JavaParser.parse(code).findFirst(ConstructorDeclaration).get()
        Optional<TestNodeExpression> testNodeExpression = new InvocationBuilder(new TypeAsLiteralValueFactory()).build(cd)
        assertThat(testNodeExpression).isPresent()
        assertThat(testNodeExpression.get().expr.toString()).isEqualToNormalizingWhitespace(expected)
    }
}

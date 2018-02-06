package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.TestUnitSpec
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration

import static org.assertj.core.api.Assertions.assertThat

class StaticFieldAccessorBuilderSpec extends TestUnitSpec {

    def "should be able to build expression for a static public variable"() {
        expect:
        expectOnCode("""
            class Foo {
                static int i = 123; 
            }
        """, "Foo.i")
    }

    def "should be able to build expression for a static public variable of inner class"() {
        expect:
        expectOnCode("""
            class Foo {                                                                   
                class Bar {
                    static int i = 123; 
                }
            }
        """, "Foo.Bar.i")
    }

    def "should not be able to instantiate private static"() {
        expect:
        expectNoExpressionOnCode """
            class Foo {
                private static final int i = 123;
            }
        """
    }

    void expectOnCode(String code, String expected) {
        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)
        FieldDeclaration fd = cu.findAll(FieldDeclaration).first()
        ResolvedFieldDeclaration rfd = fd.resolve()
        Optional<Expression> expression = new StaticFieldAccessorBuilder(rfd).build()

        assertThat(expression)
                .describedAs("Expected to have resolved access expression but none was generated")
                .isPresent()
        assertThat(expression.get().toString()).isEqualToIgnoringWhitespace(expected)
    }

    void expectNoExpressionOnCode(String code) {
        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)
        FieldDeclaration fd = cu.findAll(FieldDeclaration).first()
        ResolvedFieldDeclaration rfd = fd.resolve()
        Optional<Expression> expression = new StaticFieldAccessorBuilder(rfd).build()

        assertThat(expression)
                .describedAs("Expected to not to have resolved access expression but it was generated")
                .isEmpty()
    }
}

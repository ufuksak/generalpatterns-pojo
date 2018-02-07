package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.TestUnitSpec
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration

import static org.assertj.core.api.Assertions.assertThat

class FieldAccessorBuilderSpec extends TestUnitSpec {

    def "should be able to build expression for a non-private variable"() {
        expect:
        expectOnCode("""
            class Foo {
                int i = 123; 
            }
        """, new NameExpr("foo"), "foo.i")
    }

    def "should be able to build expression for a getter with javabeans getter"() {
        expect:
        expectOnCode("""
            class Foo {
                private int i = 123;
                
                public int getI() {
                    return i;
                }
            }
        """, new NameExpr("foo"), "foo.getI()")
    }

    def "should be able to build expression for inner class with a getter"() {
        expect:
        expectOnCode("""
            class Foo {
                static class Bar {
                    private int i = 0;

                    public int getI() {
                        return i;
                    }
                }
            }
        """, new NameExpr("bar"), "bar.getI()")
    }

    void expectOnCode(String code, Expression scope, String expected) {
        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)
        FieldDeclaration fd = cu.findAll(FieldDeclaration).first()
        ResolvedFieldDeclaration rfd = fd.resolve()
        Optional<Expression> expression = new FieldAccessorBuilder(rfd, scope).build()

        assertThat(expression)
                .describedAs("Expected to have resolved access expression but none was generated")
                .isPresent()
        assertThat(expression.get().toString()).isEqualToIgnoringWhitespace(expected)
    }
}

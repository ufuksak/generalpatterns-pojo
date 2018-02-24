package com.aurea.testgenerator.ast

import com.aurea.testgenerator.TestUnitSpec
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
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

    def "should be able to build expression for a static private variable with public static getter"() {
        expect:
        expectOnCode("""
            class Foo {
                private static int i = 123;
                
                public static int getI() {
                    return i;
                }
            }
        """, "Foo.getI()")
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
        FieldAccessResult result = new StaticFieldAccessorBuilder(rfd).build()

        assertThat(result.type)
                .describedAs("Expected to have resolved access result but none was generated")
                .isEqualTo(FieldAccessResult.Type.SUCCESS)
        assertThat(result.expression.toString()).isEqualToIgnoringWhitespace(expected)
    }

    void expectNoExpressionOnCode(String code) {
        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)
        FieldDeclaration fd = cu.findAll(FieldDeclaration).first()
        ResolvedFieldDeclaration rfd = fd.resolve()
        FieldAccessResult result = new StaticFieldAccessorBuilder(rfd).build()

        assertThat(result.getType()).isEqualTo(FieldAccessResult.Type.NO_ACCESS)
    }
}

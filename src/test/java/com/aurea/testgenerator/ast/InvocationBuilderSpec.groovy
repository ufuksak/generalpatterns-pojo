package com.aurea.testgenerator.ast

import com.aurea.testgenerator.TestUnitSpec
import com.aurea.testgenerator.extensions.Extensions
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.value.ArbitraryPrimitiveValuesFactory
import com.aurea.testgenerator.value.ArbitraryReferenceTypeFactory
import com.aurea.testgenerator.value.PrimitiveValueFactory
import com.aurea.testgenerator.value.ReferenceTypeFactory
import com.aurea.testgenerator.value.ValueFactory
import com.aurea.testgenerator.value.random.ValueFactoryImpl
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName

import static org.assertj.core.api.Assertions.assertThat

class InvocationBuilderSpec extends TestUnitSpec {

    InvocationBuilder builder

    def setupSpec() {
        Extensions.enable()
    }

    def setup() {
        ReferenceTypeFactory typesFactory = new ArbitraryReferenceTypeFactory()
        PrimitiveValueFactory primitiveValueFactory = new ArbitraryPrimitiveValuesFactory()
        ValueFactory valueFactory = new ValueFactoryImpl(typesFactory, primitiveValueFactory)
        typesFactory.setValueFactory(valueFactory)
        builder = new InvocationBuilder(valueFactory)
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

    def "should be able to invoke public constructor of inner class"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
                class Bar {
                    public Bar() {}
                } 
            }
        """, """
            new Foo().new Bar()
        """
    }

    def "should be able to invoke public constructor of static inner class"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
                static class Bar {
                    public Bar(){}
                }
            }
        """, "new Foo.Bar()"
    }

    def "complex mix type of innerness"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
                static class Bar {
                    class Crowd {
                        class Bazooka {
                            public Bazooka() {}
                        }
                    }
                }
            }
        """, "new Foo.Bar().new Crowd().new Bazooka()"
    }

    def "multiple statics"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
                static class Bar {
                    static class Crowd {
                        static class Bazooka {
                            public Bazooka() {}
                        }
                    }
                }                
            }
            
        """, "new Foo.Bar.Crowd.Bazooka()"
    }

    def "simple enum invocation"() {
        expect:
        onConstructorCodeExpect("""
            enum Foo {
                I;
                
                class B {
                    B() {}
                }
            }
        """, "Foo.I.new B()")
    }

    def "inner enum as an argument of constructor"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
    
                Foo(T t) {}
                
                enum T {
                    INSTANCE
                }
            }
            
        """, """new Foo(Foo.T.INSTANCE)"""
    }

    def "with given parameters"() {
        setup:
        builder.usingForParameters([
                (new SimpleName("i")): DependableNode.from(new IntegerLiteralExpr(123))
        ])

        expect:
        onConstructorCodeExpect """
            class Foo {
                Foo(int i) {}
            }
        """, "new Foo(123)"
    }

    def "for exception type - use exceptions, not mocks"() {
        expect:
        onConstructorCodeExpect """
            class Foo {
    
                Foo($type t) {}
            }
            
        """, """new Foo(new ${type}("Test exception"))"""

        where:
        type        | _
        "Exception" | _
    }


    void onConstructorCodeExpect(String code, String expected) {
        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)
        ConstructorDeclaration cd = cu.findFirst(ConstructorDeclaration).get()
        Optional<DependableNode<ObjectCreationExpr>> testNodeExpression = builder.build(cd)
        assertThat(testNodeExpression).isPresent()
        assertThat(testNodeExpression.get().node.toString())
                .isEqualToNormalizingWhitespace(expected)
    }
}

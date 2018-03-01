package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.TestUnitSpec
import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.ReturnStmt

class StateChangeAssertionBuilderSpec extends TestUnitSpec {

    def "should be able to find field assignments in constructor called from the method"() {
        setup:
        CompilationUnit cu = JavaParser.parse """
            class Foo {
                int age;
                String username;
                
                Foo(String username, int age) {
                    this.age = age;
                    this.username = username; 
                }
                
                Foo foo() {
                    Foo foo = new Foo("ABC", 42);
                    
                    return foo;
                }
            }
        """

        expect:
        buildAssertions(cu).join('\n') ==
                """assertThat(foo.age).isEqualTo(42)
assertThat(foo.username).isEqualTo("ABC")"""
    }

    def "should be able to find setter calls"() {
        setup:
        CompilationUnit cu = JavaParser.parse """
            class Foo {
                int age;
                String username;
                
                Foo(String username, int age) {
                    setUsername(username);
                    setAge(age);
                }
                
                Foo foo() {
                    Foo foo = new Foo("ABC", 42);
                    
                    return foo;
                }
                
                public void setAge(int age) {
                    this.age = age;
                }
                
                public void setUsername(String username) {
                    this.username = username;
                }
            }
        """

        expect:
        buildAssertions(cu).join('\n') ==
                """assertThat(foo.age).isEqualTo(42)
assertThat(foo.username).isEqualTo("ABC")"""
    }

    def "should be able to resolve even when return expr is object creation expr"() {
        setup:
        CompilationUnit cu = JavaParser.parse """
            class Foo {
                int age;
                String username;
                
                Foo(String username, int age) {
                    setUsername(username);
                    setAge(age);
                }
                
                Foo foo() {
                    return new Foo("ABC", 42);
                }
                
                public void setAge(int age) {
                    this.age = age;
                }
                
                public void setUsername(String username) {
                    this.username = username;
                }
            }
        """

        expect:
        buildAssertions(cu).join('\n') ==
                """assertThat(new Foo("ABC", 42).age).isEqualTo(42)
assertThat(new Foo("ABC", 42).username).isEqualTo("ABC")"""
    }

    def "should be able to find assignments in this() call"() {
        setup:
        CompilationUnit cu = JavaParser.parse """
            class Foo {
                int age;
                String username;
                
                Foo(String username) {
                    this(username, 42);
                }
                
                Foo(String username, int age) {
                    this.age = age;
                    this.username = username;
                }
                
                Foo foo() {
                    return new Foo("ABC");
                }
                
                public void setAge(int age) {
                    this.age = age;
                }
                
                public void setUsername(String username) {
                    this.username = username;
                }
            }
        """

        expect:
        buildAssertions(cu).join('\n') ==
                """assertThat(new Foo("ABC").age).isEqualTo(42)
assertThat(new Foo("ABC").username).isEqualTo("ABC")"""
    }

    def "should be able to find assignments in super() call"() {
        setup:
        CompilationUnit cu = JavaParser.parse """
            class Base {
                int age;
                
                Base(int age) {
                    this.age = age;
                }
            }
            class Foo extends Base {
                String username;
                
                Foo(String username) {
                    super(42);
                    this.username = username;
                }
                
                Foo foo() {
                    return new Foo("ABC");
                }
                
                public void setAge(int age) {
                    this.age = age;
                }
                
                public void setUsername(String username) {
                    this.username = username;
                }
            }
        """

        expect:
        buildAssertions(cu).join('\n') ==
                """assertThat(new Foo("ABC").age).isEqualTo(42)
assertThat(new Foo("ABC").username).isEqualTo("ABC")"""
    }

    List<DependableNode<MethodCallExpr>> buildAssertions(CompilationUnit cu) {
        injectSolver(cu)
        Expression tracked = cu.findAll(ReturnStmt)[0].expression.get()
        MethodDeclaration context = cu.findAll(MethodDeclaration).first()
        StateChangeAssertionBuilder builder = new StateChangeAssertionBuilder(
                tracked,
                tracked,
                context,
                javaParserFacade)

        builder.assertions
    }
}

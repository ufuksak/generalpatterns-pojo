package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator

class ConstructorArgumentAssignmentSpec extends MatcherPipelineTest {

    def "assigning integral arguments should be asserted"() {
        expect:
        onClassCodeExpect """
            class Foo {
                int i;
                
                Foo(int i) {
                    this.i = i;
                }
            } 
        """, """     
            package sample;
            
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_FooWithOneArgument_AssignsGivenArguments() throws Exception {
                    int i = 42;
                    
                    Foo foo = new Foo(i);
                    
                    assertThat(foo.i).isEqualTo(i);
                }
            }
        """
    }

    def "assigning strings should be asserted"() {
        expect:
        onClassCodeExpect """
            class Foo {
                String i;
                
                Foo(String i) {
                    this.i = i;
                }
                
            }
        """, """
            package sample;
            
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_FooWithOneArgument_AssignsGivenArguments() throws Exception {
                    String i = "ABC";
                    
                    Foo foo = new Foo(i);
                    
                    assertThat(foo.i).isEqualTo(i);
                }
            }
        """
    }

    def "assigning list should add collections import"() {
        expect:
        onClassCodeExpect """
            import java.util.List;

            class Foo {
                List<String> i;
                
                Foo(List<String> i) {
                    this.i = i;
                }
                
            }
        """, """
            package sample;
            
            import java.util.List;
            import java.util.Collections;
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_FooWithOneArgument_AssignsGivenArguments() throws Exception {
                    List<String> i = Collections.singletonList("ABC");
                    
                    Foo foo = new Foo(i);
                    
                    assertThat(foo.i).isEqualTo(i);
                }
            }
        """
    }

    def "type of the variable should be fully qualified"() {
        expect:
        onClassCodeExpect """
            class Foo {
                static class Bar {
                    int i;
                    Bar(int i) {
                        this.i = i;
                    }
                } 
            }   
        """, """                   
            package sample;
            
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_BarWithOneArgument_AssignsGivenArguments() throws Exception {
                    int i = 42;
                    Foo.Bar bar = new Foo.Bar(i);
                    assertThat(bar.i).isEqualTo(i);
                }
            }
        """
    }

    def "assigning in super should be tested"() {
        expect:
        withClass("""
            class Base {
                int i;
                
                Base(int i) {
                    this.i = i;
                }
            }
        """).onClassCodeExpect """
            
            class Foo extends Base {
                Foo(int i) {
                    super(i);
                }
            }   
        """, """                   
            package sample;
            
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_FooWithOneArgument_AssignsGivenArguments() throws Exception {
                    int i = 42;
                    Foo foo = new Foo(i);
                    assertThat(foo.i).isEqualTo(i);
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        TestGenerator generator = new ConstructorArgumentAssignmentGenerator(solver, valueFactory)
        generator.reporter = reporter
        generator.nomenclatures = nomenclatureFactory
        generator.visitReporter = visitReporter
        generator
    }
}

package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.constructors.ArgumentAssignmentGenerator

class ArgumentAssignmentSpec extends MatcherPipelineTest {

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
            
            import org.junit.Test;
            import static org.assertj.core.api.Assertions.assertThat;
            
            public class FooTest {
                
                @Test
                public void test_Foo_AssignsArgumentsToFields() throws Exception {
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
            
            import org.junit.Test;
            import static org.assertj.core.api.Assertions.assertThat;
            
            public class FooTest {
                
                @Test
                public void test_Foo_AssignsArgumentsToFields() throws Exception {
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
            import org.junit.Test;
            import java.util.Collections;
            import static org.assertj.core.api.Assertions.assertThat;
            
            public class FooTest {
                
                @Test
                public void test_Foo_AssignsArgumentsToFields() throws Exception {
                    List<String> i = Collections.singletonList("ABC");
                    
                    Foo foo = new Foo(i);
                    
                    assertThat(foo.i).isEqualTo(i);
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        new ArgumentAssignmentGenerator(new FieldAssignments(solver), solver, valueFactory)
    }
}

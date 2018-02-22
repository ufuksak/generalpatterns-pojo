package com.aurea.testgenerator.generation.patterns

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.patterns.methods.DifferentInstancesAbstractFactoryMethodTestGenerator

class DifferentInstancesAbstractFactoryMethodTestGeneratorSpec extends MatcherPipelineTest {

    def "static factory method should create different instances on consecutive invocation"() {
        expect:
        onClassCodeExpect """
            class Foo {
                public static Foo newFoo(String username) {
                    return new Foo();
                }   
            }
        """, """     
            package sample;
             
            import org.junit.Test;
            import static org.assertj.core.api.Assertions.assertThat;
             
            public class FooTest {
             
                @Test
                public void test_newFoo_OnSecondCall_CreateDifferentInstance() throws Exception {
                    Foo first = Foo.newFoo("ABC");
                    Foo other = Foo.newFoo("ABC");
                    
                    assertThat(first).isNotSameAs(other);
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        new DifferentInstancesAbstractFactoryMethodTestGenerator(solver, reporter, visitReporter, nomenclatureFactory, valueFactory)
    }
}

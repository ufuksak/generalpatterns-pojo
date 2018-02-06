package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.constructors.FieldLiteralAssignmentsGenerator
import com.aurea.testgenerator.pattern.PatternMatcher


class FieldLiteralAssignmentsSpec extends MatcherPipelineTest {

    def "assigning integral literals should be asserted"() {
        expect:
        onClassCodeExpect """
            class Foo {
                int i;
                Foo() {
                    this.i = 42;
                }
            } 
        """, """     
            package sample;
            
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_Foo_AssignsConstantsToFields() throws Exception {
                    Foo foo = new Foo();
                    
                    assertThat(foo.i).isEqualTo(42);
                }
            }
        """

    }

    @Override
    PatternMatcher matcher() {
        return new ConstructorMatcher()
    }

    @Override
    PatternToTest patternToTest() {
        return new FieldLiteralAssignmentsGenerator(solver)
    }
}

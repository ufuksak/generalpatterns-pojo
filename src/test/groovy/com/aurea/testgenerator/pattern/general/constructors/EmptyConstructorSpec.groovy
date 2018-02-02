package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.generation.constructors.EmptyConstructorGenerator
import com.aurea.testgenerator.pattern.PatternMatcher

class EmptyConstructorSpec extends MatcherPipelineTest {

    def "empty constructor test"() {
        expect:
        onClassCodeExpect """
        class Foo {
            Foo() {}
        }           
        """, """
package sample;

import org.junit.Test;

public class FooTest {
    
    @Test
    public void test_Foo_IsInstantiable() throws Exception {
        new Foo();
    }
}
"""
    }

    @Override
    PatternMatcher matcher() {
        new ConstructorMatcher()
    }

    @Override
    UnitTestGenerator generator() {
        new EmptyConstructorGenerator()
    }
}

package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.generation.constructors.EmptyConstructorGenerator
import com.aurea.testgenerator.pattern.PatternMatcher

class EmptyConstructorSpec extends MatcherPipelineTest {

    def "empty constructor simplest case"() {
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

    def "if constructor is private - no test"() {
        expect:
        onClassCodeDoNotExpectTest """
        class Foo {
            private Foo() {}
        }
        """
    }

    def "if constructor is public but not reachable - no test"() {
        expect:
        onClassCodeDoNotExpectTest """
        class Foo {
            private static class A {
                public A() {}
            }
        }
        """
    }

    def "if constructor is in local class - no test"() {
        expect:
        onClassCodeDoNotExpectTest """
        class Foo {
            void foo() {
                class A {
                    public A() {}
                }
            }
        }
        """
    }

    def "nested constructor of non static is being properly called"() {
        expect:
        onClassCodeExpect """
        class Foo {
            class Bar {
                Bar() {}
            }
        }
        """, """
        package sample;
        
        import org.junit.Test;
        
        public class FooTest {
            
            @Test
            public void test_Bar_IsInstantiable() throws Exception {
                new Foo().new Bar();
            }
        }
        """
    }

    def "nested constructor of static class is being properly called"() {
        expect:
        onClassCodeExpect """
        class Foo {
            static class Bar {
                Bar() {}
            }
        }
        """, """
        package sample;
        
        import org.junit.Test;
        
        public class FooTest {
            
            @Test
            public void test_Bar_IsInstantiable() throws Exception {
                new Foo.Bar();
            }
        }
        """
    }

    @Override
    PatternMatcher matcher() {
        new ConstructorMatcher()
    }

    @Override
    PatternToTest patternToTest() {
        new EmptyConstructorGenerator()
    }
}

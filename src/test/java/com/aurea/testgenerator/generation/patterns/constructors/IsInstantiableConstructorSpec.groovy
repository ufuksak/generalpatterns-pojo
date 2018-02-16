package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.MethodLevelTestGenerator

class IsInstantiableConstructorSpec extends MatcherPipelineTest {

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

    def "inner enum as a type"() {
        expect:
        onClassCodeExpect """
            class Foo {
                
                enum Status {
                    SUCCESS
                }
                
                Foo(Status status) {}
            }
        """, """
            package sample;
            
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_FooWithOneArgument_IsInstantiable() throws Exception {
                    new Foo(Foo.Status.SUCCESS);
                }
            }
        """
    }

    def "inner static class as a type"() {
        expect:
        onClassCodeExpect """
            class Foo {
                
                static class Status {
                }
                
                Foo(Status status) {}
            }
        """, """
            package sample;
            
            import static org.mockito.Mockito.mock;
            import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
            import org.junit.Test;
            
            public class FooTest {
                
                @Test
                public void test_FooWithOneArgument_IsInstantiable() throws Exception {
                    new Foo(mock(Foo.Status.class, RETURNS_DEEP_STUBS));
                }
            }
        """
    }

    @Override
    MethodLevelTestGenerator generator() {
        new IsInstantiableConstructorGenerator(solver, reporter, visitReporter, nomenclatureFactory, valueFactory)
    }
}

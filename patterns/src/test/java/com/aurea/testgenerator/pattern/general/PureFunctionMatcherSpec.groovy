package com.aurea.testgenerator.pattern.general

import com.aurea.testgenerator.pattern.MethodMatcherTestSpec
import com.aurea.testgenerator.pattern.PatternMatch

import static org.assertj.core.api.Assertions.assertThat

class PureFunctionMatcherSpec extends MethodMatcherTestSpec<PureFunctionMatcher> {

    def "simple result return is pure"() {
        expect:
        matchOnMethod '''
int foo(int a, int b) {
    return a + b;
}
'''
    }

    def "if else branches are pure"() {
        expect:
        matchOnMethod '''
int foo(int a, int b) {
    if (a == 42) {
        return b;
    }
    return a;
}
'''
    }

    def "accessing element of array is pure"() {
        expect:
        matchOnMethod '''
int foo(int[] a) {
    return a[0];
}
'''
    }

    def "assigning value to an input array is impure"() {
        expect:
        mismatchOnMethod '''
void foo(int[] a) {
     a[0] = 1;
}
'''
    }

    def "returning new array is pure"() {
        expect:
        matchOnMethod '''
int[] foo() {
    return new int[10];
}
'''
    }

    def "assigning values to new array is pure"() {
        expect:
        matchOnMethod '''
int[] foo() {
    int[] arr = new int[10];
    arr[4] = 42;
    return arr;
}
'''
    }

    def "assigning value to a field array is impure"() {
        expect:
        mismatchOnClass '''
class Foo {
    private int[] i;
 
    void foo() {
        i[0] = 123;
    }
}'''
    }

    def "switch case is pure"() {
        expect:
        matchOnMethod '''
int foo(int a) {
    switch (a) {
        case 12:
            return 145;
        default:
            return 1;
    }
}
'''
    }

    def "getting static field value is pure"() {
        expect:
        matchOnClass '''
class Foo {
    private static final int I = 123;
 
    int foo() {
        return I;
    }
}
'''
    }

    def "getting non constant field value should be impure"() {
        expect:
        mismatchOnClass '''
class Foo {
    private final int i = 123;
 
    int foo() {
        return this.i;
    }
}
'''
    }

    def "getting static constant field should be pure"() {
        expect:
        mismatchOnClass '''
class Foo {
    private static final int i = 123;
 
    int foo() {
        return i;
    }
}
'''
    }

    def "assigning to field value should is impure"() {
        expect:
        mismatchOnClass '''
class Foo {
    private int i = 123;
 
    void foo(int b) {
        this.i = b;
    }
}
'''
    }

    def "creating new object is pure"() {
        expect:
        matchOnMethod '''
Object foo() {
    return new Object();
}
'''
    }

    def "assigning new object to field is impure"() {
        expect:
        mismatchOnClass '''
class Foo {
    Object field;
    
    Object foo() {
        field = new Object();
        return field;
    }
}
'''
    }

    def "creating arrays is pure"() {
        expect:
        matchOnMethod '''
int[] foo() {
     return new int[] { 1, 2, 3};
}
'''
    }

    def "assigning arrays is impure"() {
        expect:
        mismatchOnClass '''
class Foo {
    int[] b;
    int[] foo() {
        b = new int[] { 1, 2, 3};
        return b;
    }
}
'''
    }

    def "method functions by reference is pure (testing TypeExpr)"() {
        expect:
        matchOnClass '''
class Foo {
    int foo() {
        Supplier<Integer> foo = Foo::foo;
    }
}
'''
    }

    def "incrementing primitive input is pure"() {
        expect:
        matchOnMethod '''
int foo(int a) {
    return ++a;
}
'''
    }

    void matchOnMethod(String code) {
        Optional<? extends PatternMatch> match = runOnMethod(code)

        assertThat(match.present).isTrue()
    }

    void mismatchOnMethod(String code) {
        Optional<? extends PatternMatch> match = runOnMethod(code)

        assertThat(match.present).isFalse()
    }

    void matchOnClass(String code) {
        Optional<? extends PatternMatch> match = runOnClass(code)

        assertThat(match.present).isTrue()
    }

    void mismatchOnClass(String code) {
        Optional<? extends PatternMatch> match = runOnClass(code)

        assertThat(match.present).isFalse()
    }
}

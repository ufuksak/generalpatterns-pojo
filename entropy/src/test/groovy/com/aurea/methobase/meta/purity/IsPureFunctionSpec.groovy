package com.aurea.methobase.meta.purity

import com.aurea.ast.common.UnitHelper
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.symbolsolver.SymbolSolver
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class IsPureFunctionSpec extends Specification {

    static final SourceFilter NO_FILTER = new SourceFilter() {
        @Override
        boolean test(Path path) {
            true
        }
    }

    IsPureFunction matcher

    def setup() {
        JavaSourceFinder finder = new JavaSourceFinder()
        SymbolSolver solver = new SymbolSolver(finder, Paths.get(""), NO_FILTER)
        matcher = new IsPureFunction(solver)
    }

    def "simple result return is pure"() {
        expect:
        runOnMethod '''
            int foo(int a, int b) {
            return a + b;
        }
        '''
    }

    def "if else branches are pure"() {
        expect:
        runOnMethod '''
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
        runOnMethod '''
            int foo(int[] a) {
            return a[0];
        }
        '''
    }

    def "assigning value to an input array is impure"() {
        expect:
        !runOnMethod('''
            void foo(int[] a) {
             a[0] = 1;
        }
        ''')
    }

    def "returning new array is pure"() {
        expect:
        runOnMethod '''
            int[] foo() {
            return new int[10];
        }
        '''
    }

    def "assigning values to new array is pure"() {
        expect:
        runOnMethod '''
            int[] foo() {
                int[] arr = new int[10];
                arr[4] = 42;
                return arr;
            }
        '''
    }

    def "assigning value to a field array is impure"() {
        expect:
        !runOnClass('''
            class Foo {
                private int[] i;
             
                void foo() {
                i[0] = 123;
            }
        }''')
    }

    def "switch case is pure"() {
        expect:
        runOnMethod '''
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
        runOnClass '''
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
        !runOnClass('''
            class Foo {
                private final int i = 123;
             
                int foo() {
                    return this.i;
                }
            }
        ''')
    }

    def "getting static constant field should be pure"() {
        expect:
        !runOnClass('''
            class Foo {
                private static final int i = 123;
             
                int foo() {
                    return Foo.i;
                }
            }
        ''')

    }

    def "getting static constant field without referencing class should be pure"() {
        expect:
        !runOnClass('''
            class Foo {
                private static final int i = 123;
             
                int foo() {
                    return i;
                }
            }
        ''')

    }

    def "getting static constant field with 'this' reference should be pure"() {
        expect:
        !runOnClass('''
            class Foo {
                private static final int i = 123;
             
                int foo() {
                    return this.i;
                }
            }
        ''')
    }

    def "assigning to field value should is impure"() {
        expect:
        !runOnClass('''
            class Foo {
                private int i = 123;
             
                void foo(int b) {
                    this.i = b;
                }
            }
        ''')

    }

    def "creating new object is pure"() {
        expect:
        runOnMethod '''
            Object foo() {
    return new Object();
}
        '''
    }

    def "assigning new object to field is impure"() {
        expect:
        !runOnClass('''
            class Foo {
                Object field;
                
                Object foo() {
                    field = new Object();
                    return field;
                }
            }
        ''')

    }

    def "creating arrays is pure"() {
        expect:
        runOnMethod '''
            int[] foo() {
     return new int[] { 1, 2, 3};
}
        '''
    }

    def "assigning arrays is impure"() {
        expect:
        !runOnClass('''
            class Foo {
                int[] b;
                int[] foo() {
                    b = new int[] { 1, 2, 3};
                    return b;
                }
            }
        ''')
    }

    def "method functions by reference is pure (testing TypeExpr)"() {
        expect:
        runOnClass '''
            class Foo {
                int foo() {
                    Supplier<Integer> foo = Foo::foo;
                }
            }
        '''
    }

    def "incrementing primitive input is pure"() {
        expect:
        runOnMethod '''
            int foo(int a) {
    return ++a;
}
        '''
    }

    boolean runOnMethod(String code) {
        CompilationUnit cu = UnitHelper.getUnitForMethod(code).get()
        MethodDeclaration md = cu.findAll(MethodDeclaration).first()

        matcher.test(md)
    }

    boolean runOnClass(String code) {
        CompilationUnit cu = UnitHelper.getUnitForCode(code).get()
        MethodDeclaration md = cu.findAll(MethodDeclaration).first()

        matcher.test(md)
    }
}

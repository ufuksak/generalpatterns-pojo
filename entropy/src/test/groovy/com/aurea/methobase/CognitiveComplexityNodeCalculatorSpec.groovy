package com.aurea.methobase

import com.aurea.ast.common.UnitHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import spock.lang.Specification

class CognitiveComplexityNodeCalculatorSpec extends Specification {
    def "Gets expected score for simple if condition"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                    if (a != b) // +1 for if, +1 for binary operator
                        {}
                }
            }
        '''
        score == 2
    }

    def "Enclosed boolean expr"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void myMethod() {
                    // != + && + > + || + != in total 5 different operators
                    boolean r = a != b && b > c || c != d; 
                }
            }
        '''
        score == 5
    }

    def "boolean expr and instanceof"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void myMethod() {
                    // +2 for different operators  instanceof and AND
                    boolean r = (a instanceof Object) && b; 
                }
            }

        '''
        score == 2
    }


    def "Gets expected score for recursive call"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                     myMethod();
                }
            }
        '''
        score == 1
    }

    def "Gets expected score for if-else"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                     if (condition) { // +1
                     } else { // +1
                     }
                }
            }
        '''
        score == 2
    }

    def "Gets expected score for if-elseif-else"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                     if (condition) { // +1
                     } else if (condition2) { // +1
                     } else { // +1
                     }
                }
            }
        '''
        score == 3
    }

    def "Gets expected score for if-else-if"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                     if (condition) { // +1
                     } else if(condition2) { // +1
                     }
                }
            }
        '''
        score == 2
    }


    def "Gets expected score for if-else-if-complex-condition"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                     if (condition) { // +1
                     } else if(a //+1
                     && b) { // +1
                     }
                }
            }
        '''
        score == 3
    }

    def "Gets expected score for if-if-else-if"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                     if (condition1) // +1
                       if (condition2) {} // +2 (nesting = 2)
                       else if (condition3) {}  // +1
                }
            }
        '''
        score == 4
    }

    def "Expected score from empty method"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void foo() {}
            }
        '''
        score == 0
    }

    def "Expected score from method with a statement"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void foo() {
                    System.out.println("Boom");
                }
            }
        '''
        score == 0
    }

    def "Expected score from method with multiple statements"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void foo() {
                    System.out.println("Boom");
                    System.out.println("Bam");
                    System.out.println("Bidissshh");
                }
    
            }
        '''
        score == 0
    }

    def "Recursive method with sequence of binary logical operators"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void foo(boolean b) {
                    foo(1 || 2 && 3); //+1 recursive, +2 for two different binary operators
                }
            }
        '''
        score == 3
    }

    def "Recursive method call inside of binary logical sequence"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                boolean foo() {
                    return foo() && 1 || 2; //+1 recursive, +2 for two different binary operators
                }
            }
        '''
        score == 3
    }

    def "Ternary operator"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void foo() {
                    boolean a = true ? true: false;
                }
    
            }
        '''
        score == 1
    }

    def "Nesting increments"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                void foo() {
                    if (condition1) { // +1
                        for (int i = 0; i < 10; i++) { // +3 -> +2 from ForStmt (nesting=1) + +1 from "i < 10"
                            while (condition2) {} // +3 (nesting=2)
                        }
                    }
                }
            }
        '''
        score == 7
    }

    int scoreOfMethodInClass(String methodInClassCode) {
        Optional<CompilationUnit> maybeUnit = UnitHelper.getUnitForCode(methodInClassCode)
        CompilationUnit unit = maybeUnit.orElseThrow { throw new IllegalArgumentException("Faled to parse code: $methodInClassCode") }
        CognitiveComplexityNodeCalculator.visit(unit.getNodesByType(MethodDeclaration).first())
    }
}

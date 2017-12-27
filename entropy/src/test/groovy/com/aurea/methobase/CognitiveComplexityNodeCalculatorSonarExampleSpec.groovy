package com.aurea.methobase

import com.aurea.ast.common.UnitHelper
import com.github.javaparser.ast.body.MethodDeclaration
import spock.lang.Specification

/*
 * The following methods tests the java examples provided in
 * https://www.sonarsource.com/docs/CognitiveComplexity.pdf
*/

class CognitiveComplexityNodeCalculatorSonarExampleSpec extends Specification {

    def "Gets expected score for example 1"() {
        expect:
        int score = scoreOfMethodInClass '''
        class Foo {
             public void myMethod() {
                if (a // +1 for `if`
                    && b && c // +1
                    || d || e // +1
                    && f) // +1
                    {}
             }
        }
        '''
        score == 4
    }

    def "Gets expected score for example 2"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                public void myMethod() {
                    if (a // +1 for `if`
                         && // +1
                         !(b && c)) // +1
                            {}
               }
            }
        '''
        score == 3
    }

    def "Gets expected score for example 3"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
               void myMethod () {
                    try {
                        if (condition1) { // +1
                            for (int i = 0; i < 10; i++) { // +3 (nesting=1), +2 nested if, +1 "i < 10"
                                while (condition2) {} // +3 (nesting=2)
                            }
                        }
                    } catch (ExcepType1 | ExcepType2 e) { // +1
                        if (condition2) {} // +2 (nesting=1)
                    }
                } // Cognitive Complexity 10
            }
        '''
        score == 10
    }

    def "Gets expected score for example 4 (lambda)"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
               void myMethod2 () {
                    Runnable r = () -> { // +0 (but nesting level is now 1)
                        if (condition1) {} // +2 (nesting=1)
                    };
                 }
            }
        '''
        score == 2
    }

    def "Gets expected score for example 5"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                private void addVersion(final Entry entry, final Transaction txn)
                        throws PersistitInterruptedException, RollbackException {
                    final TransactionIndex ti = _persistit.getTransactionIndex();
                    while (true) { // total = 1, nesting = 1
                        try {
                            synchronized (this) { // total = 3, nesting = 2
                                if (frst != null) { // total = 7, nesting = 3
                                    if (frst.getVersion() > entry.getVersion()) { // total = 12, nesting = 4
                                        throw new RollbackException();
                                    }
                                    if (txn.isActive()) { // total = 16, nesting = 4
                                        for (Entry e = frst; e != null; e = e.getPrevious()) { // total = 22, nesting = 5
                                            final long version = e.getVersion();
                                            final long depends = ti.wwDependency(version, txn.getTransactionStatus(), 0);
                                            if (depends == TIMED_OUT) { // total = 29, nesting = 6
                                                throw new WWRetryException(version);
                                            }
                                            if (depends != 0 && depends != ABORTED) { // total = 38, nesting = 6
                                                throw new RollbackException();
                                            }
                                        }
                                    }
                                }
                                entry.setPrevious(frst);
                                frst = entry;
                                break;
                            }
                        } catch (final WWRetryException re) { // total = 40, nesting = 2
                            try {
                                final long depends = _persistit.getTransactionIndex().wwDependency(re.getVersionHandle(),
                                        txn.getTransactionStatus(), SharedResource.DEFAULT_MAX_WAIT_TIME);
                                if (depends != 0 && depends != ABORTED) { // total = 46, nesting = 3
                                    throw new RollbackException();
                                }
                            } catch (final InterruptedException ie) { // total = 49, nesting = 3
                                throw new PersistitInterruptedException(ie);
                            }
                        } catch (final InterruptedException ie) { // total = 51, nesting = 2
                            throw new PersistitInterruptedException(ie);
                        }
                    }
                }
            }
        '''
        score == 51
    }

    def "Gets expected score for example 6"() {
        expect:
        int score = scoreOfMethodInClass '''
            class Foo {
                String toRegexp(String antPattern, String directorySeparator) {
                    final String escapedDirectorySeparator = "/" + directorySeparator; // total = 1, nesting = 0
                    final StringBuilder sb = new StringBuilder(antPattern.length());
                    sb.append("^");
                    int i = antPattern.startsWith("/") || antPattern.startsWith("/") ? 1 : 0; // total = 3, nesting = 0
                    while (i < antPattern.length()) { // total = 5, nesting = 1
                        final char ch = antPattern.charAt(i);
                        if (SPECIAL_CHARS.indexOf(ch) != -1) { // total = 9, nesting = 2
                            sb.append("/").append(ch);
                        }
                        else if (ch == "*") { // total = 11, nesting = 2
                            if (i + 1 < antPattern.length() && antPattern.charAt(i + 1) == "*") { // total = 19, nesting = 3
                                if (i + 2 < antPattern.length() && isSlash(antPattern.charAt(i + 2))) { // total = 27, nesting = 4
                                    sb.append("(?:.*").append(escapedDirectorySeparator).append("|)");
                                    i += 2;
                                }
                                else { // total = 28, nesting = 4
                                    sb.append(".*");
                                    i += 1;
                                }
                            }
                            else { // total = 29, nesting = 3
                                sb.append("[^").append(escapedDirectorySeparator).append("]*?");
                            }
                        } else if (ch == "?") { // total = 31, nesting = 2
                            sb.append("[^").append(escapedDirectorySeparator).append("]");
                        } else if (isSlash(ch)) { // total = 32, nesting = 2
                            sb.append(escapedDirectorySeparator);
                        } else { // total = 33, nesting = 2
                            sb.append(ch);
                        }
                        i++;
                    }
                    sb.append("/$");
                    return sb.toString();
                } // total complexity = 33
            }
        '''
        score == 33
    }

    int scoreOfMethodInClass(String methodInClassCode) {
        MethodDeclaration method = UnitHelper.getMethodFromSource(methodInClassCode)
        CognitiveComplexityNodeCalculator.visit(method)
    }
}

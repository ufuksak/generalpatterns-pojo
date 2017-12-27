package com.aurea.methobase

import com.aurea.ast.common.UnitHelper
import com.aurea.methobase.meta.ClassMetaInformation
import com.aurea.testgenerator.symbolsolver.SymbolSolver
import com.github.javaparser.ast.CompilationUnit
import spock.lang.Specification

import java.nio.file.Paths

class TypeVisitorSpec extends Specification {

    def "Just class"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Foo {
        }
        '''

        metas.first().name == "Foo"
        metas.first().outerType.empty
    }

    def "class with full name"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        package my.awesome.pack; 
        
        class Foo {
        }
        '''

        metas.first().name == "my.awesome.pack.Foo"
        metas.first().outerType.empty
    }

    def "inner class full name"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        package my.awesome.pack; 
        
        class Foo {
            class Bar {
                
            }
        }
        '''

        metas.collect { it.name }.containsAll([
                'my.awesome.pack.Foo',
                'my.awesome.pack.Foo$Bar'])
        metas.find {it.name.contains('Bar')}.outerType == 'my.awesome.pack.Foo'
    }

    def "inner inner class full name"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        package my.awesome.pack; 
        
        class Foo {
            class Bar {
                class XYZ {
                }
            }
        }
        '''

        metas.collect { it.name }.containsAll([
                'my.awesome.pack.Foo',
                'my.awesome.pack.Foo$Bar',
                'my.awesome.pack.Foo$Bar$XYZ'
        ])

        metas.find {it.name.contains('XYZ')}.outerType == 'my.awesome.pack.Foo$Bar'
    }

    def "inner class without package"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Foo {
            class Bar {
            }
        }
        '''

        metas.collect { it.name }.containsAll(['Foo', 'Foo$Bar'])
    }

    def "anonymous classes are ignored"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Foo {
            void foo() {
                new Bar() {
                };
            }
            
        }
        '''

        metas.size() == 1
        metas[0].name == 'Foo'
    }

    def "local class"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Foo {
            void foo() {
                class Bar {
                    
                }
            }
        }
        '''

        metas.collect { it.name }.containsAll(['Foo', 'Foo$1Bar'])
    }

    def "local classes"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Y {
            void f() {
                class X {
                }
                class Z {
                }
            }
        }
        '''

        metas.collect { it.name }.containsAll(['Y', 'Y$1X', 'Y$1Z'])
    }

    def "local class in constructor"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Foo {
            Foo() {
                class Bar {
                    
                }
            }
        }
        '''

        metas.collect { it.name }.containsAll(['Foo', 'Foo$1Bar'])
    }

    def "local class in static block"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        class Foo {
            static {
                class Bar {
                }
            }
        }
        '''

        metas.collect { it.name }.containsAll(['Foo', 'Foo$1Bar'])
    }

    def "enum"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        enum Foo {
        }
        '''

        metas.first().name == 'Foo'
    }

    def "enum with inner class"() {
        expect:
        List<ClassMetaInformation> metas = fromCode '''
        enum Foo {
            INSTANCE;
            
            class B {}
        }
        '''

        metas.collect { it.name }.containsAll(['Foo', 'Foo$B'])
        metas.find {it.name.contains('B')}.outerType == 'Foo'
    }

    List<ClassMetaInformation> fromCode(String code) {
        CompilationUnit unit = UnitHelper.getUnitForCode(code)
        SymbolSolver solverStub = Mock()
        solverStub.getParentOf(_) >> Optional.empty()
        solverStub.getOfType(_, _) >> Optional.empty()
        TypeVisitor visitor = new TypeVisitor(solverStub)
        unit.accept(visitor, new Unit(cu: unit, modulePath: Paths.get("dummy")))
        visitor.metas
    }

}

package com.aurea.testgenerator.generation.names

import com.aurea.testgenerator.generation.patterns.constructors.ConstructorTypes
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import spock.lang.Specification

class TestMethodNomenclatureSpec extends Specification {

    TestMethodNomenclature nameRepository = new TestMethodNomenclature()

    def "no arg constructor"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            Foo() {}
        }
        """

        when:
        String name = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors.first())

        then:
        name == "test_Foo_IsInstantiable"
    }

    def "two constructors"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            Foo() {}
            Foo(int i) {}
        }
        """

        when:
        String noArg = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors[0])
        String arg = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors[1])

        then:
        noArg == 'test_Foo_IsInstantiable'
        arg == 'test_FooWithOneArgument_IsInstantiable'
    }

    def "two constructors with same number of arguments"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            Foo(boolean f) {}
            Foo(int i) {}
        }
        """

        when:
        String boolArg = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors[0])
        String intArg = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors[1])

        then:
        boolArg == 'test_FooWithOneArgument_IsInstantiable'
        intArg == 'test_FooWithIntArgument_IsInstantiable'
    }

    def "constructors with array argument"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            Foo(int[] arr) {}
            Foo(double[] arr) {}
        }
        """

        when:
        String intArrayArg = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors[0])
        String doubleArrayArg = nameRepository.requestTestMethodName(ConstructorTypes.EMPTY_CONSTRUCTOR,
                constructors[1])

        then:
        intArrayArg == 'test_FooWithOneArgument_IsInstantiable'
        doubleArrayArg == 'test_FooWithDoubleArrayArgument_IsInstantiable'

    }

    List<ConstructorDeclaration> getConstructors(String code) {
        JavaParser.parse(code).findAll(ConstructorDeclaration)
    }
}

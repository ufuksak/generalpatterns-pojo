package com.aurea.testgenerator.generation.names

import com.aurea.testgenerator.generation.patterns.staticfactory.StaticFactoryMethodTypes
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
        String name = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors.first())

        then:
        name == "test_Foo_OnSecondCall_CreateDifferentInstance"
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
        String noArg = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[0])
        String arg = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[1])

        then:
        noArg == 'test_Foo_OnSecondCall_CreateDifferentInstance'
        arg == 'test_FooWithOneArgument_OnSecondCall_CreateDifferentInstance'
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
        String boolArg = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[0])
        String intArg = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[1])

        then:
        boolArg == 'test_Foo_OnSecondCall_CreateDifferentInstance'
        intArg == 'test_FooWithOneArgument_OnSecondCall_CreateDifferentInstance'
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
        String intArrayArg = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[0])
        String doubleArrayArg = nameRepository.requestTestMethodName(StaticFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[1])

        then:
        intArrayArg == 'test_Foo_OnSecondCall_CreateDifferentInstance'
        doubleArrayArg == 'test_FooWithOneArgument_OnSecondCall_CreateDifferentInstance'

    }

    List<ConstructorDeclaration> getConstructors(String code) {
        JavaParser.parse(code).findAll(ConstructorDeclaration)
    }
}

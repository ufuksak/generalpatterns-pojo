package com.aurea.testgenerator.generation.names

import com.aurea.testgenerator.generation.patterns.methods.AbstractFactoryMethodTypes
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import spock.lang.Specification

class TestMethodNomenclatureSpec extends Specification {

    TestMethodNomenclature nameRepository = new TestMethodNomenclature()

    def "no arg constructor"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            foo() {}
        }
        """

        when:
        String name = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors.first())

        then:
        name == "test_foo_OnSecondCall_CreateDifferentInstance"
    }

    def "two constructors"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            foo() {}
            foo(int i) {}
        }
        """

        when:
        String noArg = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[0])
        String arg = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[1])

        then:
        noArg == 'test_foo_OnSecondCall_CreateDifferentInstance'
        arg == 'test_fooWithOneArgument_OnSecondCall_CreateDifferentInstance'
    }

    def "two constructors with same number of arguments"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            foo(boolean f) {}
            foo(int i) {}
        }
        """

        when:
        String boolArg = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[0])
        String intArg = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[1])

        then:
        boolArg == 'test_foo_OnSecondCall_CreateDifferentInstance'
        intArg == 'test_fooWithOneArgument_OnSecondCall_CreateDifferentInstance'
    }

    def "constructors with array argument"() {
        setup:
        List<ConstructorDeclaration> constructors = getConstructors """
        class Foo {
            foo(int[] arr) {}
            foo(double[] arr) {}
        }
        """

        when:
        String intArrayArg = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[0])
        String doubleArrayArg = nameRepository.requestTestMethodName(AbstractFactoryMethodTypes.DIFFERENT_INSTANCES,
                constructors[1])

        then:
        intArrayArg == 'test_foo_OnSecondCall_CreateDifferentInstance'
        doubleArrayArg == 'test_fooWithOneArgument_OnSecondCall_CreateDifferentInstance'

    }

    List<ConstructorDeclaration> getConstructors(String code) {
        JavaParser.parse(code).findAll(ConstructorDeclaration)
    }
}

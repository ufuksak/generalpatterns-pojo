package com.aurea.testgenerator.generation.names

import com.aurea.common.ParsingUtils
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.constructors.ConstructorTypes
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.google.common.base.Splitter
import one.util.streamex.StreamEx
import pl.allegro.finance.tradukisto.ValueConverters

class NameRepository {

    static final String TEST_NAME_PREFIX = "test_"
    static final String TEST_NAME_SPACE = "_"

    Set<String> takenNames = []

    String requestTestName(TestType type, Node context) {
        String testName
        if (type instanceof ConstructorTypes) {
            try {
                testName = requestConstructorTestName(type as ConstructorTypes, context as ConstructorDeclaration)
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("Cannot generate name for $type in $context", cce)
            }
        } else {
            throw new IllegalArgumentException("Cannot generate name for $type in $context")
        }
        takenNames << testName
        return testName
    }

    String requestConstructorTestName(ConstructorTypes type, ConstructorDeclaration constructorDeclaration) {
        String suffix
        switch (type) {
            case ConstructorTypes.EMPTY_CONSTRUCTOR:
                suffix = "IsInstantiable"
                break
            case ConstructorTypes.FIELD_LITERAL_ASSIGNMENTS:
                suffix = "AssignsConstants"
                break
            case ConstructorTypes.ARGUMENT_ASSIGNMENTS:
                suffix = "AssignsGivenArguments"
                break
        }
        new ConstructorNameRepository(suffix, constructorDeclaration).get()
    }

    class ConstructorNameRepository {
        String suffix
        ConstructorDeclaration constructorDeclaration

        ConstructorNameRepository(String suffix, ConstructorDeclaration constructorDeclaration) {
            this.suffix = suffix
            this.constructorDeclaration = constructorDeclaration
        }

        String get() {
            String name = asTypeName()
            if (name in takenNames) {
                name = asNumberOfArguments()
            }
            if (name in takenNames) {
                name = asTypeOfArguments()
            }
            name
        }

        String asTypeName() {
            [TEST_NAME_PREFIX, constructorDeclaration.nameAsString, suffix].join(TEST_NAME_SPACE)
        }

        String asNumberOfArguments() {
            constructorDeclaration.nameAsString +
                    "With" +
                    integerToPascalCaseWords(constructorDeclaration.parameters.size()) +
                    "Arguments"
        }

        String asTypeOfArguments() {
            "With" + constructorDeclaration.parameters.collect {
                ParsingUtils.parseSimpleName(it.type.toString())
            }.join("") + "Arguments"
        }
    }


    static String integerToPascalCaseWords(int number) {
        String withDashes = ValueConverters.ENGLISH_INTEGER.asWords(number)
        StreamEx.of(Splitter.on("-").split(withDashes).iterator())
                       .map{it.capitalize()}.joining("")
    }
}


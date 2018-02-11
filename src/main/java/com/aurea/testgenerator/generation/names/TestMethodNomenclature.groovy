package com.aurea.testgenerator.generation.names

import com.aurea.common.ParsingUtils
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.constructors.ConstructorTypes
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.google.common.base.Splitter
import one.util.streamex.StreamEx
import pl.allegro.finance.tradukisto.ValueConverters

class TestMethodNomenclature {

    static final String TEST_NAME_PREFIX = "test"
    static final String TEST_NAME_SPACE = "_"

    Set<String> takenNames = []

    String requestTestMethodName(TestType type, Node context) {
        String testName
        if (type instanceof ConstructorTypes) {
            try {
                testName = requestConstructorTestMethodName(type as ConstructorTypes, context as ConstructorDeclaration)
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("Cannot generate name for $type in $context", cce)
            }
        } else {
            throw new IllegalArgumentException("Cannot generate name for $type in $context")
        }
        takenNames << testName
        return testName
    }

    String requestConstructorTestMethodName(ConstructorTypes type, ConstructorDeclaration constructorDeclaration) {
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
        ConstructorDeclaration constructor

        ConstructorNameRepository(String suffix, ConstructorDeclaration constructor) {
            this.suffix = suffix
            this.constructor = constructor
        }

        String get() {
            String name
            if (constructor.parameters.empty) {
                name = asTypeName()
            } else {
                name = asNumberOfArguments()
            }
            if (name in takenNames) {
                name = asTypeOfArguments()
            }
            name
        }

        String asTypeName() {
            wrap(constructor.nameAsString)
        }

        String asNumberOfArguments() {
            int numberOfParams = constructor.parameters.size()
            String main = constructor.nameAsString +
                    "With" +
                    integerToPascalCaseWords(numberOfParams) +
                    "Argument" + (numberOfParams > 1 ? "s" : "")
            wrap(main)
        }

        String asTypeOfArguments() {
            int numberOfParams = constructor.parameters.size()
            String main = constructor.nameAsString +
                    "With" + constructor.parameters.collect {
                ParsingUtils.parseSimpleName(it.type.toString()).capitalize()
            }.join("") + "Argument" + (numberOfParams > 1 ? "s" : "")
            wrap(main)
        }
        
        String wrap(String main) {
            [TEST_NAME_PREFIX, main, suffix].join(TEST_NAME_SPACE)
        }
    }

    static String integerToPascalCaseWords(int number) {
        String withDashes = ValueConverters.ENGLISH_INTEGER.asWords(number)
        StreamEx.of(Splitter.on("-").split(withDashes).iterator())
                       .map{it.capitalize()}.joining("")
    }
}


package com.aurea.testgenerator.generation.names

import com.aurea.common.ParsingUtils
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.patterns.constructors.ConstructorTypes
import com.aurea.testgenerator.generation.patterns.AbstractFactoryMethodTypes
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.CallableDeclaration
import com.google.common.base.Splitter
import one.util.streamex.StreamEx
import pl.allegro.finance.tradukisto.ValueConverters

class TestMethodNomenclature {

    static final String TEST_NAME_PREFIX = "test"
    static final String TEST_NAME_SPACE = "_"

    private static final Map<? extends TestType, String> TEST_METHOD_NAME_SUFFIXES = [
            (AbstractFactoryMethodTypes.IS_CALLABLE)         : 'IsCallable',
            (AbstractFactoryMethodTypes.ARGUMENT_ASSIGNMENTS): 'AssignsGivenArguments',

            (ConstructorTypes.EMPTY_CONSTRUCTOR)             : 'IsInstantiable',
            (ConstructorTypes.FIELD_LITERAL_ASSIGNMENTS)     : 'AssignsConstants',
            (ConstructorTypes.ARGUMENT_ASSIGNMENTS)          : 'AssignsGivenArguments'
    ].asImmutable()

    Set<String> takenNames = []

    String createTestMethodName(TestType type, Node context) {
        try {
            String suffix = TEST_METHOD_NAME_SUFFIXES[type]

            if (type instanceof ConstructorTypes || type instanceof AbstractFactoryMethodTypes) {
                return new CallableNameRepository(suffix, context as CallableDeclaration).get()
            }

            throw new IllegalArgumentException("Cannot generate name for $type in $context")
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Cannot generate name for $type in $context", cce)
        }
    }

    String requestTestMethodName(TestType type, Node context) {
        String testName = createTestMethodName(type, context)
        takenNames << testName
        testName
    }

    class CallableNameRepository {
        String suffix
        CallableDeclaration callable

        CallableNameRepository(String suffix, CallableDeclaration callable) {
            this.suffix = suffix
            this.callable = callable
        }

        String get() {
            String name
            if (callable.parameters.empty) {
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
            wrap(callable.nameAsString)
        }

        String asNumberOfArguments() {
            int numberOfParams = callable.parameters.size()
            String main = callable.nameAsString +
                    "With" +
                    integerToPascalCaseWords(numberOfParams) +
                    "Argument" + (numberOfParams > 1 ? "s" : "")
            wrap(main)
        }

        String asTypeOfArguments() {
            int numberOfParams = callable.parameters.size()
            String main = callable.nameAsString +
                    "With" + callable.parameters.collect {
                ParsingUtils.parseSimpleName(it.type.toString().replace('[]', 'Array')).capitalize()
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
                .map { it.capitalize() }.joining("")
    }
}


package com.aurea.bigcode.source

import com.aurea.bigcode.source.imports.Imports
import com.github.javaparser.ast.type.Type

class Assertions {
    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOBLE = '0.001D'
    static final FLOATING_POINT_TYPES_FLOAT = ['float', 'Float', 'java.lang.Float']
    static final FLOATING_POINT_TYPES_DOUBLE = ['double', 'Double', 'java.lang.Double']

    static SourceCodeSupplier createPrimitiveAssertion(Type type, SourceCodeSupplier actual, SourceCodeSupplier expected) {
        BasicSourceCodeSupplier result
        switch(type.toString()) {
            case FLOATING_POINT_TYPES_FLOAT:
                result = BasicSourceCodeSupplier
                        .from("assertThat($actual.sourceCode).isCloseTo($expected.sourceCode, Offset.offset($FLOATING_POINT_OFFSET_FLOAT));")
                        .addImports(Imports.ASSERTJ_OFFSET)
                break
            case FLOATING_POINT_TYPES_DOUBLE:
                result = BasicSourceCodeSupplier
                        .from("assertThat($actual.sourceCode).isCloseTo($expected.sourceCode, Offset.offset($FLOATING_POINT_OFFSET_DOBLE));")
                        .addImports(Imports.ASSERTJ_OFFSET)
                break
            default:
                result = BasicSourceCodeSupplier.from("assertThat($actual.sourceCode).isEqualTo($expected.sourceCode);")

        }
        result.addDependencies(actual, expected).addImports(Imports.ASSERTJ_ASSERTTHAT)
    }
}

package com.aurea.bigcode.source

import com.github.javaparser.ast.type.Type

class Assertions {
    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOBLE = '0.001D'
    static final FLOATING_POINT_TYPES_FLOAT = ['float', 'Float', 'java.lang.Float']
    static final FLOATING_POINT_TYPES_DOUBLE = ['double', 'Double', 'java.lang.Double']

    static SourceCodeSupplier createPrimitiveAssertion(Type type, SourceCodeSupplier actual, SourceCodeSupplier expected) {
        switch(type.toString()) {
            case FLOATING_POINT_TYPES_FLOAT:
                return BasicSourceCodeSupplier
                        .from("assertThat($actual.sourceCode).isCloseTo($expected.sourceCode, Offset.offset($FLOATING_POINT_OFFSET_FLOAT));")
                        .addImports(Imports.ASSERTJ_ASSERTTHAT, Imports.ASSERTJ_OFFSET, actual, expected)
            case FLOATING_POINT_TYPES_DOUBLE:
                return BasicSourceCodeSupplier
                        .from("assertThat($actual.sourceCode).isCloseTo($expected.sourceCode, Offset.offset($FLOATING_POINT_OFFSET_DOBLE));")
                        .addImports(Imports.ASSERTJ_ASSERTTHAT, Imports.ASSERTJ_OFFSET, actual, expected)
            default:
                BasicSourceCodeSupplier.from("assertThat($actual.sourceCode).isEqualTo($expected.sourceCode);")
                        .addImports(Imports.ASSERTJ_ASSERTTHAT, actual, expected)
        }
    }
}

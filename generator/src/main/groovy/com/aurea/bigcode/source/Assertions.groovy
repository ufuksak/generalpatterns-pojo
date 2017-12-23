package com.aurea.bigcode.source

import com.github.javaparser.ast.type.Type

class Assertions {
    static final FLOATING_POINT_OFFSET = 0.001F
    static final FLOATING_POINT_TYPES = ['float', 'double', 'Float', 'Double']

    static SourceCodeSupplier createPrimitiveAssertion(Type type, SourceCodeSupplier actual, SourceCodeSupplier expected) {
        if (type.toString() in FLOATING_POINT_TYPES) {
            BasicSourceCodeSupplier
                    .from("assertThat($actual.sourceCode).isCloseTo($expected.sourceCode, Offset.offset($FLOATING_POINT_OFFSET));")
                    .addImports(Imports.ASSERTJ_OFFSET)
        } else {
            BasicSourceCodeSupplier.from("assertThat($actual.sourceCode).isEqualTo($expected.sourceCode);")
        }
    }
}

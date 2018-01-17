package com.aurea.testgenerator.pattern.easy

import groovy.transform.Canonical


@Canonical
class ConstantReturnValue {
    AccessorConstantType type
    String value

    static ConstantReturnValue of(AccessorConstantType type, String value) {
        new ConstantReturnValue(type, value)
    }

    static ConstantReturnValue literal(String value) {
        new ConstantReturnValue(AccessorConstantType.LITERAL, value)
    }

    static ConstantReturnValue nullConstant() {
        new ConstantReturnValue(AccessorConstantType.NULL, "null")
    }

    static ConstantReturnValue objectConstant(String value) {
        new ConstantReturnValue(AccessorConstantType.OBJECT_CONSTANT, value)
    }
}

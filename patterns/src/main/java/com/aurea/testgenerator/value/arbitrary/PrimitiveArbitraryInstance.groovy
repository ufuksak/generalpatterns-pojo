package com.aurea.testgenerator.value.arbitrary

import com.github.javaparser.ast.type.PrimitiveType
import groovy.transform.Canonical


@Canonical
class PrimitiveArbitraryInstance extends ArbitraryInstance {

    PrimitiveType type

    PrimitiveArbitraryInstance(PrimitiveType type, String value) {
        super(value)
        this.type = type
    }

    @Override
    String getTemplate() {
        return '$L'
    }

    @Override
    List<Object> getArgs(NameResolver resolver) {
        return [value]
    }
}

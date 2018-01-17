package com.aurea.testgenerator.value.arbitrary

import com.aurea.testgenerator.value.TestValue
import groovy.transform.Canonical


@Canonical
abstract class ArbitraryInstance implements TestValue {

    String value

    ArbitraryInstance(String value) {
        this.value = value
    }

    boolean canBeInlined() { true }

    @Override
    String get() {
        value
    }

    abstract String getTemplate()

    abstract List<Object> getArgs(NameResolver nameResolver)
}

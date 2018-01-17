package com.aurea.testgenerator.value.arbitrary

import groovy.transform.Canonical


@Canonical
class JavaLangArbitraryInstance extends ArbitraryInstance {

    JavaLangArbitraryInstance(String value) {
        super(value)
    }

    @Override
    String getTemplate() {
        return '$L'
    }

    @Override
    List<Object> getArgs(NameResolver nameResolver) {
        return [value]
    }
}

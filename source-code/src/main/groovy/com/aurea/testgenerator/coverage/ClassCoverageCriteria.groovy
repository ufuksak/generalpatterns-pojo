package com.aurea.testgenerator.coverage

import groovy.transform.Immutable

@Immutable
class ClassCoverageCriteria {

    static final EMPTY = of("", "")

    String className, packageName

    static ClassCoverageCriteria of(String packageName, String className) {
        return new ClassCoverageCriteria(className, packageName)
    }
}

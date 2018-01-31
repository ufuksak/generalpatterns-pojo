package com.aurea.testgenerator.source

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@EqualsAndHashCode
@TupleConstructor
class JavaClass {
    String fullName

    String getPackage() {
        ParsingUtils.parsePackage(fullName)
    }

    String getName() {
        ParsingUtils.parseSimpleName(fullName)
    }
}

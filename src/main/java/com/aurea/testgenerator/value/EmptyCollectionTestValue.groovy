package com.aurea.testgenerator.value

import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import groovy.transform.TupleConstructor

@TupleConstructor
enum EmptyCollectionTestValue implements TestValue {
    LIST("Collections.emptyList()"),
    SET("Collections.emptySet()"),
    MAP("Collections.emptyMap()")

    String expression

    @Override
    String get() {
        expression
    }

    @Override
    ImmutableCollection<String> getImports() {
        ImmutableList.of("java.util.Collections")
    }
}
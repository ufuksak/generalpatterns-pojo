package com.aurea.bigcode

import com.github.javaparser.ast.type.Type
import groovy.transform.Canonical

@Canonical
class Value {
    Type type
    Object value
    String snippet
}

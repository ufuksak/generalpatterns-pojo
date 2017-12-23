package com.aurea.bigcode.executors

import com.github.javaparser.ast.type.Type
import groovy.transform.Canonical


@Canonical
class MethodOutput {
    Type type
    String result
}

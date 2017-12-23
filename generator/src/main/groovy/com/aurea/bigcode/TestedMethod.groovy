package com.aurea.bigcode

import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical


@Canonical
class TestedMethod {
    MethodMetaInformation meta
    MethodDeclaration declaration

    String getCode() {
        declaration.toString()
    }

    String fullName() {
        "$meta.filePath::$declaration.nameAsString"
    }
}

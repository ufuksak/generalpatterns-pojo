package com.aurea.testgenerator.testcase

import com.aurea.testgenerator.source.Unit
import groovy.transform.Canonical

import java.nio.file.Path


@Canonical
class ClassDescription {
    String className
    String packageName
    Collection<String> imports
    Path modulePath

    ClassDescription(Unit unit) {
        className = unit.className
        packageName = unit.packageName
        imports = unit.cu.imports.collect{ it.nameAsString}
        modulePath = unit.modulePath
    }
}

package com.aurea.methobase

import com.github.javaparser.ast.CompilationUnit
import groovy.transform.Canonical

import java.nio.file.Path


@Canonical
class Unit {
    CompilationUnit cu
    String className
    Path modulePath
}

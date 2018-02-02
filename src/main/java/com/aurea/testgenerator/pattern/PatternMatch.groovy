package com.aurea.testgenerator.pattern

import com.github.javaparser.ast.body.CallableDeclaration
import groovy.transform.Canonical

@Canonical
class PatternMatch {
    CallableDeclaration match
    PatternType type
}

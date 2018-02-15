package com.aurea.testgenerator.generation.merge

import com.aurea.testgenerator.generation.DependableNode
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Immutable

@Immutable
class UnitTestMergeConflict {
    DependableNode<MethodDeclaration> left, right
    UnitTestMergeConflictReason reason
}

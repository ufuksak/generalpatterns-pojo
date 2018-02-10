package com.aurea.testgenerator.generation.merge

import com.aurea.testgenerator.generation.TestNodeMethod
import groovy.transform.Immutable

@Immutable
class UnitTestMergeConflict {
    TestNodeMethod left, right
    UnitTestMergeConflictReason reason
}

package com.aurea.testgenerator.generation

import groovy.transform.Immutable

@Immutable
class UnitTestMergeConflict {
    TestNodeMethod left, right
    UnitTestMergeConflictReason reason
}

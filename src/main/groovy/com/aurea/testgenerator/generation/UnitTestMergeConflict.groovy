package com.aurea.testgenerator.generation

import groovy.transform.Immutable

@Immutable
class UnitTestMergeConflict {
    UnitTest left, right
    UnitTestMergeConflictReason reason
}

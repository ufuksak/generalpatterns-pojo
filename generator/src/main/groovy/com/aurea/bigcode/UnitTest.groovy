package com.aurea.bigcode

import com.aurea.bigcode.source.SourceCodeSupplier
import groovy.transform.Canonical

@Canonical
class UnitTest {
    Set<SourceCodeSupplier> fields = []
    Optional<SourceCodeSupplier> methodSetup = Optional.empty()
    Optional<SourceCodeSupplier> classSetup = Optional.empty()
    SourceCodeSupplier method
}

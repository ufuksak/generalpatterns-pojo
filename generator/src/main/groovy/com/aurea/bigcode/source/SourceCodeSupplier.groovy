package com.aurea.bigcode.source

@FunctionalInterface
interface SourceCodeSupplier {
    Set<String> getImports()
    String getSourceCode()
}

package com.aurea.bigcode.source.imports

@FunctionalInterface
interface ImportStatementsSupplier {
    Collection<String> getImports()
}
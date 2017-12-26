package com.aurea.bigcode.source

@FunctionalInterface
interface ImportStatementsSupplier {
    Collection<String> getImports()
}
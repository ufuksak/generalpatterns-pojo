package com.aurea.bigcode.source

class SingleImportStatement implements ImportStatementsSupplier {
    String importStatement

    private SingleImportStatement(String importStatement) {
        this.importStatement = importStatement
    }

    static ImportStatementsSupplier create(String importStatement) {
        new SingleImportStatement(importStatement)
    }

    @Override
    Collection<String> getImports() {
        return List.of(importStatement)
    }
}

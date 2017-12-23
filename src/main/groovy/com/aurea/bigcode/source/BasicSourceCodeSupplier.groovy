package com.aurea.bigcode.source

class BasicSourceCodeSupplier implements SourceCodeSupplier {
    Set<String> imports
    String sourceCode

    private BasicSourceCodeSupplier(String sourceCode) {
        this.sourceCode = sourceCode
        imports = []
    }

    static BasicSourceCodeSupplier from(String sourceCode) {
        new BasicSourceCodeSupplier(sourceCode)
    }

    BasicSourceCodeSupplier addImports(String... imports) {
        this.imports.addAll(imports)
        this
    }

    @Override
    String getSourceCode() {
        return sourceCode
    }

    @Override
    Set<String> getImports() {
        return imports
    }
}

package com.aurea.bigcode.source.imports

class Imports {
    static final ImportStatementsSupplier JUNIT_TEST = createImport('org.junit.Test')
    static final ImportStatementsSupplier JUNIT_RUNWITH = createImport('org.junit.runner.RunWith')
    static final ImportStatementsSupplier JUNITPARAMS_JUNITPARAMSRUNNER = createImport('junitparams.JUnitParamsRunner')
    static final ImportStatementsSupplier JUNITPARAMS_PARAMETERS = createImport('junitparams.Parameters')
    static final ImportStatementsSupplier ASSERTJ_OFFSET = createImport('org.assertj.core.data.Offset')
    static final ImportStatementsSupplier ASSERTJ_ASSERTTHAT = createStaticImport('org.assertj.core.api.Assertions.assertThat')

    static ImportStatementsSupplier createStaticImport(String singleImport) {
        SingleImportStatement.create("import static $singleImport;")
    }

    static ImportStatementsSupplier createImport(String singleImport) {
        SingleImportStatement.create("import $singleImport;")
    }
}

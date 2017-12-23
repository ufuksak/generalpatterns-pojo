package com.aurea.bigcode.source

class Imports {
    static final String JUNIT_TEST = createImport('org.junit.Test')
    static final String JUNITPARAMS_PARAMETERS = createImport('junitparams.Parameters')
    static final String ASSERTJ_OFFSET = createImport('org.assertj.core.data.Offset')

    static String createStaticImport(String singleImport) {
        "    import staric $singleImport;"
    }

    static String createImport(String singleImport) {
        "    import $singleImport;"
    }
}

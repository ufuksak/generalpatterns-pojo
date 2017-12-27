package com.aurea.bigcode.source

import com.aurea.bigcode.source.imports.ImportStatementsSupplier
import com.aurea.bigcode.source.runner.RunnerConfigurationsSupplier

interface SourceCodeSupplier extends ImportStatementsSupplier, RunnerConfigurationsSupplier {
    String getSourceCode()
}

package com.aurea.bigcode.source

import com.aurea.bigcode.source.imports.ImportStatementsSupplier
import com.aurea.bigcode.source.runner.RunnerConfiguration
import com.aurea.bigcode.source.runner.RunnerConfigurationsSupplier

class BasicSourceCodeSupplier implements SourceCodeSupplier {
    Set<RunnerConfiguration> runnerConfigurations
    Set<String> imports
    String sourceCode

    private BasicSourceCodeSupplier(String sourceCode) {
        this.sourceCode = sourceCode
        imports = []
        runnerConfigurations = []
    }

    static BasicSourceCodeSupplier from(String sourceCode) {
        new BasicSourceCodeSupplier(sourceCode)
    }

    BasicSourceCodeSupplier addImports(ImportStatementsSupplier... importStatementsSupplier) {
        importStatementsSupplier.each { imports.addAll(it.imports) }
        this
    }

    BasicSourceCodeSupplier addRunnerConfigurations(RunnerConfigurationsSupplier... runnerConfigurationSupplier) {
        runnerConfigurationSupplier.each { runnerConfigurations.addAll(it.runnerConfigurations) }
        this
    }

    BasicSourceCodeSupplier addDependencies(SourceCodeSupplier... sourceCodeSuppliers) {
        sourceCodeSuppliers.each {
            imports.addAll(it.imports)
            runnerConfigurations.addAll(it.runnerConfigurations)
        }
        this
    }
}

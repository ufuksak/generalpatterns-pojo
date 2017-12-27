package com.aurea.bigcode.source.runner

@FunctionalInterface
interface RunnerConfigurationsSupplier {
    Collection<RunnerConfiguration> getRunnerConfigurations()
}

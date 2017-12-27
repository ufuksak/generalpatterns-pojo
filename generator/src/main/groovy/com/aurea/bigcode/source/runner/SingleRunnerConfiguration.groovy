package com.aurea.bigcode.source.runner

class SingleRunnerConfiguration implements RunnerConfiguration, RunnerConfigurationsSupplier {
    @Override
    Collection<RunnerConfiguration> getRunnerConfigurations() {
        [this]
    }
}

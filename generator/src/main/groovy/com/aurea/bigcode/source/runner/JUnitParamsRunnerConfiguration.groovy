package com.aurea.bigcode.source.runner

import groovy.transform.Canonical

@Canonical
class JUnitParamsRunnerConfiguration extends SingleRunnerConfiguration {
    private JUnitParamsRunnerConfiguration() {}

    static createJUnitParamsRunnerConfiguration() {
        new JUnitParamsRunnerConfiguration()
    }
}

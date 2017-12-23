package com.aurea.bigcode.executors.jshell

import com.aurea.bigcode.executors.MethodInput
import com.aurea.bigcode.executors.context.Context

import groovy.transform.Canonical

@Canonical
class JShellContext implements Context<JShellSetup> {
    static final JShellContext EMPTY = new JShellContext(
            input: MethodInput.NO_INPUT,
            setup: JShellSetup.NO_SETUP
    )

    static JShellContext ofInput(MethodInput input) {
        new JShellContext(
                input: input,
                setup: JShellSetup.NO_SETUP
        )
    }

    static JShellContext ofSetup(JShellSetup setup) {
        new JShellContext(
                input: MethodInput.NO_INPUT,
                setup: setup
        )
    }

    static JShellContext of(MethodInput input, JShellSetup setup) {
        new JShellContext(
                input: input,
                setup: setup
        )
    }

    MethodInput input
    JShellSetup setup
}

package com.aurea.bigcode.executors.context;

import com.aurea.bigcode.executors.MethodInput;

public interface Context<S extends Setup> {

    S getSetup();
    MethodInput getInput();

    default ContextSnapshot makeSnapshot() {
        //TODO: Complete in order to capture state before\after
        //TODO: method execution
        return null;
    }
}

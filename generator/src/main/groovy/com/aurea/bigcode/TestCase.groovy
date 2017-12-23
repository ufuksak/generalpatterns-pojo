package com.aurea.bigcode

import com.aurea.bigcode.executors.MethodInput
import com.aurea.bigcode.executors.MethodOutput

import groovy.transform.Canonical

@Canonical
class TestCase {
    MethodInput input

    // Can be extended to incorporate system state
    MethodOutput output

    // Further initial context configuration


    @Override
    String toString() {
        "$input -> $output"
    }
}

package com.aurea.bigcode.executors.jshell

import com.aurea.bigcode.executors.context.Setup
import groovy.transform.Canonical


@Canonical
class JShellSetup implements Setup {
    static final JShellSetup NO_SETUP = new JShellSetup(snippets: [])

    Collection<String> snippets
}

package com.aurea.testgenerator.generation

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage = false)
class TestGeneratorError {
    String cause
}

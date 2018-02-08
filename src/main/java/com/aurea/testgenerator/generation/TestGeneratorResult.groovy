package com.aurea.testgenerator.generation

import groovy.transform.Canonical


@Canonical
class TestGeneratorResult {
    List<TestGeneratorError> errors = Collections.emptyList()
    List<TestNodeMethod> tests = Collections.emptyList()
    TestType type
}

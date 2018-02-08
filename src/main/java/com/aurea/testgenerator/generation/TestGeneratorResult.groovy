package com.aurea.testgenerator.generation

import groovy.transform.Canonical


@Canonical
class TestGeneratorResult {
    List<TestGeneratorError> errors = []
    List<TestNodeMethod> tests = []
    TestType type
}

package com.aurea.testgenerator.testcase

import com.aurea.testgenerator.source.Unit
import groovy.transform.Canonical


@Canonical
class TestCaseImpl implements TestCase {

    ClassDescription description
    String methodName
    TestCaseType type

    TestCaseImpl(Unit unit, TestCaseType type, String methodName) {
        description = new ClassDescription(
                className: unit.getClassName(),
                packageName: unit.getPackageName(),
                imports: unit.getCu().getImports().collect {it.nameAsString},
                modulePath: unit.getModulePath());
        this.methodName = methodName;
    }

    @Override
    ClassDescription description() {
        return null
    }

    @Override
    TestCaseType type() {
        type
    }
}

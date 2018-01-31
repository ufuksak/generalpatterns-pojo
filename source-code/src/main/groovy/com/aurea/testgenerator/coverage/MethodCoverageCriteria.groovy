package com.aurea.testgenerator.coverage

import groovy.transform.Immutable

@Immutable
class MethodCoverageCriteria {
    ClassCoverageCriteria classCoverageCriteria
    String methodName

    static MethodCoverageCriteria of(String methodName) {
        return new MethodCoverageCriteria(ClassCoverageCriteria.EMPTY, methodName)
    }

    static MethodCoverageCriteria of(String className, String methodName) {
        new MethodCoverageCriteria(ClassCoverageCriteria.of("", className), methodName)
    }

    static MethodCoverageCriteria of(String packageName, String className, String methodName) {
        new MethodCoverageCriteria(ClassCoverageCriteria.of(packageName, className), methodName)
    }

    String getPackageName() {
        classCoverageCriteria.packageName
    }

    String getClassName() {
        classCoverageCriteria.className
    }
}

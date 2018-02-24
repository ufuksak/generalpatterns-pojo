package com.aurea.common

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class JavaClass implements Comparable<JavaClass> {
    String fullName

    JavaClass(String fullName) {
        this.fullName = fullName
    }

    JavaClass(String packageName, String className) {
        this(ParsingUtils.createFullName(packageName, className))
    }

    String getPackage() {
        ParsingUtils.parsePackage(fullName)
    }

    String getName() {
        ParsingUtils.parseSimpleName(fullName)
    }

    @Override
    String toString() {
        fullName
    }

    @Override
    int compareTo(JavaClass o) {
        this.fullName <=> o.fullName
    }
}

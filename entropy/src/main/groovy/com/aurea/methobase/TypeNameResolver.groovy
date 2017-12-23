package com.aurea.methobase

import com.github.javaparser.ast.type.ClassOrInterfaceType


class TypeNameResolver {

    Unit unit

    TypeNameResolver(Unit unit) {
        this.unit = unit
    }

    Optional<String> resolve(ClassOrInterfaceType type) {
        Optional.empty()
    }
}

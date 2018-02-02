package com.aurea.testgenerator.ast

import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.ast.body.ConstructorDeclaration


class InvocationBuilder {

    ValueFactory factory

    InvocationBuilder(ValueFactory factory) {
        this.factory = factory
    }

    void createCall(ConstructorDeclaration cd) {

    }


}

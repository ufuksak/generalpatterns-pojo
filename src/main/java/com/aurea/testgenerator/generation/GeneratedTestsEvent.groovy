package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import groovy.transform.TupleConstructor


@TupleConstructor
class GeneratedTestsEvent extends TestGeneratorEvent {
    GeneratedTestsEvent(Object source, Unit unit, CallableDeclaration callable, TestGeneratorResult result) {
        super(source, unit, callable, result)
    }
}

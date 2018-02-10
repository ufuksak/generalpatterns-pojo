package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration

class ClassifiedButFailedToGenerateEvent extends TestGeneratorEvent {
    ClassifiedButFailedToGenerateEvent(Object source, Unit unit, CallableDeclaration callable, TestGeneratorResult result) {
        super(source, unit, callable, result)
    }
}

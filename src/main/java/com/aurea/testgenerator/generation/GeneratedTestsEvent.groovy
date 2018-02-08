package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration


class GeneratedTestsEvent extends TestGeneratorEvent {
    GeneratedTestsEvent(Object source, Unit unit, CallableDeclaration cd, TestGeneratorResult result) {
        super(source, unit, cd, result)
    }
}

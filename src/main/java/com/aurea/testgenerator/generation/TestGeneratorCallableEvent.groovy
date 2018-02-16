package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.context.ApplicationEvent

class TestGeneratorCallableEvent extends ApplicationEvent {
    TestGeneratorResult result
    CallableDeclaration callable
    Unit unit
    TestGeneratorEventType eventType

    TestGeneratorCallableEvent(Object source, Unit unit, CallableDeclaration callable, TestGeneratorResult result, TestGeneratorEventType eventType) {
        super(source)
        this.unit = unit
        this.result = result
        this.callable = callable
        this.eventType = eventType
    }
}

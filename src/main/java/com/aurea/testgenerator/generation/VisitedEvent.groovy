package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.context.ApplicationEvent

class VisitedEvent extends ApplicationEvent {
    CallableDeclaration callable
    Unit unit
    VisitedEventType eventType

    VisitedEvent(Object source, Unit unit, CallableDeclaration callable, VisitedEventType eventType) {
        super(source)
        this.unit = unit
        this.callable = callable
        this.eventType = eventType
    }
}

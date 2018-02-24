package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.TypeDeclaration
import org.springframework.context.ApplicationEvent

class TestGeneratorTypeEvent extends ApplicationEvent {
    TestGeneratorResult result
    TypeDeclaration type
    Unit unit
    TestGeneratorEventType eventType

    TestGeneratorTypeEvent(Object source,
                           Unit unit,
                           TypeDeclaration type,
                           TestGeneratorResult result,
                           TestGeneratorEventType eventType) {
        super(source)
        this.unit = unit
        this.result = result
        this.type = type
        this.eventType = eventType
    }
}

package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.context.ApplicationEvent

class TestGenerationEvent extends ApplicationEvent {
    TestGeneratorResult result
    List<String> testedMethodSignatures
    Unit unit
    TestGeneratorEventType eventType

    TestGenerationEvent(Object source,
                        Unit unit,
                        List<String> testedMethodSignatures,
                        TestGeneratorResult result,
                        TestGeneratorEventType eventType) {
        super(source)
        this.unit = unit
        this.result = result
        this.testedMethodSignatures = testedMethodSignatures
        this.eventType = eventType
    }
}

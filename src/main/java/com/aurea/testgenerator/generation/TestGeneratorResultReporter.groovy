package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component


@Component
class TestGeneratorResultReporter {

    ApplicationEventPublisher publisher

    @Autowired
    TestGeneratorResultReporter(ApplicationEventPublisher publisher) {
        this.publisher = publisher
    }

    void publish(TestGeneratorResult result, Unit unit, CallableDeclaration callable) {
        if (!result.errors.empty) {
            publisher.publishEvent(new TestGeneratorEvent(this, unit, callable, result, TestGeneratorEventType.GENERATION_FAILURE))
        } else if (!result.tests.empty) {
            publisher.publishEvent(new TestGeneratorEvent(this, unit, callable, result, TestGeneratorEventType.GENERATION_SUCCESS))
        } else {
            publisher.publishEvent(new TestGeneratorEvent(this, unit, callable, result, TestGeneratorEventType.NOT_APPLICABLE))
        }
    }
}

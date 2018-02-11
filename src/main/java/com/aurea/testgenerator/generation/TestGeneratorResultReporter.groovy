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

    void publish(TestGeneratorResult result, Unit unit, CallableDeclaration n) {
        if (result.tests.empty) {
            publisher.publishEvent(new TestGeneratorEvent(this, unit, n, result, TestGeneratorEventType.NOT_CLASSIFIED))
        } else if (!result.errors.empty) {
            publisher.publishEvent(new TestGeneratorEvent(this, unit, n, result, TestGeneratorEventType.CLASSIFIED_BUT_FAILED))
        } else {
            publisher.publishEvent(new TestGeneratorEvent(this, unit, n, result, TestGeneratorEventType.GENERATED))
        }
    }
}

package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
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
        publish(result, unit, Collections.singletonList(callable))
    }

    void publish(TestGeneratorResult result, Unit unit, List<CallableDeclaration> callables) {
        List<String> signatures = callables.collect { it.signature.toString() }
        publishEvent(result, unit, signatures)
    }

    void publishResolved(TestGeneratorResult result, Unit unit, List<ResolvedMethodDeclaration> testedMethods) {
        List<String> signatures = testedMethods.collect { it.signature }
        publishEvent(result, unit, signatures)
    }

    void publishEvent(TestGeneratorResult result, Unit unit, List<String> signatures) {
        if (!result.errors.empty) {
            publisher.publishEvent(new TestGenerationEvent(this, unit, signatures, result, TestGeneratorEventType.GENERATION_FAILURE))
        } else if (!result.tests.empty) {
            publisher.publishEvent(new TestGenerationEvent(this, unit, signatures, result, TestGeneratorEventType.GENERATION_SUCCESS))
        } else {
            publisher.publishEvent(new TestGenerationEvent(this, unit, signatures, result, TestGeneratorEventType.NOT_APPLICABLE))
        }
    }
}

package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component


@Component
abstract class ReportingTestGenerator implements TestGenerator {

    @Autowired
    ApplicationEventPublisher publisher

    void publish(TestGeneratorResult result, Unit unit, CallableDeclaration n) {
        if (result.tests.empty) {
            publisher.publishEvent(new ClassifiedButNoTestsEvent(this, unit, n, result))
        } else if (!result.errors.empty) {
            publisher.publishEvent(new ClassifiedButFailedToGenerateEvent(this, unit, n, result))
        } else {
            publisher.publishEvent(new GeneratedTestsEvent(this, unit, n, result))
        }
    }
}

package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class VisitReporter {

    ApplicationEventPublisher publisher

    @Autowired
    VisitReporter(ApplicationEventPublisher publisher) {
        this.publisher = publisher
    }

    void publishSuccessVisit(Unit unit, CallableDeclaration visitedNode) {
        publisher.publishEvent(new VisitedEvent(this, unit, visitedNode, VisitedEventType.VISITED))
    }

    void publishFailedVisit(Unit unit, CallableDeclaration visitedNode) {
        publisher.publishEvent(new VisitedEvent(this, unit, visitedNode, VisitedEventType.FAILED_TO_VISIT))
    }

    void publishSkippedVisit(Unit unit, CallableDeclaration visitedNode) {
        publisher.publishEvent(new VisitedEvent(this, unit, visitedNode, VisitedEventType.SKIPPED))
    }
}

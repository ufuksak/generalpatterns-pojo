package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.generation.VisitedEvent
import com.aurea.testgenerator.generation.VisitedEventType
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.type.Type
import groovy.util.logging.Log4j2
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

@Component
@Log4j2
class CoverageStatistics implements ApplicationListener<VisitedEvent> {

    private final LongAdder locCounter = new LongAdder()

    Map<String, String> visitedNodes = new ConcurrentHashMap<>()

    @Override
    void onApplicationEvent(VisitedEvent event) {
        if (event.eventType == VisitedEventType.VISITED) {
            Unit unitUnderTest = event.unit
            CallableDeclaration callable = event.callable
            def locs = NodeLocCounter.count(callable)
            if (!visitedBefore(unitUnderTest, callable)) {
                locCounter.add(locs)
            }
            if (callable.isMethodDeclaration()) {
                Type returnType = callable.asMethodDeclaration().type
                log.info("Visiting $unitUnderTest.fullName::$callable.signature [${returnType.asString()}]: $locs locs (${locCounter.longValue()} total)")
            } else {
                log.info("Visiting $unitUnderTest.fullName::$callable.signature: $locs locs (${locCounter.longValue()} total)")
            }
        }
    }

    boolean visitedBefore(Unit unit, CallableDeclaration callableDeclaration) {
        String fullSignature = unit.fullName + "." + callableDeclaration.signature.toString()
        visitedNodes.putIfAbsent(fullSignature, fullSignature) != null
    }
}

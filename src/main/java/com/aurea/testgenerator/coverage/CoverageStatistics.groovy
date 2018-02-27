package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.reporting.CoverageReportEvent
import com.aurea.testgenerator.reporting.CoverageReportEventType
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.type.Type
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder
import java.util.function.Supplier

import static java.lang.System.lineSeparator

@Component
@Log4j2
class CoverageStatistics implements ApplicationListener<CoverageReportEvent> {

    private final LongAdder coveredLinesCounter = new LongAdder()
    private final LongAdder totalVisitedLinesCounter = new LongAdder()

    Map<String, Map<String, Long>> visitedNodesByUnit = new ConcurrentHashMap<>()

    @Override
    void onApplicationEvent(CoverageReportEvent event) {
        Unit unitUnderTest = event.unit
        String unitName = unitUnderTest.fullName

        def visitedNode = visitedNodesByUnit.computeIfAbsent(unitName, {
            totalVisitedLinesCounter.add(NodeLocCounter.count(unitUnderTest.cu))
            new HashMap<String, Long>()
        })

        if (event.eventType != CoverageReportEventType.COVERED) {
            return
        }

        event.callables.each { callable ->
            String signature = getSignature(callable)

            visitedNode.computeIfAbsent(signature, {
                long lines = NodeLocCounter.count(callable)
                coveredLinesCounter.add(lines)
                lines
            })
        }
    }

    @PreDestroy
    void log() {
        if (log.debugEnabled) {
            StringBuilder sb = new StringBuilder()
            EntryStream.of(visitedNodesByUnit)
                       .filterValues { it.size() > 0 }
                       .sortedBy { it.key }
                       .forEach { callables ->

                sb.append(lineSeparator()).append('\t').append(callables.key).append(': ').append(callables.value.values().sum())
                EntryStream.of(callables.value)
                           .sortedBy { it.key }
                           .forEach { sb.append(lineSeparator()).append('\t' * 2).append(it.key).append(': ').append(it.value) }
            }

            log.debug """
$sb              
"""
        }

        if (log.infoEnabled) {
            float percentage = coveredLinesCounter.floatValue() / totalVisitedLinesCounter.floatValue() * 100
            log.info """
\tTotal (${String.format('%.2f', percentage)}%):  $coveredLinesCounter / $totalVisitedLinesCounter
"""
        }
    }

    private static String getSignature(CallableDeclaration callable) {
        if (callable.methodDeclaration) {
            Type returnType = callable.asMethodDeclaration().type
            return returnType.asString() + ' ' + callable.signature.toString()
        }
        callable.signature
    }
}

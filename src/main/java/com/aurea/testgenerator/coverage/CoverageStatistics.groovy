package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.reporting.CoverageReportEvent
import com.aurea.testgenerator.reporting.CoverageReportEventType
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.type.Type
import groovy.util.logging.Log4j2
import one.util.streamex.LongStreamEx
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

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
        if (!visitedNodesByUnit.containsKey(unitName)) {
            totalVisitedLinesCounter.add(NodeLocCounter.count(unitUnderTest.cu))
        }
        if (event.eventType == CoverageReportEventType.COVERED) {
            event.callables.each { callable ->
                String signature = getSignature(callable)
                if (!wasVisitedBefore(unitUnderTest, signature)) {
                    long lines = NodeLocCounter.count(callable)
                    visitedNodesByUnit.computeIfAbsent(unitName, { new ConcurrentHashMap<>() }).put(signature, lines)
                    coveredLinesCounter.add(lines)
                }
            }
        }
    }

    @PreDestroy
    void log() {

        StringBuilder sb = new StringBuilder()
        visitedNodesByUnit.keySet().sort().forEach { unitName ->
            Map<String, Long> callables = visitedNodesByUnit.get(unitName)
            long unitTotal = LongStreamEx.of(callables.values()).sum()
            sb.append(lineSeparator()).append('\t').append(unitName).append(': ').append(unitTotal)
            callables.keySet().sort().forEach { signature ->
                Long lines = callables.get(signature)
                sb.append(lineSeparator()).append('\t' * 2).append(signature).append(': ').append(lines)
            }
        }

        log.debug """
$sb              
"""

        float percentage = coveredLinesCounter.floatValue() / totalVisitedLinesCounter.floatValue() * 100
        log.info """
\tTotal (${String.format('%.2f', percentage)}%):  $coveredLinesCounter / $totalVisitedLinesCounter
"""
    }

    boolean wasVisitedBefore(Unit unit, String signature) {
        String unitName = unit.fullName
        Map<String, Long> callableMap = visitedNodesByUnit.getOrDefault(unitName, Collections.emptyMap())
        callableMap.containsKey(signature)
    }

    private static String getSignature(CallableDeclaration callable) {
        if (callable.methodDeclaration) {
            Type returnType = callable.asMethodDeclaration().type
            return returnType.asString() + ' ' + callable.signature.toString()
        }
        callable.signature
    }
}

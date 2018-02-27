package com.aurea.testgenerator.reporting

import com.aurea.common.JavaClass
import com.aurea.testgenerator.generation.TestGeneratorError
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


@Component
@Log4j2
@Profile("statistics")
class UnitStatistics implements ApplicationListener<TestGenerationEvent> {

    Map<JavaClass, AtomicInteger> testsPerUnit = new ConcurrentHashMap<>()
    Multimap<JavaClass, TestGeneratorError> errorsPerUnit = Multimaps.synchronizedListMultimap(ArrayListMultimap.create())

    @Override
    void onApplicationEvent(TestGenerationEvent event) {
        testsPerUnit.merge(event.unit.javaClass, new AtomicInteger(event.result.tests.size()), { a1, a2 ->
            a1.addAndGet(a2.intValue())
            a1
        })
        errorsPerUnit.putAll(event.unit.javaClass, event.result.errors)
    }

    @PreDestroy
    void logStats() {
        if (log.debugEnabled) {
            String text = """
\tGenerated tests per unit:
\t${EntryStream.of(testsPerUnit).filterValues { it.intValue() > 0 }.join(': ', '\t', System.lineSeparator() + '\t').sort().join("")}
\tErrors per unit:
${printErrorsPerUnit()}
"""
            log.debug text
        }
    }

    private String printErrorsPerUnit() {
        StringBuilder sb = new StringBuilder()
        for (JavaClass javaClass : errorsPerUnit.keySet().sort()) {
            List<TestGeneratorError> errors = errorsPerUnit.get(javaClass)
            sb.append(System.lineSeparator() + '\t')
              .append(javaClass)
              .append(":")
              .append(System.lineSeparator())
              .append('\t\t')
              .append(errors.join(System.lineSeparator() + '\t\t'))
        }
        sb.toString()
    }
}

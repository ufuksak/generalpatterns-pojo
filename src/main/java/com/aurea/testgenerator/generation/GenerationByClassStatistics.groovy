package com.aurea.testgenerator.generation

import com.aurea.common.JavaClass
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder

@Component
@Log4j2
@Profile("class-level")
class GenerationByClassStatistics implements ApplicationListener<TestGeneratorTypeEvent> {

    LongAdder generations = new LongAdder()
    LongAdder tests = new LongAdder()
    Map<TestType, AtomicInteger> testsPerType = new ConcurrentHashMap<>()
    EnumMap<TestGeneratorEventType, LongAdder> counters = new EnumMap<>(TestGeneratorEventType)
    Map<JavaClass, AtomicInteger> testsPerUnit = new ConcurrentHashMap<>()
    Multimap<JavaClass, TestGeneratorError> errorsPerUnit = Multimaps.synchronizedListMultimap(ArrayListMultimap.create())

    @PostConstruct
    setupCounters() {
        for (TestGeneratorEventType type : TestGeneratorEventType.values()) {
            counters[type] = new LongAdder()
        }
    }

    @Override
    void onApplicationEvent(TestGeneratorTypeEvent event) {
        generations.increment()
        counters[event.eventType].increment()
        tests.add(event.result.tests.size())
        testsPerUnit.merge(event.unit.javaClass, new AtomicInteger(event.result.tests.size()), { a1, a2 ->
            a1.addAndGet(a2.intValue())
            a1
        })
        errorsPerUnit.putAll(event.unit.javaClass, event.result.errors)
        testsPerType.merge(event.result.type, new AtomicInteger(event.result.tests.size()), { a1, a2 ->
            a1.addAndGet(a2.intValue())
            a1
        })

    }

    @PreDestroy
    void logStats() {
        String text = """
\tGeneration stats: $generations 
\t${EntryStream.of(counters).join(': ', '\t', System.lineSeparator() + '\t').join("")}
\tGenerated tests per type: $tests                                            
\t${EntryStream.of(testsPerType).join(': ', '\t', System.lineSeparator() + '\t').join("")}
\tGenerated tests per unit:
\t${EntryStream.of(testsPerUnit).join(': ', '\t', System.lineSeparator() + '\t').join("")}
\tErrors per unit:
${printErrorsPerUnit()}
        """
        log.info text
    }

    private String printErrorsPerUnit() {
        StringBuilder sb = new StringBuilder()
        for (JavaClass javaClass : errorsPerUnit.keySet()) {
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

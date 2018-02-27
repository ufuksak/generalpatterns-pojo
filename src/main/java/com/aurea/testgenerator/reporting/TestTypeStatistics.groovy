package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.generation.TestType
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder


@Component
@Log4j2
@Profile("statistics")
class TestTypeStatistics implements ApplicationListener<TestGenerationEvent> {

    private final LongAdder tests = new LongAdder()
    private final Map<TestType, AtomicInteger> testsPerType = new ConcurrentHashMap<>()

    @Override
    void onApplicationEvent(TestGenerationEvent event) {
        tests.add(event.result.tests.size())
        testsPerType.merge(event.result.type, new AtomicInteger(event.result.tests.size()), { a1, a2 ->
            a1.addAndGet(a2.intValue())
            a1
        })
    }

    @PreDestroy
    void logStats() {
        if (log.infoEnabled) {
            String text = """
\tGenerated tests per type: $tests                                            
\t${EntryStream.of(testsPerType).join(': ', '\t', System.lineSeparator() + '\t').sort().join("")}
        """
            log.info text
        }
    }
}

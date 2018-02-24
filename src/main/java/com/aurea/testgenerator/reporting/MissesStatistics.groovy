package com.aurea.testgenerator.reporting

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
@Log4j2
@Profile("statistics")
class MissesStatistics implements ApplicationListener<TestGenerationEvent> {

    Multimap<String, String> skippedCallablesByGenerator = Multimaps.synchronizedListMultimap(ArrayListMultimap.create())
    Map<String, AtomicInteger> callables = new ConcurrentHashMap<>()

    @Override
    void onApplicationEvent(TestGenerationEvent event) {
        event.testedMethodSignatures.each { signature ->
            String callableFullName = event.unit.fullName + '.' + signature
            callables.merge(callableFullName, new AtomicInteger(event.result.tests.size()), { a1, a2 ->
                a1.addAndGet(a2.intValue())
                a1
            })
            if (event.eventType == TestGeneratorEventType.NOT_APPLICABLE) {
                skippedCallablesByGenerator.put(event.result.type.name(), callableFullName)
            }
        }
    }

    @PreDestroy
    void logStats() {
        log.debug """
\t${printSkippedCallables()}      
"""
    }

    private String printSkippedCallables() {
        List<String> missedByAllGenerators = EntryStream.of(callables)
                                                        .filterValues { it.intValue() == 0 }
                                                        .keys().toSet().sort()
        long totalNumberOfCallables = callables.size()
        return "\tMisses: ${missedByAllGenerators.size()} / ${totalNumberOfCallables} " + System.lineSeparator() +
                '\t\t' + StreamEx.of(missedByAllGenerators).joining(System.lineSeparator() + '\t\t')
    }
}

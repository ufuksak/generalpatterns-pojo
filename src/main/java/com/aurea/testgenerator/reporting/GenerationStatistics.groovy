package com.aurea.testgenerator.reporting

import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.atomic.LongAdder

@Component
@Log4j2
@Profile("statistics")
class GenerationStatistics implements ApplicationListener<TestGenerationEvent> {

    LongAdder generations = new LongAdder()
    Map<TestGeneratorEventType, LongAdder> counters = new EnumMap<>(TestGeneratorEventType)

    @PostConstruct
    setupCounters() {
        for (TestGeneratorEventType type : TestGeneratorEventType.values()) {
            counters[type] = new LongAdder()
        }
    }

    @Override
    void onApplicationEvent(TestGenerationEvent event) {
        generations.increment()
        counters[event.eventType].increment()
    }

    @PreDestroy
    void logStats() {
        if (log.infoEnabled) {
            String text = """
\tGeneration stats: $generations 
\t${EntryStream.of(counters).join(': ', '\t', System.lineSeparator() + '\t').join("")}
        """
            log.info text
        }
    }
}
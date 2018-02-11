package com.aurea.testgenerator

import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.source.UnitSource
import com.aurea.testgenerator.source.UnitTestWriter
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder

@Component
@Log4j2
class Pipeline {

    final UnitSource source
    final UnitTestGenerator unitTestGenerator
    final SourceFilter sourceFilter
    final UnitTestWriter unitTestWriter

    @Autowired
    Pipeline(UnitSource unitSource,
             UnitTestGenerator unitTestGenerator,
             SourceFilter sourceFilter,
             UnitTestWriter writer) {
        this.source = unitSource
        this.unitTestGenerator = unitTestGenerator
        this.sourceFilter = sourceFilter
        this.unitTestWriter = writer
    }

    void start() {
        log.info "[$source] ⇒ [$unitTestGenerator]"

        log.info "Getting units from $source"
        StreamEx<Unit> filteredUnits = source.units(sourceFilter)

        long totalUnits = source.size(sourceFilter)
        AtomicInteger counter = new AtomicInteger()
        log.info "Generating tests for ${totalUnits} units"
        filteredUnits
                .map {
                    log.debug "${counter.incrementAndGet()} / $totalUnits: $it.fullName"
                    unitTestGenerator.tryGenerateTest(it)
                }
                .filter { it.present }
                .map { it.get() }
                .each { unitTestWriter.write(it.test) }
    }
}

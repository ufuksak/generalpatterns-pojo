package com.aurea.testgenerator.pipeline

import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.testcase.TestCaseGenerator
import com.aurea.testgenerator.testcase.TestCase
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.source.UnitSource
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.nio.file.Path
import java.util.function.Predicate

@Component
@Log4j2
class TestPipeline {

    final UnitSource source
    final TestCaseGenerator collector
    final TestGenerator generator
    final Predicate<Path> sourceFilter

    @Autowired
    TestPipeline(UnitSource unitSource, TestCaseGenerator collector, TestGenerator generator, SourceFilter sourceFilter) {
        this.source = unitSource
        this.collector = collector
        this.generator = generator
        this.sourceFilter = sourceFilter
    }

    void start() {
        log.info """
            [$source] | [$sourceFilter] ⇒ [$collector] ⇒ [$generator]"
        """
        log.info "Total units after filtering: ${source.size(sourceFilter)}"
        StreamEx<Unit> filteredUnits = source.units(sourceFilter)
        Map<Unit, List<TestCase>> matchesGroupedByType = collector.generate(filteredUnits)

        String statistics = EntryStream.of(matchesGroupedByType).mapValues {it.size()}.join(": ", "\r\n\t", "")
        log.info """Matching statistics: $statistics"""

        generator.accept(matchesGroupedByType)
    }


}

package com.aurea.testgenerator

import com.aurea.testgenerator.generation.UnitTest
import com.aurea.testgenerator.generation.UnitTestCollector
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.PatternMatchCollector
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.source.UnitSource
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class Pipeline {

    final UnitSource source
    final PatternMatchCollector collector
    final UnitTestCollector unitTestGenerator
    final SourceFilter sourceFilter

    @Autowired
    Pipeline(UnitSource unitSource, PatternMatchCollector collector, UnitTestCollector unitTestGenerator, SourceFilter sourceFilter) {
        this.source = unitSource
        this.collector = collector
        this.unitTestGenerator = unitTestGenerator
        this.sourceFilter = sourceFilter
    }

    void start() {
        log.info """[$source] ⇒ [$collector] ⇒ [$unitTestGenerator]"""

        log.info "Getting units from $source"
        StreamEx<Unit> filteredUnits = source.units(sourceFilter)

        log.info "Finding matches in ${source.size(sourceFilter)} units"
        Map<Unit, List<PatternMatch>> matchesByUnit = collector.apply(filteredUnits)

        String matchingStats = EntryStream.of(matchesByUnit).mapValues({ it.size() }).join(": ", "\r\n\t", "").joining("")
        log.info "Matching statistics: $matchingStats"

        log.info "Building unit tests"
        Map<Unit, List<UnitTest>> unitTestsByUnit = unitTestGenerator.apply(matchesByUnit)

        String unitTestStats = EntryStream.of(unitTestsByUnit).mapValues({ it.size() }).join(": ", "\r\n\t", "").joining("")
        log.info "Unit tests produced: ${unitTestStats}"
    }
}
